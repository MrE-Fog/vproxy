package io.vproxy.app.app.cmd.handle.resource;

import io.vproxy.app.app.Application;
import io.vproxy.app.app.cmd.Command;
import io.vproxy.app.app.cmd.Param;
import io.vproxy.app.app.cmd.Resource;
import io.vproxy.app.app.cmd.handle.param.AddrHandle;
import io.vproxy.app.app.cmd.handle.param.TTLHandle;
import io.vproxy.base.component.elgroup.EventLoopGroup;
import io.vproxy.base.util.exception.NotFoundException;
import io.vproxy.component.secure.SecurityGroup;
import io.vproxy.component.svrgroup.Upstream;
import io.vproxy.dns.DNSServer;
import io.vproxy.vfd.IPPort;

import java.util.LinkedList;
import java.util.List;

public class DNSServerHandle {
    private DNSServerHandle() {
    }

    public static DNSServer get(Resource dnsServer) throws NotFoundException {
        return Application.get().dnsServerHolder.get(dnsServer.alias);
    }

    public static List<String> names() {
        return Application.get().dnsServerHolder.names();
    }

    public static List<DNSServerRef> details() throws Exception {
        List<DNSServerRef> result = new LinkedList<>();
        for (String name : names()) {
            result.add(new DNSServerRef(
                Application.get().dnsServerHolder.get(name)
            ));
        }
        return result;
    }

    @SuppressWarnings("Duplicates")
    public static void add(Command cmd) throws Exception {
        if (!cmd.args.containsKey(Param.elg)) {
            cmd.args.put(Param.elg, Application.DEFAULT_WORKER_EVENT_LOOP_GROUP_NAME);
        }

        String alias = cmd.resource.alias;
        EventLoopGroup eventLoopGroup = Application.get().eventLoopGroupHolder.get(cmd.args.get(Param.elg));
        IPPort addr = AddrHandle.get(cmd);
        Upstream backend = Application.get().upstreamHolder.get(cmd.args.get(Param.ups));
        int ttl;
        if (cmd.args.containsKey(Param.ttl)) {
            ttl = TTLHandle.get(cmd);
        } else {
            ttl = 0;
        }
        SecurityGroup secg;
        if (cmd.args.containsKey(Param.secg)) {
            secg = SecurityGroupHandle.get(cmd.args.get(Param.secg));
        } else {
            secg = SecurityGroup.allowAll();
        }
        Application.get().dnsServerHolder.add(alias, addr, eventLoopGroup, backend, ttl, secg);
    }

    public static void update(Command cmd) throws Exception {
        DNSServer dnsServer = get(cmd.resource);

        if (cmd.args.containsKey(Param.ttl)) {
            dnsServer.ttl = TTLHandle.get(cmd);
        }
        if (cmd.args.containsKey(Param.secg)) {
            dnsServer.securityGroup = SecurityGroupHandle.get(cmd.args.get(Param.secg));
        }
    }

    public static void remove(Command cmd) throws Exception {
        Application.get().dnsServerHolder.removeAndStop(cmd.resource.alias);
    }

    public static class DNSServerRef {
        public final DNSServer dnsServer;

        public DNSServerRef(DNSServer dnsServer) {
            this.dnsServer = dnsServer;
        }

        @Override
        public String toString() {
            return dnsServer.alias + " -> event-loop-group " + dnsServer.eventLoopGroup.alias
                + " bind " + dnsServer.bindAddress.getAddress().formatToIPString() + ":" + dnsServer.bindAddress.getPort()
                + " rrsets " + dnsServer.rrsets.alias
                + " ttl " + dnsServer.ttl
                + " security-group " + dnsServer.securityGroup.alias;
        }
    }
}
