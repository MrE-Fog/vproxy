package io.vproxy.app.app.cmd.handle.resource;

import io.vproxy.app.app.cmd.Command;
import io.vproxy.app.app.cmd.Resource;
import io.vproxy.app.app.cmd.handle.param.NetworkHandle;
import io.vproxy.app.app.cmd.handle.param.PortRangeHandle;
import io.vproxy.app.app.cmd.handle.param.ProtocolHandle;
import io.vproxy.app.app.cmd.handle.param.SecGRDefaultHandle;
import io.vproxy.base.connection.Protocol;
import io.vproxy.base.util.Network;
import io.vproxy.base.util.coll.Tuple;
import io.vproxy.component.secure.SecurityGroup;
import io.vproxy.component.secure.SecurityGroupRule;

import java.util.List;
import java.util.stream.Collectors;

public class SecurityGroupRuleHandle {
    private SecurityGroupRuleHandle() {
    }

    public static List<String> names(Resource parent) throws Exception {
        SecurityGroup grp = SecurityGroupHandle.get(parent);
        return grp.getRules().stream().map(r -> r.alias).collect(Collectors.toList());
    }

    public static List<SecurityGroupRule> detail(Resource parent) throws Exception {
        SecurityGroup grp = SecurityGroupHandle.get(parent);
        return grp.getRules();
    }

    public static void remove(Command cmd) throws Exception {
        SecurityGroup grp = SecurityGroupHandle.get(cmd.prepositionResource);
        grp.removeRule(cmd.resource.alias);
    }

    public static void add(Command cmd) throws Exception {
        SecurityGroup grp = SecurityGroupHandle.get(cmd.prepositionResource);

        Network net = NetworkHandle.get(cmd);
        Protocol protocol = ProtocolHandle.get(cmd);
        Tuple<Integer, Integer> range = PortRangeHandle.get(cmd);
        boolean allow = SecGRDefaultHandle.get(cmd);

        SecurityGroupRule rule = new SecurityGroupRule(
            cmd.resource.alias, net,
            protocol, range.left, range.right,
            allow
        );

        grp.addRule(rule);
    }
}
