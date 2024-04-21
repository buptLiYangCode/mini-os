package bupt.os.component.process;

import bupt.os.component.memory.PCB;
import bupt.os.component.memory.ProtectedMemory;

import java.util.Queue;

/**
 * 调度器管理
 */
public class ProcessScheduler {
    private static final ProtectedMemory protectedMemory = ProtectedMemory.getInstance();
    /**
     *
     * @return 返回下一个会执行的进程
     */
    public static PCB nextExecuteProcess() {
        Queue<PCB> readyQueue = protectedMemory.getReadyQueue();
        // TODO 进程调度算法

        return readyQueue.poll();

    }
}
