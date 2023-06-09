package io.vproxy.app.app.cmd.handle.resource;

import io.vproxy.app.app.cmd.Command;
import io.vproxy.base.dns.Cache;
import io.vproxy.base.dns.Resolver;

import java.util.LinkedList;
import java.util.List;

public class DnsCacheHandle {
    private DnsCacheHandle() {
    }

    public static int count() {
        Resolver resolver = Resolver.getDefault();
        return resolver.cacheCount();
    }

    public static List<Cache> detail() {
        List<Cache> caches = new LinkedList<>();
        Resolver.getDefault().copyCache(caches);
        return caches;
    }

    public static void remove(Command cmd) {
        List<Cache> caches = detail();
        String host = cmd.resource.alias;
        for (Cache c : caches) {
            if (c.host.equals(host)) {
                c.remove();
                // there can be no other cache with the same host
                break;
            }
        }
    }
}
