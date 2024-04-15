package bupt.os.component.memory;

/**
 * 页表一个表项，每个进程的页表都是存储在内存中
 */
public class PageTableEntry {
    // 页是否在物理内存中
    private boolean isValid;
    // 页是否被修改过
    private boolean isDirty;
    // 页是否被访问过，用于某些页面替换算法
    private boolean isReferenced;
    // 对应的物理页号，如果在内存中
    private int pageNumber;
}
