package bupt.os.service.impl;


import bupt.os.component.cpu.CPUSimulator;
import bupt.os.component.cpu.ProcessExecutionTask;
import bupt.os.component.disk.filesystem_ly.Disk;
import bupt.os.component.memory.ly.PCB;
import bupt.os.component.memory.ly.ProtectedMemory;
import bupt.os.dto.req.ProcessCreateReqDTO;
import bupt.os.dto.resp.ProcessQueryAllRespDTO;
import bupt.os.service.ProcessManageService;
import bupt.os.tools.DiskTool;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Queue;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

import static bupt.os.common.constant.InstructionConstant.M;
import static bupt.os.common.constant.ProcessStateConstant.CREATED;
import static bupt.os.tools.CommonTool.getPid;

@Service
public class ProcessManageServiceImpl implements ProcessManageService {

    private final CPUSimulator cpuSimulator = CPUSimulator.getInstance();
    private final Disk disk = Disk.getInstance();
    private final ProtectedMemory protectedMemory = ProtectedMemory.getInstance();

    /**
     * 创建进程
     *
     * @param processCreateReqDTO 创建请求
     */
    @Override
    public void createProcess(ProcessCreateReqDTO processCreateReqDTO) {
        String processName = processCreateReqDTO.getProcessName();

        String[] instructions = processCreateReqDTO.getInstructions();
        // 根据 M 指令，计算作业文件大小
        int pageCount;
        String MInst = Arrays.stream(instructions).filter(inst -> M.equals(inst.charAt(0) + "")).toArray(String[]::new)[0];
        pageCount = Integer.parseInt(MInst.split(" ")[1]);

        // M 指令以外的其他指令
        String[] otherInst = Arrays.stream(instructions).filter(inst -> !M.equals(inst.charAt(0) + "")).toArray(String[]::new);
        // TODO 进程号的获得方式需要修改
        int index = DiskTool.getINodeIndex(processName);

        // 创建进程pcb，放进pcbTable
        PCB pcb = new PCB(index, processName, 0, pageCount * 1024, CREATED, -1, -1, -1, otherInst, null);
        HashMap<Integer, PCB> pcbTable = protectedMemory.getPcbTable();
        pcbTable.put(index, pcb);

    }

    /**
     * 执行进程，指令放入CPB
     *
     * @param processName 进程名
     */
    @Override
    public void executeProcess(String processName) {

        int pid = getPid(processName);
        HashMap<Integer, PCB> pcbTable = protectedMemory.getPcbTable();
        PCB pcb = pcbTable.get(pid);
        // 封装成可提交任务对象
        ProcessExecutionTask processExecutionTask = new ProcessExecutionTask(pcb);
        // 有空闲CPU，直接提交
        ThreadPoolExecutor cpuSimulatorExecutor = (ThreadPoolExecutor) cpuSimulator.getExecutor();
        int idleThreads = cpuSimulatorExecutor.getMaximumPoolSize() - cpuSimulatorExecutor.getActiveCount();
        if (idleThreads > 0) {
            Future<?> future = cpuSimulatorExecutor.submit(processExecutionTask);
            try {
                future.get();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            // CPU都繁忙，pcb放进就绪队列
            Queue<PCB> readyQueue = protectedMemory.getReadyQueue();
            readyQueue.add(pcb);
        }
    }

    @Override
    public ProcessQueryAllRespDTO queryAllProcessInfo() {
        Queue<PCB> runningQueue = protectedMemory.getRunningQueue();
        Queue<PCB> waitingQueue = protectedMemory.getWaitingQueue();
        Queue<PCB> readyQueue = protectedMemory.getReadyQueue();

        PCB pcb = runningQueue.peek();
        int pid = -1;
        String currInst = "";
        long startTime = -1;
        if (pcb != null) {
            pid = pcb.getPid();
            currInst = pcb.getInstructions()[pcb.getIr()];
            System.out.println(pcb.getIr());
            startTime = pcb.getStartTime();
        }
        return new ProcessQueryAllRespDTO(pid, currInst, startTime, waitingQueue.stream().map(PCB::getPid).collect(Collectors.toList()), readyQueue.stream().map(PCB::getPid).collect(Collectors.toList()));
    }
}
