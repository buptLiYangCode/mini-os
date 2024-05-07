package bupt.os.component.memory.ly;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 进程页加载进内存的信息
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PageSwapInfo {
    // 当前页属于的进程号
    private int pid;
    // 虚拟页号
    private int vpn;
    // 页加载进磁盘时间
    private long loadTime;
    // 页最近访问时间
    private long lastAccessTime;
}
