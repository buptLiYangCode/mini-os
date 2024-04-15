package bupt.os.component.memory;


public class Memory {
    // 内存大小，例如16KB
    public static final int MEMORY_SIZE = 16 * 1024;
    // 页数
    public static final int TOTAL_PAGES = 4;
    // 页大小，例如4KB
    public static final int PAGE_SIZE = 4 * 1024;
    // 页数组
    public static Page[] pages;
}
