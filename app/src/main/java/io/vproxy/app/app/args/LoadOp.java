package io.vproxy.app.app.args;

import io.vproxy.app.app.Main;
import io.vproxy.app.app.MainCtx;
import io.vproxy.app.app.MainOp;
import io.vproxy.app.process.Shutdown;
import io.vproxy.base.util.LogType;
import io.vproxy.base.util.Logger;
import io.vproxy.base.util.Utils;
import io.vproxy.base.util.callback.JoinCallback;

import java.io.File;

public class LoadOp implements MainOp {
    @Override
    public String key() {
        return "load";
    }

    @Override
    public int argCount() {
        return 1;
    }

    @Override
    public int order() {
        return 100;
    }

    @Override
    public int pre(MainCtx ctx, String[] args) {
        if (ctx.get("noLoad", false)) {
            System.err.println("load and noLoadLast cannot be set together");
            return 1;
        }
        ctx.set("loaded", true);
        return 0;
    }

    @Override
    public int execute(MainCtx ctx, String[] args) {
        String fileToLoad = Utils.filename(args[0]);
        File f = new File(fileToLoad);
        if (!f.exists()) {
            Logger.warn(LogType.ALERT, "file " + fileToLoad + " does not exist");
            return 0;
        }

        JoinCallback<String, Throwable> cb = new JoinCallback<>(new Main.CallbackInMain());
        try {
            Shutdown.load(args[0], cb);
        } catch (Exception e) {
            System.err.println("got exception when do pre-loading: " + Utils.formatErr(e));
            return 1;
        }
        cb.join();
        return 0;
    }
}
