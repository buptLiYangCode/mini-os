package bupt.os.component.cpu;

import bupt.os.component.interrupt.InterruptRequestLine;
import bupt.os.component.memory.DeviceInfo;
import bupt.os.component.memory.IoRequest;
import bupt.os.component.memory.PCB;
import bupt.os.component.memory.ProtectedMemory;
import lombok.extern.slf4j.Slf4j;

import java.util.LinkedList;
import java.util.Optional;
import java.util.Queue;

import static bupt.os.common.constant.InstructionConstant.*;
import static bupt.os.common.constant.ProcessStateConstant.*;
import static bupt.os.component.interrupt.InterruptHandler.*;
import static bupt.os.component.memory.MMU.accessPage;
import static bupt.os.component.process.scheduler.ProcessScheduler.executeNextProcess;

@Slf4j
public class ProcessExecutionTask implements Runnable {

    // 物理组件
    private static final CPUSimulator cpuSimulator = CPUSimulator.getInstance();
    private static final ProtectedMemory protectedMemory = ProtectedMemory.getInstance();
    private static final InterruptRequestLine irl = InterruptRequestLine.getInstance();

    // 保护空间存储的表
    LinkedList<DeviceInfo> deviceInfoTable = protectedMemory.getDeviceInfoTable();
    Queue<PCB> waitingQueue = protectedMemory.getWaitingQueue();
    Queue<PCB> runningQueue = protectedMemory.getRunningQueue();
    Queue<PCB> readyQueue = protectedMemory.getReadyQueue();
    // 可执行任务的属性
    private final PCB pcb;
    private final String[] instructions;

    public ProcessExecutionTask(PCB pcb) {
        this.pcb = pcb;
        this.instructions = pcb.getInstructions();
    }

    /**
     *
     */
    @Override
    public void run() {

        startUpdate();
        // CPU 收到时钟中断、执行IO指令都会切换进程，0表示未切换，1表示切换后放进就绪队列，2表示切换后放进等待队列
        int isSwitchProcess;

        for (int ir = pcb.getIr(); ir < instructions.length; ir = pcb.getIr()) {

            String instruction = instructions[ir];
            if (instruction.equals(Q)) {
                executeInstruction(instruction);
                String peek = irl.peek();
                if (peek != null) {
                    handleHardInterruptIo();
                }
                break;
            } else {
                // 执行到IO指令也会导致进程切换 isSwitchProcess = 2
                isSwitchProcess = executeInstruction(instruction);
                if (isSwitchProcess != 2)
                    pcb.setIr(pcb.getIr() + 1);
                // CPU每执行一条指令，都需要去检查 irl 是否有中断信号
                String peek = irl.peek();
                if (peek != null) {
                    // 处理硬件中断信号，CPU去执行中断处理程序了。时间片耗尽也会导致进程切换，isSwitchProcess = 1
                    isSwitchProcess = handleHardInterrupt(pcb);
                }
                // 时间片耗尽导致进程切换
                if (isSwitchProcess > 0)
                    break;
            }
        }
        log.info(pcb.getProcessName() + "出让CPU");

        // 调度器调度下一个可执行进程
        executeNextProcess();

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
        readyQueue.removeIf(p -> p.equals(pcb));
        // 运行队列
        runningQueue.add(pcb);
    }

    /**
     * 不同指令对应执行程序
     *
     * @param instruction 指令
     * @return 是否发送进程切换
     */
    private int executeInstruction(String instruction) {
        // 执行指令时，是否会导致进程切换
        int isSwitchProcess = 0;

        String[] parts = instruction.split(" ");
        String command = parts[0];
        try {
            log.info("当前指令" + instruction);
            switch (command) {
                case A -> {
                    int vpn = Integer.parseInt(parts[1]);
                    int pageNumber = protectedMemory.getProcessPageTable().get(pcb.getPid()).get(vpn).getPageNumber();
                    System.out.println(vpn + "->" + pageNumber);
                    boolean isPageFault = accessPage(pcb, vpn);
                    // 页错误属于软件中断
                    if (isPageFault) {
                        handleSoftInterrupt(pcb, vpn);
                    }
                    log.info("执行完" + instruction);
                }
                case C -> {
                    int computeTime = Integer.parseInt(parts[1]);
                    Thread.sleep(computeTime); // Simulate computation
                    log.info(pcb.getProcessName() + "：" + instruction + "执行完成");
                }
                case D -> {
                    long inputTime = Integer.parseInt(parts[2]);
                    String deviceName = parts[1];
                    Optional<DeviceInfo> first = deviceInfoTable.stream()
                            .filter(deviceInfo -> deviceInfo.getDeviceName().equals(deviceName))
                            .findFirst();
                    // 检查 first 是否有值
                    if (first.isPresent()) {
                        DeviceInfo deviceInfo = first.get();
                        LinkedList<IoRequest> ioRequestQueue = deviceInfo.getIoRequestQueue();
                        ioRequestQueue.add(new IoRequest(pcb, inputTime));
                        // 进程切换
                        pcb.setState(WAITING);
                        pcb.setRemainingTime(-1);
                        pcb.setStartTime(-1);
                        // 放入等待队列
                        waitingQueue.add(pcb);
                        // 移出运行队列
                        runningQueue.remove(pcb);
                        isSwitchProcess = 2;
                    } else
                        log.info("设备" + deviceName + "不存在");
                    log.info(pcb.getProcessName() + "：" + instruction + "执行完成");
                }
                case R -> {
                    String readFile = parts[1];
                    int readTime = Integer.parseInt(parts[2]);
                    Thread.sleep(readTime); // Simulate file reading
                    log.info(pcb.getProcessName() + "：" + instruction + "执行完成");
                }
                case W -> {
                    String writeFile = parts[1];
                    int writeTime = Integer.parseInt(parts[2]);
                    int fileSize = Integer.parseInt(parts[3]);
                    Thread.sleep(writeTime); // Simulate file writing
                    log.info(pcb.getProcessName() + "：" + instruction + "执行完成");
                }
                case Q -> {
                    pcb.setIr(0);
                    pcb.setState(CREATED);
                    pcb.setRemainingTime(-1);
                    pcb.setStartTime(-1);
                    // 移出运行队列
                    runningQueue.remove(pcb);
                    log.info(pcb.getProcessName() + "：" + instruction + "执行完成，***进程结束***");
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
