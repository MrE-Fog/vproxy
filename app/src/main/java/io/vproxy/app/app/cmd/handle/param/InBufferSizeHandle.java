package io.vproxy.app.app.cmd.handle.param;

import io.vproxy.app.app.cmd.Command;
import io.vproxy.app.app.cmd.Param;
import io.vproxy.base.util.exception.XException;

public class InBufferSizeHandle {
    private InBufferSizeHandle() {
    }

    public static void check(Command cmd) throws Exception {
        try {
            Integer.parseInt(cmd.args.get(Param.inbuffersize));
        } catch (Exception e) {
            throw new XException("invalid " + Param.inbuffersize.fullname);
        }
    }

    public static int get(Command cmd, int defaultValue) {
        if (!cmd.args.containsKey(Param.inbuffersize)) {
            return defaultValue;
        }
        return Integer.parseInt(cmd.args.get(Param.inbuffersize));
    }
}
