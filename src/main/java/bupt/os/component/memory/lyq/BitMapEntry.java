package bupt.os.component.memory.lyq;

import lombok.Data;

@Data
public class BitMapEntry {
    private int pid;
    private int vpn;

    public BitMapEntry(int pid, int vpn) {
        this.pid = pid;
        this.vpn = vpn;
    }

}