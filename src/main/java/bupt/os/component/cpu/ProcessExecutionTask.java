package bupt.os.component.cpu;

import bupt.os.component.filesystem.FileNode;
import bupt.os.component.filesystem.FileReader;
import bupt.os.component.filesystem.FileSystem;
import bupt.os.component.filesystem.FileWriter;
import bupt.os.component.interrupt.InterruptRequestLine;
import bupt.os.component.memory.protected_.DeviceInfo;
import bupt.os.component.memory.protected_.IoRequest;
import bupt.os.component.memory.protected_.PCB;
import bupt.os.component.memory.protected_.ProtectedMemory;
import bupt.os.component.memory.user.MemoryManagementImpl;
import bupt.os.component.scheduler.ProcessScheduler;
import lombok.extern.slf4j.Slf4j;

import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;

import static bupt.os.common.constant.InstructionConstant.*;
import static bupt.os.common.constant.ProcessStateConstant.*;
import static bupt.os.component.interrupt.InterruptHandler.*;
import static bupt.os.component.scheduler.ProcessScheduler.executeNextProcess;

@Slf4j
public class ProcessExecutionTask implements Runnable {

    // 物理组件
    private static final ProtectedMemory protectedMemory = ProtectedMemory.getInstance();
    private static final MemoryManagementImpl mmu = new MemoryManagementImpl();
    private static final FileSystem fileSystem = FileSystem.getInstance();
    private static final FileReader fileReader = FileReader.getInstance();
    private static final FileWriter fileWriter = FileWriter.getInstance();
    // 保护空间存储的表
    LinkedList<DeviceInfo> deviceInfoTable = protectedMemory.getDeviceInfoTable();
    Queue<PCB> waitingQueue = protectedMemory.getWaitingQueue();
    Queue<PCB> runningQueue = protectedMemory.getRunningQueue();
    Queue<PCB> readyQueue = protectedMemory.getReadyQueue();
    private static final ConcurrentHashMap<Long, InterruptRequestLine> irlTable = protectedMemory.getIrlTable();

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
        try {
            InterruptRequestLine irl = irlTable.get(Thread.currentThread().getId());
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
                    // 执行到IO指令，一直获取不到文件资源，都会导致进程切换，ir不会+1  isSwitchProcess = 2 表示已经完成 进程 在就绪、运行队列中切换
                    isSwitchProcess = executeInstruction(instruction);
                    if (isSwitchProcess != 2)
                        pcb.setIr(pcb.getIr() + 1);
                    // CPU每执行一条指令，都需要去检查 irl 是否有中断信号
                    String peek = irl.peek();
                    if (peek != null && isSwitchProcess != 2) {
                        // 处理硬件中断信号，CPU去执行中断处理程序了。时间片耗尽也会导致进程切换，isSwitchProcess = 1 表示时间片耗尽导致的进程切换
                        int i = handleHardInterrupt(pcb);
                        if (i != 0)
                            isSwitchProcess = i;
                    }
                    // 时间片耗尽导致进程切换
                    if (isSwitchProcess > 0)
                        break;
                }
            }
            log.info(pcb.getProcessName() + "出让CPU");
            System.out.println("运行队列" + protectedMemory.getRunningQueue().stream().map(PCB::getProcessName).toList());
            System.out.println("就绪队列" + protectedMemory.getReadyQueue().stream().map(PCB::getProcessName).toList());
            System.out.println("等待队列" + protectedMemory.getWaitingQueue().stream().map(PCB::getProcessName).toList());
            // 调度器调度下一个可执行进程
            executeNextProcess();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * 进程（任务）开始执行时：
     * 1.先更新进程pcb，再将pcb移出就绪队列，放进运行队列
     * 2.分配驻留集
     */
    private void startUpdate() {
        // 1
        pcb.setState(RUNNING);
        if (ProcessScheduler.strategy.equals("RR"))
            pcb.setRemainingTime(3800L);
        else if (ProcessScheduler.strategy.equals("MLFQ") && pcb.getRemainingTime() < 0)
            pcb.setRemainingTime(3800L);
        else if (ProcessScheduler.strategy.equals("FCFS") || ProcessScheduler.strategy.equals("SJF"))
            pcb.setRemainingTime(99999999L);
        try {
            // 这里之前检测并发异常，添加了的try-catch结构
            readyQueue.removeIf(p -> p.equals(pcb));
        } catch (Exception e) {
            e.printStackTrace();
        }
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
                    // TODO lyq
                    int logicAddress = Integer.parseInt(parts[1]);
                    if (logicAddress == 8024) {
                        System.out.println("nihoa");
                    }
                    byte[] byteArray = new byte[4];
                    System.out.println("----------------------testtestetset:   " + pcb.getRegister());
                    int result = mmu.Read(pcb.getRegister(), logicAddress, byteArray);
                    if (result == 0) {
                        System.out.println("逻辑地址" + logicAddress + "访问成功");
                    } else if (result == -1) {
                        System.out.println("逻辑地址" + logicAddress + "页错误");
                        // TODO lyq

                        handlePageFaultInterrupt(pcb.getRegister(), logicAddress, ByteBuffer.wrap(byteArray).getInt());
                        mmu.Read(pcb.getRegister(), logicAddress, byteArray);
                        System.out.println("将缺失页换入内存后，Read操作成功");
                    } else if (result == -2) {
                        System.out.println("逻辑地址" + logicAddress + "越界访问");
                    }
                    log.info("执行完" + instruction);
                }
                case C -> {
                    int computeTime = Integer.parseInt(parts[1]);
                    Thread.sleep(computeTime); // Simulate computation
                    pcb.setRemainingTime(pcb.getRemainingTime() - computeTime);
                    log.info(pcb.getProcessName() + "：" + instruction + "执行完成");
                }
                case D -> {
                    long inputTime = Integer.parseInt(parts[2]);
                    String deviceName = parts[1];
                    // 查询是否有同名设备
                    Optional<DeviceInfo> first = deviceInfoTable.stream()
                            .filter(deviceInfo -> deviceInfo.getDeviceName().equals(deviceName))
                            .findFirst();
                    // 检查 first 是否有值
                    if (first.isPresent()) {
                        DeviceInfo deviceInfo = first.get();
                        LinkedList<IoRequest> ioRequestQueue = deviceInfo.getIoRequestQueue();
                        // 将设备使用请求添加进请求队列
                        ioRequestQueue.add(new IoRequest(pcb, inputTime, Thread.currentThread().getId()));
                        // 进程切换
                        pcb.setState(WAITING);
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
                    String filePath = parts[1];
                    long readTime = Integer.parseInt(parts[2]);
                    FileNode fileNode = fileSystem.getFile(filePath);
                    boolean acquired = fileReader.readFile(pcb, fileNode, readTime);
                    if (acquired)
                        log.info(pcb.getProcessName() + "：" + instruction + "执行完成，成功读取该文件");
                    else {
                        // 阻塞到时间片耗尽，仍然不能访问资源，isSwitchProcess置为2，表示进程切换，下一次仍会执行获取文件资源的指令
                        // 进程切换
                        pcb.setState(READY);
                        pcb.setRemainingTime(-1);
                        if (ProcessScheduler.strategy.equals("MLFQ") && pcb.getPriority() > 1)
                            pcb.setPriority(pcb.getPriority() - 1);
                        // 放入等待队列 TODO
                        readyQueue.add(pcb);
                        // 移出运行队列
                        runningQueue.remove(pcb);
                        isSwitchProcess = 2;
                        log.info(pcb.getProcessName() + "：" + instruction + "执行失败，正在读取该文件的进程数达到最大值");
                    }
                }
                case W -> {
                    String filePath = parts[1];
                    long writeTime = Integer.parseInt(parts[2]);
                    String data = parts[3];
                    FileNode fileNode = fileSystem.getFile(filePath);
                    boolean acquired = fileWriter.writeFile(pcb, fileNode, writeTime, data);
                    if (acquired)
                        log.info(pcb.getProcessName() + "：" + instruction + "执行完成，成功读取该文件");
                    else {
                        // 阻塞到时间片耗尽，仍然不能访问资源，isSwitchProcess置为2，表示进程切换，下一次仍会执行获取文件资源的指令
                        // 进程切换
                        pcb.setState(READY);
                        pcb.setRemainingTime(-1);
                        if (ProcessScheduler.strategy.equals("MLFQ") && pcb.getPriority() > 1)
                            pcb.setPriority(pcb.getPriority() - 1);
                        // 放入等待队列 TODO
                        readyQueue.add(pcb);
                        // 移出运行队列
                        runningQueue.remove(pcb);
                        isSwitchProcess = 2;
                        log.info(pcb.getProcessName() + "：" + instruction + "执行失败，正在读取该文件的进程数达到最大值");
                    }
                }
                case Q -> {
                    pcb.setIr(0);
                    pcb.setState(CREATED);
                    pcb.setRemainingTime(-1);
                    // TODO lyq 释放内存
                    mmu.Free(pcb.getRegister());
                    pcb.setRegister(-1);

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
