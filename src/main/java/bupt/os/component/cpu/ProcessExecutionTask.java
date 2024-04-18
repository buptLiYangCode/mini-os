package bupt.os.component.cpu;

import bupt.os.component.interrupt.InterruptRequestLine;
import bupt.os.component.memory.PCB;
import bupt.os.component.memory.ProtectedMemory;
import bupt.os.tools.DiskTool;

import java.util.HashMap;
import java.util.Queue;

import static bupt.os.component.interrupt.InterruptHandler.handleInterrupt;

public class ProcessExecutionTask implements Runnable {

    private final String processName;
    private final String[] instructions;

    public ProcessExecutionTask(String processName, String[] instructions) {
        this.processName = processName;
        this.instructions = instructions;
    }

    @Override
    public void run() {
        for (String instruction : instructions) {
            executeInstruction(instruction);
            // CPU每执行一条指令，都需要去检查 irl 是否有信号
            InterruptRequestLine interruptRequestLine = InterruptRequestLine.getInstance();
            String interruptRequest = interruptRequestLine.get();
            if (interruptRequest != null) {
                System.out.println("收到中断信号: " + interruptRequest);
                // 处理中断信号
                handleInterrupt(interruptRequest);
            }
        }
    }


    private void executeInstruction(String instruction) {
        String[] parts = instruction.split(" ");
        String command = parts[0];
        try {
            switch (command) {
                case "S" -> {
                    // 进程（任务）开始执行时，将进程pcb放进运行队列
                    int pid = DiskTool.getINodeIndex(processName) - 1;
                    ProtectedMemory protectedMemory = ProtectedMemory.getInstance();
                    HashMap<Integer, PCB> pcbTable = protectedMemory.getPcbTable();
                    PCB pcb = pcbTable.get(pid);
                    // 运行队列
                    Queue<PCB> runningQueue = protectedMemory.getRunningQueue();
                    runningQueue.add(pcb);
                    // 就绪队列
                    Queue<PCB> readyQueue = protectedMemory.getReadyQueue();
                    readyQueue.remove(pcb);
                }
                case "C" -> {
                    int computeTime = Integer.parseInt(parts[1]);
                    System.out.println("Computing for " + computeTime + " units of time.");
                    Thread.sleep(computeTime * 1000L); // Simulate computation
                    System.out.println(processName + "：" + instruction + "执行完成");
                }
                case "K" -> {
                    int inputTime = Integer.parseInt(parts[1]);
                    System.out.println("Waiting for keyboard input for " + inputTime + " units of time.");
                    Thread.sleep(inputTime * 1000L); // Simulate keyboard input
                    System.out.println(processName + "：" + instruction + "执行完成");
                }
                case "P" -> {
                    int printTime = Integer.parseInt(parts[1]);
                    System.out.println("Printing for " + printTime + " units of time.");
                    Thread.sleep(printTime * 1000L); // Simulate printing
                    System.out.println(processName + "：" + instruction + "执行完成");
                }
                case "R" -> {
                    String readFile = parts[1];
                    int readTime = Integer.parseInt(parts[2]);
                    System.out.println("Reading from file " + readFile + " for " + readTime + " units of time.");
                    Thread.sleep(readTime * 1000L); // Simulate file reading
                    System.out.println(processName + "：" + instruction + "执行完成");
                }
                case "W" -> {
                    String writeFile = parts[1];
                    int writeTime = Integer.parseInt(parts[2]);
                    int fileSize = Integer.parseInt(parts[3]);
                    System.out.println("Writing to file " + writeFile + " of size " + fileSize + " blocks for " + writeTime + " units of time.");
                    Thread.sleep(writeTime * 1000L); // Simulate file writing
                    System.out.println(processName + "：" + instruction + "执行完成");
                }
                case "Q" -> {
                    System.out.println("Terminating the program.");
                    System.out.println(processName + "：" + instruction + "执行完成");
                }
                default -> System.out.println("Unknown command.");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("Task was interrupted.");
        }
    }

}
