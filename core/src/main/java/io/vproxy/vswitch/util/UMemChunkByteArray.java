package io.vproxy.vswitch.util;

import io.vproxy.base.util.ByteArray;
import io.vproxy.base.util.Consts;
import io.vproxy.base.util.bytearray.AbstractByteArray;
import io.vproxy.base.util.unsafe.SunUnsafe;
import io.vproxy.xdp.Chunk;
import io.vproxy.xdp.XDPSocket;

import java.nio.ByteBuffer;

public class UMemChunkByteArray extends AbstractByteArray implements ByteArray {
    public final ByteBuffer buffer;
    public final int off;
    public final int len;

    public final XDPSocket xsk;
    public final Chunk chunk;

    public UMemChunkByteArray(XDPSocket xsk, Chunk chunk) {
        ByteBuffer buffer = xsk.umem.getBuffer();
        int off = chunk.addr();
        int len = chunk.endaddr() - chunk.addr();

        this.buffer = buffer;
        this.off = off + Consts.XDP_HEADROOM_DRIVER_RESERVED;
        this.len = len - Consts.XDP_HEADROOM_DRIVER_RESERVED;

        if (buffer.capacity() < off + len) {
            throw new IllegalArgumentException("buffer.cap=" + buffer.capacity() + ", off=" + off + ", len=" + len);
        }
        if (!buffer.isDirect()) {
            throw new IllegalArgumentException("buffer is not direct");
        }

        this.xsk = xsk;
        this.chunk = chunk;
    }

    public void releaseRef() {
        chunk.releaseRef(xsk.umem);
        if (chunk.ref() == 0) {
            chunk.returnToPool();
        }
    }

    public void reference() {
        chunk.reference();
    }

    @Override
    public byte get(int idx) {
        if (len <= idx) {
            throw new IndexOutOfBoundsException("len=" + len + ", idx=" + idx);
        }
        int n = off + idx;
        return SunUnsafe.getByte(xsk.umem.getBufferAddress() + n);
    }

    @Override
    public ByteArray set(int idx, byte value) {
        if (len <= idx) {
            throw new IndexOutOfBoundsException("len=" + len + ", idx=" + idx);
        }
        int n = off + idx;
        SunUnsafe.putByte(xsk.umem.getBufferAddress() + n, value);
        return this;
    }

    @Override
    public int length() {
        return len;
    }

    @Override
    public void byteBufferPut(ByteBuffer dst, int off, int len) {
        if (this.off + off + len > buffer.capacity()) {
            throw new IndexOutOfBoundsException("buffer.cap=" + buffer.capacity() + ", this.off=" + this.off + ", off=" + off + ", len=" + len);
        }
        if (dst == buffer) {
            if (dst.position() != this.off + off) {
                SunUnsafe.copyMemory(xsk.umem.getBufferAddress() + dst.position(), xsk.umem.getBufferAddress() + this.off + off, len);
            } // else same memory region, nothing to be copied
            dst.position(dst.position() + len);
            return;
        }

        buffer.limit(this.off + off + len).position(this.off + off);
        dst.put(buffer);
    }

    @Override
    public void byteBufferGet(ByteBuffer src, int off, int len) {
        if (this.off + off + len > buffer.capacity()) {
            throw new IndexOutOfBoundsException("buffer.cap=" + buffer.capacity() + ", this.off=" + this.off + ", off=" + off + ", len=" + len);
        }
        if (src.limit() - src.position() < len) {
            throw new IndexOutOfBoundsException("src.lim=" + src.limit() + ", src.pos=" + src.position() + ", len=" + len);
        }
        if (src == buffer) {
            if (src.position() != this.off + off) {
                SunUnsafe.copyMemory(xsk.umem.getBufferAddress() + this.off + off, xsk.umem.getBufferAddress() + src.position(), len);
            } // else same memory region, nothing to be copied
            src.position(src.position() + len);
            return;
        }

        int srcLim = src.limit();

        buffer.limit(this.off + off + len).position(this.off + off);
        src.limit(src.position() + len);
        buffer.put(src);

        src.limit(srcLim);
    }

    @Override
    protected void doToNewJavaArray(byte[] dst, int dstOff, int srcOff, int srcLen) {
        buffer.limit(buffer.capacity()).position(off + srcOff);
        buffer.get(dst, dstOff, srcLen);
    }
}
