package io.vproxy.component.secure;

import io.vproxy.base.connection.Protocol;
import io.vproxy.base.util.Network;
import io.vproxy.vfd.IP;

public class SecurityGroupRule {
    public final String alias;
    public final Network network;
    public final Protocol protocol;
    public final int minPort;
    public final int maxPort;
    public final boolean allow;

    public SecurityGroupRule(String alias,
                             Network network,
                             Protocol protocol, int minPort, int maxPort,
                             boolean allow) {
        this.alias = alias;
        this.network = network;
        this.protocol = protocol;
        this.minPort = minPort;
        this.maxPort = maxPort;
        this.allow = allow;
    }

    public boolean match(IP address, int port) {
        return network.contains(address) && matchByPort(port);
    }

    public boolean matchByPort(int port) {
        return minPort <= port && port <= maxPort;
    }

    public boolean ipMaskMatch(SecurityGroupRule rule) {
        return this.network.equals(rule.network);
    }

    @Override
    public String toString() {
        return alias + " -> " + (allow ? "allow" : "deny") + " " + network +
            " protocol " + protocol +
            " port [" + minPort + "," + maxPort + "]";
    }
}
