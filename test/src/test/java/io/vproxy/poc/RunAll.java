package io.vproxy.poc;

import io.vproxy.base.dns.Resolver;

public class RunAll {
    public static void main(String[] args) throws Exception {
        System.out.println("==============================================");
        System.out.println("     selector event loop echo server");
        System.out.println("==============================================");
        SelectorEventLoopEchoServer.main(new String[0]);

        System.out.println("==============================================");
        System.out.println("         net event loop echo server");
        System.out.println("==============================================");
        NetEventLoopEchoServer.main(new String[0]);

        System.out.println("==============================================");
        System.out.println("   net event loop split buffers echo server");
        System.out.println("==============================================");
        NetEventLoopSplitBuffersEchoServer.main(new String[0]);

        System.out.println("==============================================");
        System.out.println("            proxy an echo server");
        System.out.println("==============================================");
        ProxyEchoServer.main(new String[0]);

        System.out.println("==============================================");
        System.out.println("             health check client");
        System.out.println("==============================================");
        HealthCheckClientExample.main(new String[0]);

        System.out.println("==============================================");
        System.out.println("                server group");
        System.out.println("==============================================");
        ServerGroupExample.main(new String[0]);

        System.out.println("==============================================");
        System.out.println("             lb for echo servers");
        System.out.println("==============================================");
        LBForEchoServers.main(new String[0]);

        System.out.println("==============================================");
        System.out.println("                parse commands");
        System.out.println("==============================================");
        CommandParser.main(new String[0]);

        System.out.println("==============================================");
        System.out.println("                 parse resp");
        System.out.println("==============================================");
        TestRESPParser.main(new String[0]);

        System.out.println("==============================================");
        System.out.println("            echo protocol server");
        System.out.println("==============================================");
        EchoProtocolServer.main(new String[0]);

        System.out.println("==============================================");
        System.out.println("            resp ping pong server");
        System.out.println("==============================================");
        RESPPingPongServer.main(new String[0]);

        System.out.println("==============================================");
        System.out.println("               resp app server");
        System.out.println("==============================================");
        RESPApplicationServer.main(new String[0]);

        System.out.println("==============================================");
        System.out.println("              forbid lb server");
        System.out.println("==============================================");
        ForbidLBForEchoServers.main(new String[0]);

        System.out.println("==============================================");
        System.out.println("                http2 proxy");
        System.out.println("==============================================");
        Http2Proxy.main(new String[0]);

        System.out.println("==============================================");
        System.out.println("             grpc over h2 proxy");
        System.out.println("==============================================");
        GrpcOverH2Proxy.main(new String[0]);

        System.out.println("==============================================");
        System.out.println("         udp echo server and client");
        System.out.println("==============================================");
        UDPNetEventLoopEchoServer.main(new String[0]);

        System.out.println("==============================================");
        System.out.println("         kcp echo server and client");
        System.out.println("==============================================");
        KCPNetEventLoopEchoServer.main(new String[0]);

        Resolver.stopDefault();
    }
}
