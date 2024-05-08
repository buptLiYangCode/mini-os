package bupt.os.component.process.scheduler;

import bupt.os.component.cpu.CPUSimulator;
import bupt.os.component.cpu.ProcessExecutionTask;
import bupt.os.component.interrupt.InterruptRequestLine;
import bupt.os.component.memory.ly.PCB;
import bupt.os.component.memory.ly.ProtectedMemory;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Queue;
import java.util.concurrent.ExecutorService;

import static bupt.os.component.interrupt.InterruptHandler.handleHardInterruptIo;

/**
 * 调度器管理
 */
@Slf4j
public class ProcessScheduler {
    private static final ProtectedMemory protectedMemory = ProtectedMemory.getInstance();
    private static final CPUSimulator cpuSimulator = CPUSimulator.getInstance();

    private static final Queue<PCB> readyQueue = protectedMemory.getReadyQueue();
    private static final HashMap<Long, InterruptRequestLine> irlTable = protectedMemory.getIrlTable();

    /**
     * @return 返回下一个会执行的进程
     */
    public static PCB nextExecutableProcess() {
        Queue<PCB> readyQueue = protectedMemory.getReadyQueue();
        // TODO 进程调度算法
        return readyQueue.poll();
    }


    public static void executeNextProcess() {
        // 调度器将下一个待调度的进程放进线程池任务队列
        ExecutorService cpuSimulatorExecutor = cpuSimulator.getExecutor();
        PCB nextProcessPcb = nextExecutableProcess();
        if (nextProcessPcb != null) {
            ProcessExecutionTask nextExecuteProcess = new ProcessExecutionTask(nextProcessPcb);
            cpuSimulatorExecutor.submit(nextExecuteProcess);
        } else {
            // 暂无可执行进程，需要等待中断出现
            log.info("暂无可执行线程，CPU处于空闲，不断监听中断信号");
            cpuSimulatorExecutor.submit(() -> {
                try {
                    while (true) {
                        Thread.sleep(500);
                        log.info("cpu" + Thread.currentThread().getId() + "正在空转");
                        long threadId = Thread.currentThread().getId();
                        InterruptRequestLine irl = irlTable.get(threadId);
                        if (irl.peek() != null) {
                            handleHardInterruptIo();
                        }
                        try {
                            PCB pcb = readyQueue.poll();
                            if (pcb != null) {
                                cpuSimulatorExecutor.submit(new ProcessExecutionTask(pcb));
                                break;
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
    }
}
