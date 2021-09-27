module io.vproxy.app {
    requires jdk.unsupported;
    requires kotlin.stdlib;
    requires kotlinx.coroutines.core.jvm;
    requires io.vproxy.dep;
    requires io.vproxy.base;
    requires io.vproxy.core;
    requires io.vproxy.extended;
    requires io.vproxy.lib;

    exports io.vproxy.app.app;
    exports io.vproxy.app.app.args;
    exports io.vproxy.app.app.cmd;
    exports io.vproxy.app.app.cmd.handle.param;
    exports io.vproxy.app.app.cmd.handle.resource;
    exports io.vproxy.app.app.util;
    exports io.vproxy.app.controller;
    exports io.vproxy.app.plugin;
    exports io.vproxy.app.plugin.impl;
    exports io.vproxy.app.process;
    exports io.vproxy.app.vproxyx;
}
