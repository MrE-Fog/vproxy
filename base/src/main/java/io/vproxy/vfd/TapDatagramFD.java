package io.vproxy.vfd;

public interface TapDatagramFD extends AbstractDatagramFD<NoSockAddr> {
    TapInfo getTap();

    boolean isTun();
}
