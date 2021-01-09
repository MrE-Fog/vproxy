package vproxybase.selector.wrap;

import vfd.FD;
import vfd.IPPort;
import vfd.ServerSocketFD;
import vfd.SocketFD;
import vfd.abs.AbstractBaseFD;
import vfd.type.FDCloseReq;
import vproxybase.selector.SelectorEventLoop;
import vproxybase.util.Comment;
import vproxybase.util.Logger;

import java.io.IOException;
import java.net.SocketOption;
import java.util.concurrent.ConcurrentLinkedQueue;

public abstract class AbstractBaseVirtualServerSocketFD<ACCEPTED extends SocketFD> extends AbstractBaseFD implements ServerSocketFD, VirtualFD {
    private final VirtualFD _self;
    private SelectorEventLoop loop;

    private volatile boolean closed;
    private boolean bond;

    private IPPort local;

    private final ConcurrentLinkedQueue<SocketFD> acceptQueue = new ConcurrentLinkedQueue<>();
    private IOException error;

    protected AbstractBaseVirtualServerSocketFD() {
        this(null);
    }

    protected AbstractBaseVirtualServerSocketFD(VirtualFD _self) {
        this._self = _self;
    }

    protected void checkOpen() throws IOException {
        if (closed) throw new IOException("closed");
    }

    protected void checkBond() throws IOException {
        if (!bond) throw new IOException("bind() not called");
    }

    protected void checkError() throws IOException {
        if (error != null) {
            IOException err = error;
            error = null;
            throw err;
        }
    }

    protected SelectorEventLoop getLoop() {
        return loop;
    }

    @Override
    public boolean loopAware(SelectorEventLoop loop) {
        if (this.loop == null) {
            this.loop = loop;
            return true;
        }
        // check whether loops are the same
        return this.loop == loop;
    }

    @Override
    public IPPort getLocalAddress() throws IOException {
        checkOpen();
        return local;
    }

    @Override
    public ACCEPTED accept() throws IOException {
        checkOpen();
        checkBond();
        checkError();
        SocketFD ret = acceptQueue.poll();
        if (ret == null) {
            cancelReadable();
        }
        //noinspection unchecked
        return (ACCEPTED) ret;
    }

    protected boolean acceptQueueIsEmpty() {
        return acceptQueue.isEmpty();
    }

    protected void newAcceptableFD(SocketFD fd) {
        if (closed) {
            assert Logger.lowLevelDebug("got new accepted fd, but the fd is already closed, fd " + fd + " will be closed");
            try {
                fd.close();
            } catch (IOException ignore) {
            }
            return;
        }
        acceptQueue.add(fd);
        setReadable();
    }

    @Comment("error raised by ServerSocketFD will only trigger once when accept() called")
    protected void raiseErrorOneTime(IOException err) {
        if (closed) {
            assert Logger.lowLevelDebug("got new error, but the fd is already closed, error " + err + " will not raise");
            return;
        }
        error = err;
        if (loop == null) {
            setReadable();
        } else {
            loop.runOnLoop(() -> {
                if (closed) {
                    assert Logger.lowLevelDebug("raising error " + err + " canceled, because the fd is already closed");
                    return;
                }
                setReadable();
            });
        }
    }

    @Override
    public void bind(IPPort l4addr) throws IOException {
        checkOpen();
        if (bond) throw new IOException("bind() already called");
        bond = true;
        local = l4addr;
    }

    @Override
    public void configureBlocking(boolean b) {
        if (b) throw new UnsupportedOperationException();
    }

    @Override
    public <T> void setOption(SocketOption<T> name, T value) {
        // not supported, but do not raise exception
    }

    @Override
    public FD real() {
        return null;
    }

    @Override
    public boolean isOpen() {
        return !closed;
    }

    @SuppressWarnings("unused")
    protected class AbstractBaseVirtualServerSocketFDCloseReturn extends AbstractBaseFDCloseReturn {
        protected AbstractBaseVirtualServerSocketFDCloseReturn(FDCloseReq req, DummyCall unused) throws IOException {
            super(req, dummyCall());
        }

        protected AbstractBaseVirtualServerSocketFDCloseReturn(FDCloseReq req, RealCall realCall) throws IOException {
            super(req, dummyCall());
            close0(req);
        }

        protected AbstractBaseVirtualServerSocketFDCloseReturn(FDCloseReq req, SuperCall realCall) throws IOException {
            super(req, realCall());
        }
    }

    private AbstractBaseVirtualServerSocketFDCloseReturn close0(FDCloseReq req) {
        if (closed) {
            return superClose(req);
        }
        var closeReturn = superClose(req);
        closed = true;
        doClose();
        return closeReturn;
    }

    @Override
    public void close() { // virtual fd must not raise exceptions when closing
        FDCloseReq.inst().wrapClose(this::close);
    }

    @Override
    public AbstractBaseVirtualServerSocketFDCloseReturn close(FDCloseReq req) {
        return close0(req);
    }

    private AbstractBaseVirtualServerSocketFDCloseReturn superClose(FDCloseReq req) {
        try {
            return req.superClose(AbstractBaseVirtualServerSocketFDCloseReturn::new);
        } catch (IOException e) {
            Logger.shouldNotHappen("closing base fd failed", e);
            throw new RuntimeException(e);
        }
    }

    protected abstract void doClose();

    @Override
    public String toString() {
        return formatToString();
    }

    protected abstract String formatToString();

    // =====
    // event
    // =====

    private VirtualFD self() {
        return _self == null ? this : _self;
    }

    private boolean readable = false;

    protected void setReadable() {
        readable = true;
        if (loop == null) {
            return;
        }
        loop.runOnLoop(() -> loop.selector.registerVirtualReadable(self()));
    }

    protected void cancelReadable() {
        readable = false;
        if (loop == null) {
            return;
        }
        loop.runOnLoop(() -> loop.selector.removeVirtualReadable(self()));
    }

    @Override
    public void onRegister() {
        if (readable) {
            setReadable();
        }
    }

    @Override
    public void onRemove() {
        // do nothing
    }
}
