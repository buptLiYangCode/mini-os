package bupt.os.component.memory.protected_;

import bupt.os.component.filesystem.FileNode;
import bupt.os.component.interrupt.InterruptRequestLine;
import lombok.Data;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentLinkedQueue;


/**
 * 保护内存
 */
@Data
public class ProtectedMemory {
    private static volatile ProtectedMemory instance;

    // 进程控制块表
    private HashMap<Integer, PCB> pcbTable; // 假设每个PCB有一个唯一的PID
    // 中断向量表

    // 设备控制表
    private LinkedList<DeviceInfo> deviceInfoTable;
    // 文件信息表
    private HashMap<FileNode, FileInfoo> fileInfoTable;
    // irl表
    private HashMap<Long, InterruptRequestLine> irlTable;

    // 运行队列
    private ConcurrentLinkedQueue<PCB> runningQueue;
    // 就绪队列
    private ConcurrentLinkedQueue<PCB> readyQueue;
    // 等待队列
    private ConcurrentLinkedQueue<PCB> waitingQueue;

    // 私有构造函数，防止外部实例化
    private ProtectedMemory() {
        // 初始化数据结构
        pcbTable = new HashMap<>();
        deviceInfoTable = new LinkedList<>();
        fileInfoTable = new HashMap<>();
        this.irlTable = new HashMap<>();

        runningQueue = new ConcurrentLinkedQueue<>();
        readyQueue = new ConcurrentLinkedQueue<>();
        waitingQueue = new ConcurrentLinkedQueue<>();
    }

    // 获取单例实例的静态方法
    public static ProtectedMemory getInstance() {
        if (instance == null) {
            synchronized (ProtectedMemory.class) {
                if (instance == null) {
                    instance = new ProtectedMemory();
                }
            }
        }
        return instance;
    }
}