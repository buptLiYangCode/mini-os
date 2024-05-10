package bupt.os.tools;

public class CommonTool {

    // 根据processName 计算pid
    public static int getPid(String fileName) {
        int hashCode = fileName.hashCode();
        return Math.abs(hashCode) % 256;
    }
}
