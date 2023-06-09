package io.vproxy.vfd.posix;

import io.vproxy.vfd.IPPort;
import io.vproxy.vfd.ServerSocketFD;
import io.vproxy.vfd.SocketFD;
import io.vproxy.vfd.UDSPath;

import java.io.File;
import java.io.IOException;
import java.net.SocketOption;
import java.net.StandardSocketOptions;

public class UnixDomainServerSocketFD extends PosixNetworkFD implements ServerSocketFD {
    private UDSPath local;
    private File sockFile;

    protected UnixDomainServerSocketFD(Posix posix) throws IOException {
        super(posix);
        this.fd = posix.createUnixDomainSocketFD();
    }

    @Override
    public <T> void setOption(SocketOption<T> name, T value) throws IOException {
        if (name == StandardSocketOptions.SO_RCVBUF) {
            super.setOption(name, value);
        }
    }

    @Override
    public IPPort getLocalAddress() {
        return local;
    }

    @Override
    public SocketFD accept() throws IOException {
        checkNotClosed();
        int subFd = posix.accept(fd);
        if (subFd == 0) {
            return null;
        }
        return new UnixDomainSocketFD(posix, subFd);
    }

    @Override
    public void bind(IPPort l4addr) throws IOException {
        if (!(l4addr instanceof UDSPath)) {
            throw new IllegalArgumentException("unix domain socket cannot take binding argument " + l4addr);
        }
        checkNotClosed();
        if (local != null) {
            throw new IOException("already bond " + local);
        }
        var local = (UDSPath) l4addr;
        posix.bindUnixDomainSocket(fd, local.path);
        this.local = local;

        sockFile = new File(local.path);
        // remove the file on exit
        sockFile.deleteOnExit();
    }

    @Override
    public void close() throws IOException {
        super.close();

        // remove file now
        if (sockFile != null) {
            //noinspection ResultOfMethodCallIgnored
            sockFile.delete();
        }
        sockFile = null;
    }
}
