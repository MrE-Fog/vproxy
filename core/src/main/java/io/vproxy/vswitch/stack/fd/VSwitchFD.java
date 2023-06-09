package io.vproxy.vswitch.stack.fd;

import io.vproxy.base.selector.wrap.VirtualFD;
import io.vproxy.vfd.FD;

import java.io.IOException;
import java.net.SocketOption;

public abstract class VSwitchFD implements FD, VirtualFD {
    protected final VSwitchFDContext ctx;
    protected boolean closed = false;

    protected VSwitchFD(VSwitchFDContext ctx) {
        this.ctx = ctx;
    }

    protected void checkNotClosed() throws IOException {
        if (closed) {
            throw new IOException("closed");
        }
    }

    @Override
    public void configureBlocking(boolean b) {
        if (b) {
            throw new UnsupportedOperationException("blocking mode not supported");
        }
    }

    @Override
    public <T> void setOption(SocketOption<T> name, T value) {
        // do nothing
    }

    @Override
    public FD real() {
        return this;
    }

    @Override
    public boolean contains(FD fd) {
        return false;
    }

    @Override
    public boolean isOpen() {
        return !closed;
    }
}
