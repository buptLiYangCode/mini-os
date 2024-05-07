package bupt.os.component.process.scheduler;

import bupt.os.component.cpu.CPUSimulator;
import bupt.os.component.cpu.ProcessExecutionTask;
import bupt.os.component.interrupt.InterruptRequestLine;
import bupt.os.component.memory.ly.PCB;
import bupt.os.component.memory.ly.ProtectedMemory;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;
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
    private static final InterruptRequestLine irl = InterruptRequestLine.getInstance();

    private static final Queue<PCB> readyQueue = protectedMemory.getReadyQueue();

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
            log.info("--进程切换为---" + nextProcessPcb.getProcessName());
        } else {
            // 暂无可执行进程，需要等待中断出现
            log.info("暂无可执行线程，循环等待");
            cpuSimulatorExecutor.submit(() -> {
                while (true) {
                    if (irl.peek() != null) {
                        log.info("处理IO中断");
                        handleHardInterruptIo();
                    }
                    if (!readyQueue.isEmpty()) {
                        cpuSimulatorExecutor.submit(new ProcessExecutionTask(Objects.requireNonNull(readyQueue.poll())));
                        break;
                    }
                }
            });
        }
    }
}
