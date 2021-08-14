package vproxy.app.controller;

import vproxy.app.app.Application;
import vproxy.app.app.cmd.Command;
import vproxy.app.app.cmd.handle.resource.BPFObjectHandle;
import vproxy.app.app.cmd.handle.resource.SwitchHandle;
import vproxy.base.component.elgroup.EventLoopGroup;
import vproxy.base.util.*;
import vproxy.base.util.coll.IntMap;
import vproxy.base.util.exception.NotFoundException;
import vproxy.component.secure.SecurityGroup;
import vproxy.vfd.*;
import vproxy.vswitch.Switch;
import vproxy.vswitch.Table;
import vproxy.vswitch.dispatcher.BPFMapKeySelectors;
import vproxy.vswitch.iface.XDPIface;
import vproxy.xdp.BPFMode;
import vproxy.xdp.UMem;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class DockerNetworkDriverImpl implements DockerNetworkDriver {
    private static final String SWITCH_NAME = "sw-docker";
    private static final String UMEM_NAME = "umem0";
    private static final String TABLE_NETWORK_ID_ANNOTATION = AnnotationKeys.SWTable_DockerNetworkDriverNetworkId.name;
    private static final MacAddress GATEWAY_MAC_ADDRESS = new MacAddress("02:00:00:00:00:20");
    private static final String GATEWAY_IP_ANNOTATION = AnnotationKeys.SWIp_DockerNetworkDriverGatewayIp.name;
    private static final String GATEWAY_IPv4_FLAG_VALUE = "gateway-ipv4";
    private static final String GATEWAY_IPv6_FLAG_VALUE = "gateway-ipv6";
    private static final String VETH_ENDPOINT_ID_ANNOTATION = AnnotationKeys.SWIface_DockerNetworkDriverEndpointId.name;
    private static final String VETH_ENDPOINT_IPv4_ANNOTATION = AnnotationKeys.SWIface_DockerNetworkDriverEndpointIpv4.name;
    private static final String VETH_ENDPOINT_IPv6_ANNOTATION = AnnotationKeys.SWIface_DockerNetworkDriverEndpointIpv6.name;
    private static final String VETH_ENDPOINT_MAC_ANNOTATION = AnnotationKeys.SWIface_DockerNetworkDriverEndpointMac.name;
    private static final String CONTAINER_VETH_SUFFIX = "pod";
    private static final String NETWORK_ENTRY_VETH_PREFIX = "vp-docker";
    private static final String NETWORK_ENTRY_VETH_PEER_SUFFIX = "sw";
    private static final int NETWORK_ENTRY_VNI = 99999;

    @Override
    public synchronized void createNetwork(CreateNetworkRequest req) throws Exception {
        // check ipv4 data length
        if (req.ipv4Data.size() > 1) {
            throw new Exception("we only support at most one ipv4 cidr in one network");
        }
        if (req.ipv6Data.size() > 1) {
            throw new Exception("we only support at most one ipv6 cidr in one network");
        }
        if (req.ipv4Data.isEmpty()) {
            throw new Exception("no ipv4 network info provided");
        }
        // validate
        for (var ipv4Data : req.ipv4Data) {
            if (ipv4Data.auxAddresses != null && !ipv4Data.auxAddresses.isEmpty()) {
                throw new Exception("auxAddresses are not supported");
            }
            Network net;
            try {
                net = new Network(ipv4Data.pool);
            } catch (IllegalArgumentException e) {
                throw new Exception("ipv4 network is not a valid cidr " + ipv4Data.pool);
            }
            if (!(net.getIp() instanceof IPv4)) {
                throw new Exception("address " + ipv4Data.pool + " is not ipv4 cidr");
            }
            var gatewayStr = ipv4Data.gateway;
            if (gatewayStr.contains("/")) {
                int mask;
                try {
                    mask = Integer.parseInt(gatewayStr.substring(gatewayStr.indexOf("/") + 1));
                } catch (NumberFormatException e) {
                    throw new Exception("invalid format for ipv4 gateway " + gatewayStr);
                }
                if (mask != net.getMask()) {
                    throw new Exception("the gateway mask " + mask + " must be the same as the network " + net.getMask());
                }
            }
            gatewayStr = gatewayStr.substring(0, gatewayStr.indexOf("/"));
            IP gateway;
            try {
                gateway = IP.from(gatewayStr);
            } catch (IllegalArgumentException e) {
                throw new Exception("ipv4 gateway is not a valid ip address " + ipv4Data.gateway);
            }
            if (!net.contains(gateway)) {
                throw new Exception("the cidr " + ipv4Data.pool + " does not contain the gateway " + ipv4Data.gateway);
            }
            ipv4Data.__transformedGateway = gateway.formatToIPString();
        }
        for (var ipv6Data : req.ipv6Data) {
            if (ipv6Data.auxAddresses != null && ipv6Data.auxAddresses.isEmpty()) {
                throw new Exception("auxAddresses are not supported");
            }
            Network net;
            try {
                net = new Network(ipv6Data.pool);
            } catch (IllegalArgumentException e) {
                throw new Exception("ipv6 network is not a valid cidr " + ipv6Data.pool);
            }
            if (!(net.getIp() instanceof IPv6)) {
                throw new Exception("address " + ipv6Data.pool + " is not ipv6 cidr");
            }
            var gatewayStr = ipv6Data.gateway;
            if (gatewayStr.contains("/")) {
                int mask;
                try {
                    mask = Integer.parseInt(gatewayStr.substring(gatewayStr.indexOf("/") + 1));
                } catch (NumberFormatException e) {
                    throw new Exception("invalid format for ipv6 gateway " + gatewayStr);
                }
                if (mask != net.getMask()) {
                    throw new Exception("the gateway mask " + mask + " must be the same as the network " + net.getMask());
                }
            }
            gatewayStr = gatewayStr.substring(0, gatewayStr.indexOf("/"));
            IP gateway;
            try {
                gateway = IP.from(gatewayStr);
            } catch (IllegalArgumentException e) {
                throw new Exception("ipv6 gateway is not a valid ip address " + ipv6Data.gateway);
            }
            if (!net.contains(gateway)) {
                throw new Exception("the cidr " + ipv6Data.pool + " does not contain the gateway " + ipv6Data.gateway);
            }
            ipv6Data.__transformedGateway = gateway.formatToIPString();
        }

        // handle
        var sw = ensureSwitch();
        IntMap<Table> tables = sw.getTables();
        int n = 0;
        for (int i : tables.keySet()) {
            if (n < i) {
                n = i;
            }
        }
        n += 1; // greater than the biggest recorded vni

        Network v4net;
        {
            v4net = new Network(req.ipv4Data.get(0).pool);
        }
        Network v6net = null;
        if (!req.ipv6Data.isEmpty()) {
            v6net = new Network(req.ipv6Data.get(0).pool);
        }
        sw.addTable(n, v4net, v6net, new Annotations(Collections.singletonMap(TABLE_NETWORK_ID_ANNOTATION, req.networkId)));
        Logger.alert("table added: vni=" + n + ", v4=" + v4net + ", v6=" + v6net + ", docker:networkId=" + req.networkId);
        Table tbl = sw.getTable(n);
        if (!req.networkId.equals(tbl.getAnnotations().other.get(TABLE_NETWORK_ID_ANNOTATION))) {
            Logger.shouldNotHappen("adding table failed, maybe concurrent modification");
            try {
                sw.delTable(n);
            } catch (Exception e2) {
                Logger.error(LogType.SYS_ERROR, "rollback table " + n + " failed", e2);
            }
            throw new Exception("unexpected state");
        }

        // add entry veth
        try {
            var umem = ensureUMem();
            createNetworkEntryVeth(sw, umem, tbl);
        } catch (Exception e) {
            Logger.error(LogType.SYS_ERROR, "creating network entry veth for table " + n + " failed", e);
            try {
                sw.delTable(n);
            } catch (Exception e2) {
                Logger.error(LogType.SYS_ERROR, "rollback table " + n + " failed", e2);
            }
            throw e;
        }

        // add ipv4 gateway ip
        {
            var gateway = IP.from(req.ipv4Data.get(0).__transformedGateway);
            var mac = GATEWAY_MAC_ADDRESS;
            tbl.addIp(gateway, mac, new Annotations(Collections.singletonMap(GATEWAY_IP_ANNOTATION, GATEWAY_IPv4_FLAG_VALUE)));
            Logger.alert("ip added: vni=" + n + ", ip=" + gateway + ", mac=" + mac);
        }

        if (!req.ipv6Data.isEmpty()) {
            // add ipv6 gateway ip
            var gateway = IP.from(req.ipv6Data.get(0).__transformedGateway);
            var mac = GATEWAY_MAC_ADDRESS;
            tbl.addIp(gateway, mac, new Annotations(Collections.singletonMap(GATEWAY_IP_ANNOTATION, GATEWAY_IPv6_FLAG_VALUE)));
            Logger.alert("ip added: vni=" + n + ", ip=" + gateway + ", mac=" + mac);
        }
    }

    private Switch ensureSwitch() throws Exception {
        Switch sw;
        try {
            sw = Application.get().switchHolder.get(SWITCH_NAME);
        } catch (NotFoundException ignore) {
            // need to create one
            EventLoopGroup elg;
            try {
                elg = Application.get().eventLoopGroupHolder.get(Application.DEFAULT_WORKER_EVENT_LOOP_GROUP_NAME);
            } catch (NotFoundException x) {
                Logger.shouldNotHappen(Application.DEFAULT_WORKER_EVENT_LOOP_GROUP_NAME + " not exists");
                throw new RuntimeException("should not happen: no event loop to handle the request");
            }
            sw = Application.get().switchHolder.add(
                SWITCH_NAME,
                new IPPort("0.0.0.0", 4789),
                elg,
                SwitchHandle.MAC_TABLE_TIMEOUT,
                SwitchHandle.ARP_TABLE_TIMEOUT,
                SecurityGroup.allowAll(),
                1500,
                true);
            Logger.alert("switch " + SWITCH_NAME + " created");
        }
        return sw;
    }

    private UMem ensureUMem() throws Exception {
        var sw = ensureSwitch();
        var umemOpt = sw.getUMems().stream().filter(n -> n.alias.equals(UMEM_NAME)).findAny();
        UMem umem;
        if (umemOpt.isEmpty()) {
            // need to add umem
            umem = sw.addUMem(UMEM_NAME, 4096, 32, 32, 2048);
        } else {
            return umemOpt.get();
        }

        try {
            createNetworkEntryVeth(sw, umem, null);
        } catch (Exception e) {
            try {
                sw.delUMem(UMEM_NAME);
            } catch (Exception e2) {
                Logger.error(LogType.SYS_ERROR, "rollback umem failed", e2);
            }
            throw e;
        }

        return umem;
    }

    private void createNetworkEntryVeth(Switch sw, UMem umem, Table tbl) throws Exception {
        int index = 0;
        if (tbl != null) {
            index = tbl.vni;
        }

        String hostNic = NETWORK_ENTRY_VETH_PREFIX + index;
        String swNic = NETWORK_ENTRY_VETH_PREFIX + index + NETWORK_ENTRY_VETH_PEER_SUFFIX;

        // veth for the host to access docker must be created
        createVethPair(hostNic, swNic, null);

        // add xdp for the veth
        try {
            createXDPIface(sw, umem, tbl, swNic);
        } catch (Exception e) {
            try {
                deleteNic(sw, swNic);
            } catch (Exception e2) {
                Logger.error(LogType.SYS_ERROR, "rollback nic " + hostNic + " failed", e2);
            }
            throw e;
        }
    }

    private XDPIface createXDPIface(Switch sw, UMem umem, Table tbl, String nicname) throws Exception {
        var cmd = Command.parseStrCmd("add bpf-object " + nicname + " mode SKB force");
        var bpfobj = BPFObjectHandle.add(cmd);
        try {
            return sw.addXDP(nicname, bpfobj.getMap("xsks_map"), umem, 0,
                32, 32, BPFMode.SKB, false, 0,
                tbl != null ? tbl.vni : NETWORK_ENTRY_VNI,
                BPFMapKeySelectors.useQueueId.keySelector.get());
        } catch (Exception e) {
            try {
                Application.get().bpfObjectHolder.removeAndRelease(bpfobj.nic);
            } catch (Exception e2) {
                Logger.error(LogType.SYS_ERROR, "rollback bpf-object " + bpfobj.nic + " failed", e2);
            }
            throw e;
        }
    }

    private Table findNetwork(Switch sw, String networkId) throws Exception {
        var tables = sw.getTables();
        for (var tbl : tables.values()) {
            var netId = tbl.getAnnotations().other.get(TABLE_NETWORK_ID_ANNOTATION);
            if (netId != null && netId.equals(networkId)) {
                return tbl;
            }
        }
        throw new Exception("network " + networkId + " not found");
    }

    @Override
    public synchronized void deleteNetwork(String networkId) throws Exception {
        var sw = ensureSwitch();
        var tbl = findNetwork(sw, networkId);

        deleteNetworkEntryVeth(sw, tbl);

        sw.delTable(tbl.vni);
        Logger.alert("table deleted: vni=" + tbl.vni + ", docker:networkId=" + networkId);
    }

    private void deleteNetworkEntryVeth(Switch sw, Table tbl) throws Exception {
        String swNic = NETWORK_ENTRY_VETH_PREFIX + tbl.vni + NETWORK_ENTRY_VETH_PEER_SUFFIX;
        deleteNic(sw, swNic);
    }

    @Override
    public synchronized CreateEndpointResponse createEndpoint(CreateEndpointRequest req) throws Exception {
        if (req.netInterface == null) {
            throw new Exception("we do not support auto ip allocation for now");
        }
        if (req.netInterface.address == null || req.netInterface.address.isEmpty()) {
            throw new Exception("ipv4 must be provided");
        }

        var sw = ensureSwitch();
        var tbl = findNetwork(sw, req.networkId);
        if (req.netInterface.addressIPV6 != null && !req.netInterface.addressIPV6.isEmpty()) {
            if (tbl.v6network == null) {
                throw new Exception("network " + req.networkId + " does not support ipv6");
            }
        }

        Map<String, String> anno = new HashMap<>();
        anno.put(VETH_ENDPOINT_ID_ANNOTATION, req.endpointId);
        anno.put(VETH_ENDPOINT_IPv4_ANNOTATION, req.netInterface.address);
        if (req.netInterface.addressIPV6 != null && !req.netInterface.addressIPV6.isEmpty()) {
            anno.put(VETH_ENDPOINT_IPv6_ANNOTATION, req.netInterface.addressIPV6);
        }
        if (req.netInterface.macAddress != null && !req.netInterface.macAddress.isEmpty()) {
            anno.put(VETH_ENDPOINT_MAC_ANNOTATION, req.netInterface.macAddress);
        }

        String swNic = "veth" + req.endpointId.substring(0, 8);
        String containerNic = swNic + CONTAINER_VETH_SUFFIX;
        createVethPair(swNic, containerNic, req.netInterface.macAddress);

        try {
            var umem = ensureUMem();

            XDPIface xdpIface = createXDPIface(sw, umem, tbl, swNic);
            xdpIface.setAnnotations(new Annotations(anno));
            Logger.alert("xdp added: " + xdpIface.nic + ", vni=" + tbl.vni
                + ", endpointId=" + req.endpointId
                + ", ipv4=" + anno.get(VETH_ENDPOINT_IPv4_ANNOTATION)
                + ", ipv6=" + anno.get(VETH_ENDPOINT_IPv6_ANNOTATION)
                + ", mac=" + anno.get(VETH_ENDPOINT_MAC_ANNOTATION)
                + ", netId=" + req.networkId
            );
        } catch (Exception e) {
            try {
                deleteNic(sw, swNic);
            } catch (Exception e2) {
                Logger.error(LogType.SOCKET_ERROR, "failed to rollback nic " + swNic, e2);
            }

            throw e;
        }
        var resp = new CreateEndpointResponse();
        resp.netInterface = null;
        return resp;
    }

    private void createVethPair(String hostVeth, String containerVeth, String mac) throws Exception {
        String scriptContent = "" +
            "ip link add " + hostVeth + " type veth peer name " + containerVeth + "\n";
        if (mac != null && !mac.isBlank()) {
            scriptContent += "" +
                "ip link set " + containerVeth + " address " + mac + "\n";
        }
        scriptContent += "" +
            "ip link set " + hostVeth + " up\n" +
            "ip link set " + containerVeth + " up\n";
        Utils.execute(scriptContent);
    }

    private XDPIface findEndpoint(Switch sw, String endpointId) throws Exception {
        var ifaces = sw.getIfaces();
        for (var iface : ifaces) {
            if (iface instanceof XDPIface) {
                var xdp = (XDPIface) iface;
                var epId = xdp.getAnnotations().other.get(VETH_ENDPOINT_ID_ANNOTATION);
                if (epId != null && epId.equals(endpointId)) {
                    return xdp;
                }
            }
        }
        throw new Exception("endpoint " + endpointId + " not found");
    }

    @Override
    public synchronized void deleteEndpoint(String networkId, String endpointId) throws Exception {
        var sw = ensureSwitch();
        findNetwork(sw, networkId);
        var xdp = findEndpoint(sw, endpointId);

        // delete nic
        try {
            deleteNic(sw, xdp.nic);
        } catch (Exception e) {
            Logger.warn(LogType.ALERT, "failed to delete nic " + xdp.nic, e);
        }
        Logger.alert("xdp deleted: " + xdp.nic + ", endpointId=" + endpointId);
    }

    private void deleteNic(Switch sw, String swNic) throws Exception {
        try {
            Application.get().bpfObjectHolder.removeAndRelease(swNic);
        } catch (NotFoundException ignore) {
        }
        try {
            sw.delXDP(swNic);
        } catch (NotFoundException ignore) {
        }

        Utils.execute("" +
            "#!/bin/bash\n" +
            "x=`ip link show dev " + swNic + " | wc -l`\n" +
            "if [ \"$x\" == \"0\" ]\n" +
            "then\n" +
            "    exit 0\n" +
            "fi\n" +
            "ip link del " + swNic + "\n");
    }

    @Override
    public synchronized JoinResponse join(String networkId, String endpointId, String sandboxKey) throws Exception {
        var sw = ensureSwitch();
        var tbl = findNetwork(sw, networkId);
        var xdp = findEndpoint(sw, endpointId);
        var ifName = xdp.nic + CONTAINER_VETH_SUFFIX;
        var ipv6 = xdp.getAnnotations().other.get(VETH_ENDPOINT_IPv6_ANNOTATION);

        if (tbl.v6network == null && ipv6 != null) {
            throw new Exception("internal error: should not reach here: " +
                "network " + networkId + " does not support ipv6 but the endpoint is assigned with ipv6 addr");
        }

        String gatewayV4 = null;
        String gatewayV6 = null;
        for (var info : tbl.ips.entries()) {
            var value = info.annotations.other.get(GATEWAY_IP_ANNOTATION);
            if (value != null) {
                if (value.equals(GATEWAY_IPv4_FLAG_VALUE)) {
                    gatewayV4 = info.ip.formatToIPString();
                } else if (value.equals(GATEWAY_IPv6_FLAG_VALUE)) {
                    gatewayV6 = info.ip.formatToIPString();
                }
            }
        }
        if (gatewayV4 == null) {
            throw new Exception("ipv4 gateway not found in network " + networkId);
        }
        if (gatewayV6 == null && ipv6 != null) {
            throw new Exception("ipv6 gateway not found in network " + networkId);
        }
        if (gatewayV6 != null && gatewayV6.startsWith("[") && gatewayV6.endsWith("]")) {
            gatewayV6 = gatewayV6.substring(1, gatewayV6.length() - 1);
        }

        var resp = new JoinResponse();
        resp.interfaceName.srcName = ifName;
        resp.interfaceName.dstPrefix = "eth";
        resp.gateway = gatewayV4;
        if (gatewayV6 != null && ipv6 != null) {
            resp.gatewayIPv6 = gatewayV6;
        }
        return resp;
    }

    @SuppressWarnings("RedundantThrows")
    @Override
    public synchronized void leave(String networkId, String endpointId) throws Exception {
        // do nothing
    }
}
