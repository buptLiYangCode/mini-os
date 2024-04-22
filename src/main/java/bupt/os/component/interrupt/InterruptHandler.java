package bupt.os.component.interrupt;

import bupt.os.component.memory.PCB;
import bupt.os.component.memory.PageInfo;
import bupt.os.component.memory.PageSwapInfo;
import bupt.os.component.memory.ProtectedMemory;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

import static bupt.os.component.memory.MMU.lruPageSwap;

@Slf4j
public class InterruptHandler {

    private static final ProtectedMemory protectedMemory = ProtectedMemory.getInstance();

    /**
     * 硬件中断处理程序，如果发生进程切换，需要更新PCB状态
     * @param interruptRequest 中断信号
     * @return 是否切换进程，返回true，则当前运行进程出让CPU
     */
    public static boolean handleHardInterrupt(PCB pcb, String interruptRequest) {
        Queue<PCB> runningQueue = protectedMemory.getRunningQueue();
        Queue<PCB> readyQueue = protectedMemory.getReadyQueue();

        boolean isSwitchProcess = false;
        switch (interruptRequest) {
            case "TIMER_INTERRUPT" -> {
                // 时间片耗尽了
                if (System.currentTimeMillis() - pcb.getStartTime() > pcb.getRemainingTime()) {
                    // 移除满足条件的元素
                    runningQueue.remove(pcb);
                    // 添加到就绪队列
                    readyQueue.add(pcb);
                    log.info("进程" + pcb.getProcessName() + "时间片耗尽");
                    isSwitchProcess = true;
                }
            }
            // TODO 进程执行完了，IO中断才到irl 上，后续执行的进程 会读取到这些无用的IO中断信号
            case "IO_INTERRUPT" -> {
                log.info("处理" + "IO_INTERRUPT");
                runningQueue.remove(pcb);
                readyQueue.add(pcb);
                log.info("进程" + pcb.getProcessName() + "进行IO，放弃CPU");
                isSwitchProcess = true;
            }
        }

        return isSwitchProcess;
    }

    /**
     * 将错误页对应磁盘块，重新加载到内存中，更新页表上的ppn和present，换出的页present置为false
     * @param pcb pcb
     * @param vpn 需要重新加载进内存的虚拟页号
     */
    public static void handleSoftInterrupt(PCB pcb, int vpn) {
        int loadPageNumber = lruPageSwap();
        // 更新进程页表
        int pid = pcb.getPid();
        HashMap<Integer, LinkedList<PageInfo>> processPageTable = protectedMemory.getProcessPageTable();
        LinkedList<PageInfo> list = processPageTable.get(pid);
        PageInfo pageInfo = list.get(vpn);
        pageInfo.setPageNumber(loadPageNumber);
        pageInfo.setPresent(true);
        // 更新页全部信息表
        LinkedList<PageSwapInfo> allPagesInfo = protectedMemory.getAllPagesInfo();
        PageSwapInfo pageSwapInfo = allPagesInfo.get(loadPageNumber);

        int lastPid = pageSwapInfo.getPid();
        int lastVpn = pageSwapInfo.getVpn();
        // 如果该页已经被其他进程使用
        if (lastPid != -1) {
            LinkedList<PageInfo> lastList = processPageTable.get(lastPid);
            PageInfo lastPageInfo = lastList.get(lastVpn);
            lastPageInfo.setPresent(false);
        }
        pageSwapInfo.setPid(pid);
        pageSwapInfo.setVpn(vpn);
        pageSwapInfo.setLoadTime(System.currentTimeMillis());
        pageSwapInfo.setLastAccessTime(System.currentTimeMillis());

        System.out.println("进程"+ pcb.getProcessName() + "vpn：" + vpn + "映射到ppn：" + loadPageNumber);
    }
}
