package bupt.os.service.impl;


import bupt.os.component.cpu.CPUSimulator;
import bupt.os.component.cpu.ProcessExecutionTask;
import bupt.os.component.filesystem.FileNode;
import bupt.os.component.filesystem.FileReader;
import bupt.os.component.filesystem.FileSystem;
import bupt.os.component.filesystem.FileWriter;
import bupt.os.component.memory.protected_.PCB;
import bupt.os.component.memory.protected_.ProtectedMemory;
import bupt.os.component.memory.user.MemoryManagementImpl;
import bupt.os.component.scheduler.ProcessScheduler;
import bupt.os.dto.req.ProcessCreateReqDTO;
import bupt.os.dto.resp.ProcessQueryAllRespDTO;
import bupt.os.service.ProcessManageService;
import bupt.os.tools.CommonTool;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

import static bupt.os.common.constant.ProcessStateConstant.CREATED;
import static bupt.os.tools.CommonTool.getPid;

@Service
public class ProcessManageServiceImpl implements ProcessManageService {

    private final CPUSimulator cpuSimulator = CPUSimulator.getInstance();
    private final ProtectedMemory protectedMemory = ProtectedMemory.getInstance();
    private static final MemoryManagementImpl mmu = new MemoryManagementImpl();
    private static final FileSystem fileSystem = FileSystem.getInstance();
    // 表
    private final HashMap<Integer, PCB> pcbTable = protectedMemory.getPcbTable();
    private final Queue<PCB> readyQueue = protectedMemory.getReadyQueue();
    private final Queue<PCB> runningQueue = protectedMemory.getRunningQueue();
    private final Queue<PCB> waitingQueue = protectedMemory.getWaitingQueue();

    /**
     * 创建进程
     *
     * @param processCreateReqDTO 创建请求
     */
    @Override
    public void createProcess(ProcessCreateReqDTO processCreateReqDTO) {
        String processName = processCreateReqDTO.getProcessName();
        int pid = CommonTool.getPid(processName);

        // 创建进程pcb，放进pcbTable
        PCB pcb = new PCB(pid, processName, 0, -1, CREATED, -1, -1, -1, 3, null);
        pcbTable.put(pid, pcb);

        String[] instructions = processCreateReqDTO.getInstructions();
        LinkedList<String> list = new LinkedList<>();
        long expectedTime = 0;
        for (String inst : instructions) {
            if (inst.charAt(0) == 'M') {
                pcb.setSize(inst.charAt(2) * 1024);
            } else {
                list.add(inst);
                if (inst.charAt(0) == 'C') {
                    expectedTime += Long.parseLong(inst.split(" ")[1]);
                }
                if (inst.charAt(0) == 'R') {
                    expectedTime += Long.parseLong(inst.split(" ")[2]);
                }
                if (inst.charAt(0) == 'W') {
                    expectedTime += Long.parseLong(inst.split(" ")[2]);
                }
            }
        }
        pcb.setInstructions(list.toArray(new String[1]));
        pcb.setExpectedTime(expectedTime);

        // 写进文件系统
        fileSystem.touch(processName);
        FileNode file = fileSystem.getFile(processName);
        StringBuilder sb = new StringBuilder();
        for (String l: list) {
            sb.append(l).append("#");
        }
        file.setContent(sb.toString());
    }

    /**
     * 执行进程，指令放入CPB
     *
     * @param processName 进程名
     */
    @Override
    public void executeProcess(String processName) {

        int pid = getPid(processName);
        PCB pcb = pcbTable.get(pid);
        // 创建进程时，需要分配内存
        int pageTable = mmu.Allocate(pcb.getPid(), pcb.getSize());
        pcb.setRegister(pageTable);
        // 封装成可提交任务对象
        ProcessExecutionTask processExecutionTask = new ProcessExecutionTask(pcb);
        // 有空闲CPU，直接提交
        ThreadPoolExecutor cpuSimulatorExecutor = (ThreadPoolExecutor) cpuSimulator.getExecutor();
        int idleThreads = cpuSimulatorExecutor.getMaximumPoolSize() - cpuSimulatorExecutor.getActiveCount();
        if (idleThreads > 0) {
            cpuSimulatorExecutor.submit(processExecutionTask);
        } else {
            // CPU都繁忙，pcb放进就绪队列
            readyQueue.add(pcb);
        }
    }

    @Override
    public ProcessQueryAllRespDTO queryAllProcessInfo() {
        HashMap<Integer, List<Integer>> hashMap = new HashMap<>();
        List<Integer> q1 = readyQueue.stream().filter(p -> p.getPriority() == 1).map(PCB::getPid).toList();
        hashMap.put(1, q1);
        List<Integer> q2 = readyQueue.stream().filter(p -> p.getPriority() == 2).map(PCB::getPid).toList();
        hashMap.put(2, q2);
        List<Integer> q3 = readyQueue.stream().filter(p -> p.getPriority() == 3).map(PCB::getPid).toList();
        hashMap.put(3, q3);
        return new ProcessQueryAllRespDTO(runningQueue.stream().toList(), waitingQueue.stream().map(PCB::getPid).collect(Collectors.toList()), hashMap);
    }

    @Override
    public void switchStrategy(String strategy) {
        ProcessScheduler.strategy = strategy;
    }
}
