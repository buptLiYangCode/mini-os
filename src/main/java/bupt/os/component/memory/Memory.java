package bupt.os.component.memory;

import lombok.Data;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 内存
 * 当页不足时，有对应的置换算法，所以不需要位图
 */
@Component
@Data
public class Memory {
    // 内存大小，例如16KB
    public static final int MEMORY_SIZE = 16 * 1024;
    // 页数
    public static final int TOTAL_PAGES = 4;
    // 页大小，例如4KB
    public static final int PAGE_SIZE = 4 * 1024;

    // 二维数组，行坐标表示物理页号，列（调入内存时间、最近访问时间、未来访问时间）
    private LocalDateTime[][] pageAccessTime;
    // 页数组
    private Page[] pages;
}
