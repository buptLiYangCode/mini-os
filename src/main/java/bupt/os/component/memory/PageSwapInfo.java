package bupt.os.component.memory;

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
    // 物理页号
    private int ppn;
    private boolean used;
    private long loadTime;
    private long lastAccessTime;
}
