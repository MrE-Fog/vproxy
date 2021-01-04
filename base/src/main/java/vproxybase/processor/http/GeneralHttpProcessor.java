package vproxybase.processor.http;

import vfd.IPPort;
import vproxybase.processor.Hint;
import vproxybase.processor.Processor;
import vproxybase.processor.http1.HttpProcessor;
import vproxybase.processor.httpbin.BinaryHttpProcessor;
import vproxybase.processor.httpbin.BinaryHttpSubContext;
import vproxybase.processor.httpbin.HttpVersion;
import vproxybase.util.ByteArray;
import vproxybase.util.Logger;

public class GeneralHttpProcessor implements Processor<GeneralHttpContext, GeneralHttpSubContext> {
    private final HttpProcessor httpProcessor = new HttpProcessor();
    private final BinaryHttpProcessor http2Processor = new BinaryHttpProcessor(HttpVersion.HTTP2);

    @Override
    public String name() {
        return "http";
    }

    @Override
    public String[] alpn() {
        String[] h2 = http2Processor.alpn();
        String[] h1 = httpProcessor.alpn();
        String[] ret = new String[h1.length + h2.length];
        System.arraycopy(h2, 0, ret, 0, h2.length);
        System.arraycopy(h1, 0, ret, h2.length, h1.length);
        return ret;
    }

    @Override
    public GeneralHttpContext init(IPPort clientAddress) {
        return new GeneralHttpContext(httpProcessor.init(clientAddress), http2Processor.init(clientAddress));
    }

    @Override
    public GeneralHttpSubContext initSub(GeneralHttpContext ctx, int id, IPPort associatedAddress) {
        return new GeneralHttpSubContext(
            id,
            httpProcessor.initSub(ctx.httpContext, id, associatedAddress),
            http2Processor.initSub(ctx.http2Context, id, associatedAddress)
        );
    }

    @Override
    public Mode mode(GeneralHttpContext ctx, GeneralHttpSubContext subCtx) {
        if (ctx.useHttp) return httpProcessor.mode(ctx.httpContext, subCtx.httpSubContext);
        if (ctx.useHttp2) return http2Processor.mode(ctx.http2Context, subCtx.http2SubContext);
        // if (ctx.willUseHttp2)
        return Mode.handle;
    }

    @Override
    public boolean expectNewFrame(GeneralHttpContext ctx, GeneralHttpSubContext subCtx) {
        if (ctx.useHttp) return httpProcessor.expectNewFrame(ctx.httpContext, subCtx.httpSubContext);
        if (ctx.useHttp2) return http2Processor.expectNewFrame(ctx.http2Context, subCtx.http2SubContext);
        // if (ctx.willUseHttp2)
        return false;
    }

    @Override
    public int len(GeneralHttpContext ctx, GeneralHttpSubContext subCtx) {
        if (ctx.useHttp) return httpProcessor.len(ctx.httpContext, subCtx.httpSubContext);
        if (ctx.useHttp2) return http2Processor.len(ctx.http2Context, subCtx.http2SubContext);
        if (ctx.willUseHttp2) {
            // NOTE: this is the same as (Http2SubContext#len when state == 0) - (the bytes to determine h1/h2);
            return BinaryHttpSubContext.H2_PREFACE.length() - 2;
        }
        return 2; // we only need two bytes to determine whether its h2 or h1
    }

    @Override
    public ByteArray feed(GeneralHttpContext ctx, GeneralHttpSubContext subCtx, ByteArray data) throws Exception {
        if (ctx.useHttp) return httpProcessor.feed(ctx.httpContext, subCtx.httpSubContext, data);
        if (ctx.useHttp2) return http2Processor.feed(ctx.http2Context, subCtx.http2SubContext, data);
        if (ctx.willUseHttp2) {
            ctx.useHttp2 = true;
            return http2Processor.feed(ctx.http2Context, subCtx.http2SubContext, ByteArray.from("PR".getBytes()).concat(data));
        }
        if (data.get(0) == 'P' && data.get(1) == 'R') {
            ctx.willUseHttp2 = true;
        } else {
            ctx.useHttp = true;
            // feed the h1 processor with these two bytes
            ByteArray ret = httpProcessor.feed(ctx.httpContext, subCtx.httpSubContext, data);
            assert ret == null; // the data would be cached inside the processor
        }
        return null;
    }

    @Override
    public ByteArray produce(GeneralHttpContext ctx, GeneralHttpSubContext subCtx) {
        if (ctx.useHttp) return httpProcessor.produce(ctx.httpContext, subCtx.httpSubContext);
        if (ctx.useHttp2) return http2Processor.produce(ctx.http2Context, subCtx.http2SubContext);
        // if (ctx.willUseHttp2)
        return null;
    }

    @Override
    public void proxyDone(GeneralHttpContext ctx, GeneralHttpSubContext subCtx) {
        if (ctx.useHttp) {
            httpProcessor.proxyDone(ctx.httpContext, subCtx.httpSubContext);
            return;
        }
        if (ctx.useHttp2) {
            http2Processor.proxyDone(ctx.http2Context, subCtx.http2SubContext);
            return;
        }
        shouldNotCall("proxyDone");
    }

    @Override
    public int connection(GeneralHttpContext ctx, GeneralHttpSubContext subCtx) {
        if (ctx.useHttp) return httpProcessor.connection(ctx.httpContext, subCtx.httpSubContext);
        if (ctx.useHttp2) return http2Processor.connection(ctx.http2Context, subCtx.http2SubContext);
        return shouldNotCall("connection");
    }

    @Override
    public Hint connectionHint(GeneralHttpContext ctx, GeneralHttpSubContext subCtx) {
        if (ctx.useHttp) return httpProcessor.connectionHint(ctx.httpContext, subCtx.httpSubContext);
        if (ctx.useHttp2) return http2Processor.connectionHint(ctx.http2Context, subCtx.http2SubContext);
        return shouldNotCall("connectionHint");
    }

    @Override
    public void chosen(GeneralHttpContext ctx, GeneralHttpSubContext front, GeneralHttpSubContext subCtx) {
        if (ctx.useHttp) {
            httpProcessor.chosen(ctx.httpContext, front.httpSubContext, subCtx.httpSubContext);
            return;
        }
        if (ctx.useHttp2) {
            http2Processor.chosen(ctx.http2Context, front.http2SubContext, subCtx.http2SubContext);
            return;
        }
        shouldNotCall("chosen");
    }

    @Override
    public ByteArray connected(GeneralHttpContext ctx, GeneralHttpSubContext subCtx) {
        if (ctx.useHttp) return httpProcessor.connected(ctx.httpContext, subCtx.httpSubContext);
        if (ctx.useHttp2) return http2Processor.connected(ctx.http2Context, subCtx.http2SubContext);
        // if (ctx.willUseHttp2)
        return null;
    }

    private <T> T shouldNotCall(String methodName) {
        String errMsg = "should not call " + methodName + "() when h1/h2 not determined";
        Logger.shouldNotHappen(errMsg);
        throw new RuntimeException(errMsg);
    }
}
