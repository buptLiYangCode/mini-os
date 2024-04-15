package bupt.os.common.constant;

public class InstructionConstant {
    // C time （计算指令，使用CPU，时长time）
    public static final String C = "C";
    // K time （I/O指令，键盘输入，时长time）
    public static final String K = "K";
    // P time （I/O指令，打印机输出，时长time）
    public static final String P = "P";
    // R filename time （读文件，时长time）
    public static final String R = "R";
    // W filename time size （写文件，时长，文件大小size）
    public static final String W = "W";
    // M block 进程占用内存空间 （资源需求声明）
    public static final String M = "M";
    // Y number 进程的优先数 （调度参数声明，优先级）
    public static final String Y = "Y";
    // Q 结束运行 （程序结束）
    public static final String Q = "Q";
}
