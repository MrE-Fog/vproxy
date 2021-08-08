package vproxy.app.app.cmd.handle.resource;

import vproxy.app.app.Application;
import vproxy.app.app.cmd.Command;
import vproxy.app.app.cmd.Param;
import vproxy.app.app.cmd.Resource;
import vproxy.app.app.cmd.ResourceType;
import vproxy.app.app.cmd.handle.param.FloodHandle;
import vproxy.app.app.cmd.handle.param.MTUHandle;
import vproxy.base.util.exception.NotFoundException;
import vproxy.vswitch.Switch;
import vproxy.vswitch.iface.Iface;

import java.util.List;

public class IfaceHandle {
    private IfaceHandle() {
    }

    public static int count(Resource parent) throws Exception {
        return list(parent).size();
    }

    public static List<Iface> list(Resource parent) throws Exception {
        Switch sw = Application.get().switchHolder.get(parent.alias);
        return sw.getIfaces();
    }

    public static void update(Command cmd) throws Exception {
        List<Iface> ifaces = list(cmd.resource.parentResource);
        String name = cmd.resource.alias;

        Iface target = null;
        for (Iface iface : ifaces) {
            if (iface.name().equals(name)) {
                target = iface;
                break;
            }
        }
        if (target == null) {
            throw new NotFoundException(ResourceType.iface.fullname, cmd.resource.alias);
        }
        if (cmd.args.containsKey(Param.mtu)) {
            target.setBaseMTU(MTUHandle.get(cmd));
        }
        if (cmd.args.containsKey(Param.flood)) {
            target.setFloodAllowed(FloodHandle.get(cmd));
        }
    }
}
