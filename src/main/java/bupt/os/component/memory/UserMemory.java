package bupt.os.component.memory;

import lombok.Data;

/**
 * 用户内存
 */
@Data
public class UserMemory {
    private static volatile UserMemory instance;

    // 内存大小，例如16KB，保护空间4KB，用户空间12KB
    public static final int MEMORY_SIZE = 12 * 1024;
    // 页数
    public static final int TOTAL_PAGES = 4;
    // 页大小，例如4KB
    public static final int PAGE_SIZE = 4 * 1024;

    // 用户空间可访问页数组
    private Page[] pages;

    // 私有构造函数，防止外部实例化
    private UserMemory() {
        // 初始化页数组
        pages = new Page[TOTAL_PAGES];
        for (int i = 0; i < TOTAL_PAGES; i++) {
            pages[i] = new Page();
            pages[i].setData(new char[PAGE_SIZE]);
        }
    }

    // 获取单例实例的静态方法
    public static UserMemory getInstance() {
        if (instance == null) {
            synchronized (UserMemory.class) {
                if (instance == null) {
                    instance = new UserMemory();
                }
            }
        }
        return instance;
    }
}
