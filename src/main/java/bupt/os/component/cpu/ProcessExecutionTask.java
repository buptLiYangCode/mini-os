package bupt.os.component.cpu;

import bupt.os.component.interrupt.InterruptRequestLine;
import bupt.os.component.memory.*;
import lombok.extern.slf4j.Slf4j;

import java.util.LinkedList;
import java.util.Optional;
import java.util.Queue;

import static bupt.os.common.constant.InstructionConstant.*;
import static bupt.os.common.constant.ProcessStateConstant.RUNNING;
import static bupt.os.component.interrupt.InterruptHandler.handleHardInterrupt;
import static bupt.os.component.interrupt.InterruptHandler.handleSoftInterrupt;
import static bupt.os.component.memory.MMU.accessPage;

@Slf4j
public class ProcessExecutionTask implements Runnable {

    private final PCB pcb;
    private final String[] instructions;

    public ProcessExecutionTask(PCB pcb, String[] instructions) {
        this.pcb = pcb;
        this.instructions = instructions;
    }

    /**
     *
     */
    @Override
    public void run() {
        startUpdate();
        int ir = 0;
        // CPU 收到时钟中断、执行IO指令都会切换进程
        boolean isSwitchProcess = false;
        for (String instruction : instructions) {
            // 执行到IO指令也会导致进程切换
            isSwitchProcess = executeInstruction(instruction);
            ir++;
            // CPU每执行一条指令，都需要去检查 irl 是否有中断信号
            InterruptRequestLine interruptRequestLine = InterruptRequestLine.getInstance();
            String interruptRequest = interruptRequestLine.get();
            if (interruptRequest != null) {
                log.info("收到中断信号: " + interruptRequest + ", 当前运行进程：" + pcb.getProcessName());
                // 处理硬件中断信号，CPU去执行中断处理程序了，
                isSwitchProcess = handleHardInterrupt(pcb, ir, interruptRequest);
            }
            // 发生进程切换，结束任务
            if (isSwitchProcess) {
                break;
            }
        }
        log.info("进程" + pcb.getProcessName() + "执行完毕");
    }

    /**
     * 进程（任务）开始执行时，先更新进程pcb，再将pcb移出就绪队列，放进运行队列
     */
    private void startUpdate() {
        ProtectedMemory protectedMemory = ProtectedMemory.getInstance();
        // 更新pcb
        pcb.setState(RUNNING);
        pcb.setStartTime(System.currentTimeMillis());
        pcb.setRemainingTime(2000);
        // 就绪队列
        Queue<PCB> readyQueue = protectedMemory.getReadyQueue();
        readyQueue.remove(pcb);
        // 运行队列
        Queue<PCB> runningQueue = protectedMemory.getRunningQueue();
        runningQueue.add(pcb);
    }

    /**
     * 不同指令对应执行程序
     * @param instruction 指令
     * @return 是否发送进程切换
     */
    private boolean executeInstruction(String instruction) {
        ProtectedMemory protectedMemory = ProtectedMemory.getInstance();
        LinkedList<DeviceInfo> deviceInfoTable = protectedMemory.getDeviceInfoTable();
        // 执行指令时，是否会导致进程切换
        boolean isSwitchProcess = false;

        String[] parts = instruction.split(" ");
        String command = parts[0];
        try {
            switch (command) {
                case A -> {
                    int vpn = Integer.parseInt(parts[1]);
                    boolean isPageFault = accessPage(pcb, vpn);
                    // 页错误属于软件中断
                    if (isPageFault) {
                        handleSoftInterrupt(pcb, vpn);
                    }
                }
                case C -> {
                    int computeTime = Integer.parseInt(parts[1]);
                    log.info("Computing for " + computeTime + " units of time.");
                    Thread.sleep(computeTime); // Simulate computation
                    log.info(pcb.getProcessName() + "：" + instruction + "执行完成");
                }
                case K, P -> {
                    long inputTime = Integer.parseInt(parts[1]);
                    log.info("Waiting for keyboard input for " + inputTime + " units of time.");
                    Optional<DeviceInfo> first = deviceInfoTable.stream()
                            .filter(deviceInfo -> deviceInfo.getDeviceType().equals(command))
                            .findFirst();
                    // 检查 first 是否有值
                    if (first.isPresent()) {
                        DeviceInfo deviceInfo = first.get();
                        LinkedList<IoRequest> ioRequestQueue = deviceInfo.getIoRequestQueue();
                        ioRequestQueue.add(new IoRequest(pcb, inputTime));
                        // 发送进程切换
                        isSwitchProcess = true;
                        log.info(pcb.getProcessName() + "：" + instruction + "执行完成");
                    }
                    log.info(command + "不存在");
                }
                case R -> {
                    String readFile = parts[1];
                    int readTime = Integer.parseInt(parts[2]);
                    log.info("Reading from file " + readFile + " for " + readTime + " units of time.");
                    Thread.sleep(readTime); // Simulate file reading
                    log.info(pcb.getProcessName() + "：" + instruction + "执行完成");
                }
                case W -> {
                    String writeFile = parts[1];
                    int writeTime = Integer.parseInt(parts[2]);
                    int fileSize = Integer.parseInt(parts[3]);
                    log.info("Writing to file " + writeFile + " of size " + fileSize + " blocks for " + writeTime + " units of time.");
                    Thread.sleep(writeTime); // Simulate file writing
                    log.info(pcb.getProcessName() + "：" + instruction + "执行完成");
                }
                case Q -> {
                    log.info("Terminating the program.");
                    log.info(pcb.getProcessName() + "：" + instruction + "执行完成");
                }
                default -> log.info("Unknown command.");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.info("Task was interrupted.");
        }
        return isSwitchProcess;
    }

}
