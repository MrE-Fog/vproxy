package vproxy.app.app.cmd.handle.resource;

import vproxy.app.app.Application;
import vproxy.app.app.cmd.Command;
import vproxy.app.app.cmd.Resource;
import vproxy.app.app.cmd.ResourceType;
import vproxy.base.util.exception.XException;
import vproxy.component.app.TcpLB;
import vproxy.component.svrgroup.Upstream;

import java.util.List;

public class UpstreamHandle {
    private UpstreamHandle() {
    }

    public static Upstream get(Resource r) throws Exception {
        return Application.get().upstreamHolder.get(r.alias);
    }

    public static void checkUpstream(Resource upstream) throws Exception {
        if (upstream.parentResource != null)
            throw new Exception(upstream.type.fullname + " is on top level");
    }

    public static List<String> names() {
        return Application.get().upstreamHolder.names();
    }

    public static void add(Command cmd) throws Exception {
        Application.get().upstreamHolder.add(cmd.resource.alias);
    }

    public static void preRemoveCheck(Command cmd) throws Exception {
        // whether used by lb ?
        Upstream groups = Application.get().upstreamHolder.get(cmd.resource.alias);
        List<String> lbNames = Application.get().tcpLBHolder.names();
        for (String lbName : lbNames) {
            TcpLB tcpLB = Application.get().tcpLBHolder.get(lbName);
            if (tcpLB.backend.equals(groups))
                throw new XException(ResourceType.ups.fullname + " " + cmd.resource.alias
                    + " is used by " + ResourceType.tl.fullname + " " + tcpLB.alias);
        }
    }

    public static void forceRemove(Command cmd) throws Exception {
        Application.get().upstreamHolder.remove(cmd.resource.alias);
    }
}