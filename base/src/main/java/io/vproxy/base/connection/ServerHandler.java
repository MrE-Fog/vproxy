package io.vproxy.base.connection;

import io.vproxy.base.util.RingBuffer;
import io.vproxy.base.util.coll.Tuple;
import io.vproxy.vfd.SocketFD;

import java.io.IOException;

public interface ServerHandler {
    void acceptFail(ServerHandlerContext ctx, IOException err);

    void connection(ServerHandlerContext ctx, Connection connection);

    // <in buffer, out buffer>
    Tuple<RingBuffer, RingBuffer> getIOBuffers(SocketFD channel);

    void removed(ServerHandlerContext ctx);

    default void exception(ServerHandlerContext ctx, IOException err) {
        // do nothing
    }

    default ConnectionOpts connectionOpts() {
        return DefaultConnectionOpts.defaultConnectionOpts;
    }
}
