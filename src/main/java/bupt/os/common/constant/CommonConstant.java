package bupt.os.common.constant;

import static bupt.os.component.disk.Disk.BLOCK_SIZE;
import static bupt.os.component.memory.UserMemory.PAGE_SIZE;

public class CommonConstant {
    public static final int BLOCKS_PER_PAGE = PAGE_SIZE / BLOCK_SIZE;
}
