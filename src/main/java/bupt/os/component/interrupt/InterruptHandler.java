package bupt.os.component.interrupt;

import bupt.os.component.memory.ly.PCB;
import bupt.os.component.memory.ly.ProtectedMemory;
import bupt.os.component.memory.lyq.MemoryManagementImpl;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Queue;

import static bupt.os.common.constant.ProcessStateConstant.READY;

@Slf4j
public class InterruptHandler {
    // 物理组件
    private static final ProtectedMemory protectedMemory = ProtectedMemory.getInstance();
    private static final MemoryManagementImpl mmu = new MemoryManagementImpl();

    // 内核空间中存储的表
    private static final HashMap<Integer, PCB> pcbTable = protectedMemory.getPcbTable();
    private static final Queue<PCB> runningQueue = protectedMemory.getRunningQueue();
    private static final Queue<PCB> readyQueue = protectedMemory.getReadyQueue();
    private static final Queue<PCB> waitingQueue = protectedMemory.getWaitingQueue();
    private static final HashMap<Long, InterruptRequestLine> irlTable = protectedMemory.getIrlTable();

    /**
     * 硬件中断处理程序，如果发生进程切换，需要更新PCB状态，以及几个队列的状态
     *
     * @return 是否切换进程，返回1，则当前运行进程时间片耗尽，出让CPU。返回0，继续执行
     */
    public static int handleHardInterrupt(PCB pcb) {
        int isSwitchProcess = 0;
        InterruptRequestLine irl = irlTable.get(Thread.currentThread().getId());
        String interruptRequest;
        while ((interruptRequest = irl.poll()) != null) {
            if (interruptRequest.equals("TIMER_INTERRUPT")) {
                // 时间片耗尽了，发生进程切换
                if (System.currentTimeMillis() - pcb.getStartTime() > pcb.getRemainingTime()) {
                    pcb.setState(READY);
                    pcb.setRemainingTime(-1);
                    pcb.setStartTime(-1);
                    // 放入就绪队列
                    readyQueue.add(pcb);
                    // 移出运行队列
                    runningQueue.remove(pcb);
                    log.info("进程" + pcb.getProcessName() + "时间片耗尽");
                    isSwitchProcess = 1;
                }
            } else {
                // IO操作完成中断，ir + 1
                PCB pcbInWaitingQueue = getPcbInWaitingQueue(interruptRequest);
                // 移出等待队列
                waitingQueue.remove(pcbInWaitingQueue);
                // 放入就绪队列
                readyQueue.add(pcbInWaitingQueue);
                log.info("进程" + pcbInWaitingQueue.getProcessName() + "IO操作完成");
            }
        }
        return isSwitchProcess;
    }

    /**
     * 专门处理IO操作完成中断
     */
    public static int handleHardInterruptIo() {
        // count 是此次处理IO中断的个数
        int count = 0;
        InterruptRequestLine irl = irlTable.get(Thread.currentThread().getId());
        while (irl.peek() != null) {
            String interruptRequest = irl.poll();
            if (!interruptRequest.equals("TIMER_INTERRUPT")) {
                // IO操作完成中断，ir + 1
                PCB pcbInWaitingQueue = getPcbInWaitingQueue(interruptRequest);
                // 移出等待队列
                waitingQueue.remove(pcbInWaitingQueue);
                // 放入就绪队列
                readyQueue.add(pcbInWaitingQueue);
                log.info("进程" + pcbInWaitingQueue.getProcessName() + "IO操作完成");
                count++;
            }
        }
        return count;
    }

    /**
     * 根据收到的IO完成中断信号，获得等待队列中的PCB
     *
     * @param interruptRequest 中断信号
     * @return pcb
     */
    private static PCB getPcbInWaitingQueue(String interruptRequest) {
        String[] strings = interruptRequest.split("-");
        String interruptType = strings[0];
        String pid = strings[1];
        PCB pcbInWaitingQueue = pcbTable.get(Integer.parseInt(pid));
        pcbInWaitingQueue.setIr(pcbInWaitingQueue.getIr() + 1);

        pcbInWaitingQueue.setState(READY);
        pcbInWaitingQueue.setRemainingTime(-1);
        pcbInWaitingQueue.setStartTime(-1);
        return pcbInWaitingQueue;
    }

    /**
     * 将错误页对应磁盘块，重新加载到内存中，更新页表上的ppn和present，换出的页present置为false
     */
    public static void handlePageFaultInterrupt(int register, int logicAddress, int oldPage) {
        mmu.PageFaultProcess(register, logicAddress, oldPage);
    }
}
