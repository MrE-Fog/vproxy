package vproxy.lib.http1

import kotlinx.coroutines.suspendCancellableCoroutine
import vjson.JSON
import vproxy.base.connection.ConnectableConnection
import vproxy.base.connection.ConnectionOpts
import vproxy.base.dns.Resolver
import vproxy.base.http.HttpRespParser
import vproxy.base.processor.http1.entity.Header
import vproxy.base.processor.http1.entity.Request
import vproxy.base.processor.http1.entity.Response
import vproxy.base.selector.SelectorEventLoop
import vproxy.base.util.ByteArray
import vproxy.base.util.RingBuffer
import vproxy.base.util.callback.Callback
import vproxy.base.util.promise.Promise
import vproxy.base.util.ringbuffer.SSLUtils
import vproxy.base.util.thread.VProxyThread
import vproxy.lib.common.coroutine
import vproxy.lib.common.execute
import vproxy.lib.common.unsafeIO
import vproxy.lib.tcp.CoroutineConnection
import vproxy.vfd.IP
import vproxy.vfd.IPPort
import java.io.IOException
import java.net.UnknownHostException
import javax.net.ssl.SSLEngine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@Suppress("LiftReturnOrAssignment")
class CoroutineHttp1ClientConnection(val conn: CoroutineConnection) : AutoCloseable {
  fun get(url: String): CoroutineHttp1Request {
    return request("GET", url)
  }

  fun post(url: String): CoroutineHttp1Request {
    return request("POST", url)
  }

  fun put(url: String): CoroutineHttp1Request {
    return request("PUT", url)
  }

  fun del(url: String): CoroutineHttp1Request {
    return request("DELETE", url)
  }

  fun request(method: String, url: String): CoroutineHttp1Request {
    return CoroutineHttp1Request(method, url)
  }

  inner class CoroutineHttp1Request(private val method: String, private val url: String) :
    CoroutineHttp1Common(conn) {
    private val headers = ArrayList<Header>()

    fun header(key: String, value: String): CoroutineHttp1Request {
      headers.add(Header(key, value))
      return this
    }

    suspend fun send() {
      send(null)
    }

    suspend fun send(body: String) {
      send(ByteArray.from(body))
    }

    suspend fun send(json: JSON.Instance<*>) {
      send(json.stringify())
    }

    @Suppress("DuplicatedCode")
    suspend fun send(body: ByteArray?) {
      val req = Request()
      req.method = method
      req.uri = url
      req.version = "HTTP/1.1"
      if (body != null && body.length() > 0) {
        headers.add(Header("content-length", "" + body.length()))
      } else {
        headers.add(Header("content-length", "0"))
      }
      req.headers = headers
      req.body = body
      conn.write(req.toByteArray())
    }

    override suspend fun sendHeadersBeforeChunks() {
      val req = Request()
      req.method = method
      req.uri = url
      req.version = "HTTP/1.1"
      headers.add(Header("transfer-encoding", "chunked"))
      req.headers = headers
      conn.write(req.toByteArray())
    }
  }

  /**
   * @return a full response object including body or chunks/trailers.
   * If eof received, an IOException would be thrown instead of returning null Response
   */
  suspend fun readResponse(): Response {
    val parser = HttpRespParser(true)
    while (true) {
      val rb = conn.read() ?: throw IOException("unexpected eof")
      val res = parser.feed(rb)
      if (res == -1) {
        if (parser.errorMessage != null) {
          throw IOException(parser.errorMessage)
        }
        continue
      }
      // done
      return parser.result
    }
  }

  override fun close() {
    conn.close()
  }

  @Suppress("HttpUrlsUsage", "CascadeIf")
  companion object {
    @JvmStatic
    fun simpleGet(full: String): Promise<ByteArray> {
      val invokerLoop = SelectorEventLoop.current()

      val protocolAndHostAndPort: String
      val uri: String

      val protocol: String
      val hostAndPortAndUri: String
      if (full.startsWith("http://")) {
        protocol = "http://"
        hostAndPortAndUri = full.substring("http://".length)
      } else if (full.startsWith("https://")) {
        protocol = "https://"
        hostAndPortAndUri = full.substring("https://".length)
      } else {
        throw Exception("unknown protocol in $full")
      }
      if (hostAndPortAndUri.contains("/")) {
        protocolAndHostAndPort = protocol + hostAndPortAndUri.substring(0, hostAndPortAndUri.indexOf("/"))
        uri = hostAndPortAndUri.substring(hostAndPortAndUri.indexOf("/"))
      } else {
        protocolAndHostAndPort = hostAndPortAndUri
        uri = "/"
      }

      val loop: SelectorEventLoop
      if (invokerLoop == null) {
        loop = SelectorEventLoop.open()
        loop.ensureNetEventLoop()
        loop.loop { VProxyThread.create(it, "http1-simple-get") }
      } else {
        loop = invokerLoop
      }

      return loop.execute {
        val conn = create(protocolAndHostAndPort)
        defer { conn.close() }
        if (invokerLoop == null) { // which means a new loop is created
          defer { loop.close() }
        }

        conn.conn.setTimeout(5_000)
        conn.get(uri).send()
        val resp = conn.readResponse()
        val ret: ByteArray
        if (resp.statusCode != 200) {
          throw IOException("request failed: response status is " + resp.statusCode + " instead of 200")
        } else if (resp.body == null) {
          ret = ByteArray.allocate(0)
        } else {
          ret = resp.body
        }
        ret
      }
    }

    suspend fun create(protocolAndHostAndPort: String): CoroutineHttp1ClientConnection {
      val ssl: Boolean
      val hostAndPort: String
      val host: String
      val port: Int

      if (protocolAndHostAndPort.startsWith("http://")) {
        ssl = false
        hostAndPort = protocolAndHostAndPort.substring("http://".length)
      } else if (protocolAndHostAndPort.startsWith("https://")) {
        ssl = true
        hostAndPort = protocolAndHostAndPort.substring("https://".length)
      } else {
        ssl = false
        hostAndPort = protocolAndHostAndPort
      }
      if (IP.isIpLiteral(hostAndPort)) {
        host = hostAndPort
        port = if (ssl) {
          443
        } else {
          80
        }
      } else if (hostAndPort.contains(":")) {
        host = hostAndPort.substring(0, hostAndPort.lastIndexOf(":"))
        val portStr = hostAndPort.substring(hostAndPort.lastIndexOf(":") + 1)
        try {
          port = Integer.parseInt(portStr)
        } catch (e: NumberFormatException) {
          throw IllegalArgumentException("invalid port number")
        }
      } else {
        host = hostAndPort
        port = if (ssl) {
          443
        } else {
          80
        }
      }

      // resolve
      val ip = if (IP.isIpLiteral(host)) {
        IP.from(host)
      } else {
        suspendCancellableCoroutine { cont ->
          Resolver.getDefault().resolve(host, object : Callback<IP, UnknownHostException>() {
            override fun onSucceeded(value: IP) {
              cont.resume(value)
            }

            override fun onFailed(err: UnknownHostException) {
              cont.resumeWithException(err)
            }
          })
        }
      }

      // now try to connect
      if (ssl) {
        val sslContext = SSLUtils.getDefaultClientSSLContext()
        val engine: SSLEngine
        if (IP.isIpLiteral(host)) {
          engine = sslContext.createSSLEngine()
        } else {
          engine = sslContext.createSSLEngine(host, port)
        }
        return create(IPPort(ip, port), engine)
      } else {
        return create(IPPort(ip, port))
      }
    }

    suspend fun create(ipport: IPPort, engine: SSLEngine): CoroutineHttp1ClientConnection {
      engine.useClientMode = true
      val pair = SSLUtils.genbuf(
        engine, RingBuffer.allocate(24576), RingBuffer.allocate(24576),
        SelectorEventLoop.current(), ipport
      )
      val conn = unsafeIO {
        ConnectableConnection.create(
          ipport, ConnectionOpts(),
          pair.left, pair.right
        ).coroutine()
      }
      conn.connect()
      return conn.asHttp1ClientConnection()
    }

    suspend fun create(ipport: IPPort): CoroutineHttp1ClientConnection {
      val conn = unsafeIO {
        ConnectableConnection.create(
          ipport, ConnectionOpts(),
          RingBuffer.allocate(16384), RingBuffer.allocate(16384)
        ).coroutine()
      }
      conn.connect()
      return conn.asHttp1ClientConnection()
    }
  }
}
