package io.vproxy.app.app.cmd.handle.param;

import io.vproxy.app.app.cmd.Command;
import io.vproxy.app.app.cmd.Param;
import io.vproxy.base.util.Network;
import io.vproxy.base.util.exception.XException;
import io.vproxy.vfd.IP;

public class NetworkHandle {
    private NetworkHandle() {
    }

    public static void check(Command cmd) throws Exception {
        check(cmd, Param.net);
    }

    public static void check(Command cmd, Param param) throws Exception {
        try {
            get(cmd, param);
        } catch (Exception e) {
            throw new XException("invalid format for " + param.fullname);
        }
    }

    public static Network get(String net) {
        String[] arr = net.split("/");
        if (arr.length > 2)
            throw new IllegalArgumentException();
        byte[] addr = IP.parseIpString(arr[0]);
        byte[] mask = Network.parseMask(Integer.parseInt(arr[1]));
        if (!Network.validNetwork(addr, mask))
            throw new IllegalArgumentException();
        return Network.from(addr, mask);
    }

    public static Network get(Command cmd) {
        return get(cmd, Param.net);
    }

    public static Network get(Command cmd, Param param) {
        String net = cmd.args.get(param);
        return get(net);
    }
}
