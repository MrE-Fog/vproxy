package io.vproxy.vswitch.plugin;

public enum FilterResult {
    PASS,
    DROP,
    REDIRECT,
    TX,
    L3_TX,
    L4_TX,
    ABORT,
}
