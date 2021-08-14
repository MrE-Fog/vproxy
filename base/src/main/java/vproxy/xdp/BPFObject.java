package vproxy.xdp;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class BPFObject {
    public final String nic;
    public final String filename;
    public final boolean isAutogenEbpf;
    public final String prog;
    public final BPFMode mode;

    public final long bpfobj;
    private final Map<String, BPFMap> maps = new ConcurrentHashMap<>();

    BPFObject(String nic, String filename, boolean isAutogenEbpf, String prog, BPFMode mode, long bpfobj) {
        this.nic = nic;
        this.filename = filename;
        this.isAutogenEbpf = isAutogenEbpf;
        this.prog = prog;
        this.mode = mode;

        this.bpfobj = bpfobj;
    }

    public static BPFObject loadAndAttachToNic(String filepath, String programName, String nicName,
                                               BPFMode mode, boolean forceAttach) throws IOException {
        return loadAndAttachToNic(filepath, programName, false, nicName, mode, forceAttach);
    }

    public static BPFObject loadAndAttachToNic(String filepath, String programName, boolean isAutogenEbpf, String nicName,
                                               BPFMode mode, boolean forceAttach) throws IOException {
        long bpfobj = NativeXDP.get().loadAndAttachBPFProgramToNic(filepath, programName, nicName, mode.mode, forceAttach);
        return new BPFObject(nicName, filepath, isAutogenEbpf, programName, mode, bpfobj);
    }

    public BPFMap getMap(String name) throws IOException {
        if (maps.containsKey(name)) {
            return maps.get(name);
        }
        synchronized (maps) {
            if (maps.containsKey(name)) {
                return maps.get(name);
            }
            long map = NativeXDP.get().findMapByNameInBPF(bpfobj, name);
            var m = new BPFMap(name, map, this);
            maps.put(name, m);
            return m;
        }
    }

    public void release() {
        NativeXDP.get().releaseBPFObject(bpfobj);
    }

    public String toString() {
        return nic + " ->"
            + " path " + filename
            + " prog " + prog
            + " mode " + mode.name();
    }
}
