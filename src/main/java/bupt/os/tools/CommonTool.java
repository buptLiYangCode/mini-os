package bupt.os.tools;

import static bupt.os.component.filesystem.filesystem_ly.Disk.TOTAL_BLOCKS;

public class CommonTool {

    // 根据processName 计算pid
    public static int getPid(String fileName) {
        int hashCode = fileName.hashCode();
        return Math.abs(hashCode) % TOTAL_BLOCKS;
    }
}
