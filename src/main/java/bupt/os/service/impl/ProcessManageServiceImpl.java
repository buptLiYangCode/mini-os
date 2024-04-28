package bupt.os.service.impl;


import bupt.os.component.cpu.CPUSimulator;
import bupt.os.component.cpu.ProcessExecutionTask;
import bupt.os.component.disk.Disk;
import bupt.os.component.filesystem.CommonFile;
import bupt.os.component.memory.PCB;
import bupt.os.component.memory.PageInfo;
import bupt.os.component.memory.ProtectedMemory;
import bupt.os.dto.req.ProcessCreateReqDTO;
import bupt.os.dto.resp.ProcessQueryAllRespDTO;
import bupt.os.service.ProcessManageService;
import bupt.os.tools.DiskTool;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

import static bupt.os.common.constant.CommonConstant.BLOCKS_PER_PAGE;
import static bupt.os.common.constant.InstructionConstant.M;
import static bupt.os.common.constant.ProcessStateConstant.CREATED;
import static bupt.os.component.memory.UserMemory.PAGE_SIZE;
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
        CommonFile commonFile = new CommonFile();
        commonFile.setFileName(processName);
        commonFile.setCreateTime(LocalDateTime.now());
        String[] instructions = processCreateReqDTO.getInstructions();
        // 根据 M 指令，计算作业文件大小
        int pageCount;
        String MInst = Arrays.stream(instructions).filter(inst -> M.equals(inst.charAt(0) + "")).toArray(String[]::new)[0];
        pageCount = Integer.parseInt(MInst.split(" ")[1]);
        commonFile.setSize(pageCount * PAGE_SIZE);
        commonFile.setBlockCount(pageCount * BLOCKS_PER_PAGE);
        LinkedList<Integer> freeBlockNumbers = DiskTool.getFreeBlocks(disk, pageCount * BLOCKS_PER_PAGE);
        commonFile.setBlockNumbers(freeBlockNumbers);


        // M 指令以外的其他指令
        String[] otherInst = Arrays.stream(instructions).filter(inst -> !M.equals(inst.charAt(0) + "")).toArray(String[]::new);
        // 所有指令会写入分配给文件的block块中，从第一个块开始写
        StringBuilder sb = new StringBuilder();
        for (String inst : otherInst) {
            // #表示换行符
            sb.append(inst).append("#");
        }
        Integer firstBlock = commonFile.getBlockNumbers().get(0);
        char[] data = disk.getBlocks()[firstBlock];
        char[] arr = sb.toString().toCharArray();
        System.arraycopy(arr, 0, data, 0, arr.length);

        // 将文件信息存进磁盘inode[]、inodeBitMap、blocks[]、blockBitmap
        int index = DiskTool.getINodeIndex(processName);
        disk.setINodeByIndex(index, commonFile);
        disk.setINodeBitmapByIndex(index, true);
        if (freeBlockNumbers != null)
            disk.setBlockBitmapByBlockNumbers(freeBlockNumbers, true);


        // 创建进程pcb，放进pcbTable
        PCB pcb = new PCB(index, processName, 0, pageCount * PAGE_SIZE, CREATED, null, -1, -1, otherInst, null);
        HashMap<Integer, PCB> pcbTable = protectedMemory.getPcbTable();
        pcbTable.put(index, pcb);
        // 存储进程虚拟页号->物理页号的映射关系
        HashMap<Integer, LinkedList<PageInfo>> processPageTable = protectedMemory.getProcessPageTable();
        LinkedList<PageInfo> pageInfoList = new LinkedList<>();
        for (int i = 0; i < pageCount; i++) {
            PageInfo pageInfo = new PageInfo(-1, false);
            pageInfoList.add(pageInfo);
        }
        processPageTable.put(index, pageInfoList);
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
