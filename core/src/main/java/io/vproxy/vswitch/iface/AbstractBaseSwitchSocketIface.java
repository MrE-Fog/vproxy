package io.vproxy.vswitch.iface;

import io.vproxy.base.util.Logger;
import io.vproxy.base.util.Utils;
import io.vproxy.vfd.DatagramFD;
import io.vproxy.vfd.IPPort;
import io.vproxy.vswitch.PacketBuffer;
import io.vproxy.vswitch.util.SwitchUtils;

import java.io.IOException;
import java.nio.ByteBuffer;

public abstract class AbstractBaseSwitchSocketIface extends Iface {
    protected DatagramFD sock;
    protected boolean sockConnected = false; // default sock is the server sock in switch, so it's not connected
    public final IPPort remote;
    protected final ByteBuffer sndBuf = Utils.allocateByteBuffer(2048);

    protected AbstractBaseSwitchSocketIface(IPPort remote) {
        this.remote = remote;
    }

    @Override
    public void init(IfaceInitParams params) throws Exception {
        super.init(params);
        this.sock = params.sock;
    }

    protected void setSock(DatagramFD sock, boolean connected) {
        this.sock = sock;
        this.sockConnected = connected;
    }

    @SuppressWarnings("DuplicatedCode")
    @Override
    public void sendPacket(PacketBuffer pkb) {
        assert Logger.lowLevelDebug(this + ".sendPacket(" + pkb + ")");

        var vxlan = SwitchUtils.getOrMakeVXLanPacket(pkb);

        sndBuf.limit(sndBuf.capacity()).position(0);

        byte[] bytes = vxlan.getRawPacket(0).toJavaArray();
        sndBuf.put(bytes);
        sndBuf.flip();

        manipulate();

        statistics.incrTxPkts();
        statistics.incrTxBytes(sndBuf.limit() - sndBuf.position());

        try {
            if (sockConnected) {
                sock.write(sndBuf);
            } else {
                sock.send(sndBuf, remote);
            }
        } catch (IOException e) {
            assert Logger.lowLevelDebug("sending packet to " + this + " failed: " + e);
            statistics.incrTxErr();
        }
    }

    protected void manipulate() {
        // do nothing
    }
}
