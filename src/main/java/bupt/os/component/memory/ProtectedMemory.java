package bupt.os.component.memory;

import lombok.Data;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

import static bupt.os.component.memory.UserMemory.TOTAL_PAGES;

/**
 * 保护内存
 */
@Data
public class ProtectedMemory {
    private static volatile ProtectedMemory instance;

    // 进程页表，存储进程，虚拟页号-物理页号的映射关系
    private HashMap<Integer, LinkedList<PageInfo>> processPageTable;
    // 进程控制块表
    private HashMap<Integer, PCB> pcbTable; // 假设每个PCB有一个唯一的PID
    // TODO 中断向量表

    // 页信息表，页最近访问时间，页进入内存的时间
    private LinkedList<PageSwapInfo> allPageInfo;
    // 设备控制表
    private LinkedList<DeviceInfo> deviceInfoTable;

    // 运行队列
    private Queue<PCB> runningQueue;
    // 就绪队列
    private Queue<PCB> readyQueue;
    // 等待队列
    private Queue<PCB> waitingQueue;

    // 私有构造函数，防止外部实例化
    private ProtectedMemory() {
        // 初始化数据结构
        processPageTable = new HashMap<>();
        pcbTable = new HashMap<>();
        allPageInfo = new LinkedList<>();
        for (int i = 0; i < TOTAL_PAGES; i++) {
            PageSwapInfo pageSwapInfo = new PageSwapInfo(-1, -1, 0L, 0L);
            allPageInfo.add(pageSwapInfo);
        }
        deviceInfoTable = new LinkedList<>();

        runningQueue = new LinkedList<>();
        readyQueue = new LinkedList<>();
        waitingQueue = new LinkedList<>();
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