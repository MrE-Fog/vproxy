package io.vproxy.app.app.cmd.handle.param;

import io.vproxy.app.app.cmd.Command;
import io.vproxy.app.app.cmd.Param;
import io.vproxy.base.util.exception.XException;
import io.vproxy.vfd.IP;

public class IpParamHandle {
    private IpParamHandle() {
    }

    public static IP get(Command cmd) throws Exception {
        String ipStr = cmd.args.get(Param.ip);
        return IP.from(ipStr);
    }

    public static void check(Command cmd) throws Exception {
        if (!cmd.args.containsKey(Param.ip))
            throw new Exception("missing argument " + Param.ip.fullname);

        try {
            get(cmd);
        } catch (Exception e) {
            throw new XException("invalid format for " + Param.ip.fullname);
        }
    }
}
