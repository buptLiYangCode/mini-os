package bupt.os.component.interrupt;

import bupt.os.component.disk.Disk;
import bupt.os.component.filesystem.CommonFile;
import bupt.os.component.memory.PCB;
import bupt.os.component.memory.ProtectedMemory;
import lombok.extern.slf4j.Slf4j;

import java.util.LinkedList;
import java.util.Queue;

import static bupt.os.common.constant.ProcessStateConstant.READY;
import static bupt.os.component.memory.MMU.loadPageIntoMemory;

@Slf4j
public class InterruptHandler {

    private static final Disk disk = Disk.getInstance();
    private static final ProtectedMemory protectedMemory = ProtectedMemory.getInstance();

    /**
     * 硬件中断处理程序，如果发生进程切换，需要更新PCB状态
     * @param interruptRequest 中断信号
     * @return 是否切换进程，返回true，则当前运行进程出让CPU
     */
    public static boolean handleHardInterrupt(PCB pcb, int ir, String interruptRequest) {
        boolean isSwitchProcess = false;
        switch (interruptRequest) {
            case "TIMER_INTERRUPT" -> {
                Queue<PCB> runningQueue = protectedMemory.getRunningQueue();
                Queue<PCB> readyQueue = protectedMemory.getReadyQueue();

                // 时间片是否耗尽
                if (System.currentTimeMillis() - pcb.getStartTime() > pcb.getRemainingTime()) {
                    // 更新PCB状态
                    pcb.setIr(ir);
                    pcb.setState(READY);
                    pcb.setRemainingTime(0);
                    pcb.setStartTime(-1);
                    // 移除满足条件的元素
                    runningQueue.remove(pcb);
                    // 添加到就绪队列
                    readyQueue.add(pcb);
                    log.info("进程" + pcb.getProcessName() + "时间片耗尽");
                    isSwitchProcess = true;
                }
            }
            case "IO_INTERRUPT" -> {
                log.info("处理" + "IO_INTERRUPT");
            }
        }

        return isSwitchProcess;
    }

    /**
     * 将错误页对应磁盘块，重新加载到内存中，更新页表上的ppn和present，换出的页present置为false
     * @param pcb pcb
     * @param vpn 需要换入内存的虚拟页号
     */
    public static void handleSoftInterrupt(PCB pcb, int vpn) {
        int iNodeIndex = pcb.getPid();
        CommonFile jobFile = (CommonFile) disk.getINodes()[iNodeIndex];
        LinkedList<Integer> blockNumbers = jobFile.getBlockNumbers();

        loadPageIntoMemory(blockNumbers);
    }
}
