package vproxy.vpacket;

import vproxy.base.util.ByteArray;
import vproxy.base.util.Utils;

import java.util.Objects;

public class PacketBytes extends AbstractPacket {
    private ByteArray bytes;

    @Override
    public String from(PacketDataBuffer raw) {
        this.bytes = raw.pktBuf;
        return null;
    }

    @Override
    protected ByteArray buildPacket() {
        return bytes;
    }

    @Override
    protected void __updateChecksum() {
        // do nothing
    }

    @Override
    public PacketBytes copy() {
        var ret = new PacketBytes();
        ret.bytes = bytes;
        return ret;
    }

    @Override
    public String description() {
        return "packet,len=" + bytes.length();
    }

    @Override
    public String toString() {
        return "PacketBytes(" + Utils.runAvoidNull(() -> bytes.toHexString(), "null") + ')';
    }

    public ByteArray getBytes() {
        return bytes;
    }

    public void setBytes(ByteArray bytes) {
        clearRawPacket();
        this.bytes = bytes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PacketBytes that = (PacketBytes) o;
        return Objects.equals(bytes, that.bytes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(bytes);
    }
}
