package io.vproxy.base.selector.wrap;

// only works on non-virtual fds
public interface WritableAware {
    void writable();
}
