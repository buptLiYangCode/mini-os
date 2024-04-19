package bupt.os.service.impl;


import bupt.os.component.cpu.CPUSimulator;
import bupt.os.component.cpu.ProcessExecutionTask;
import bupt.os.component.disk.Block;
import bupt.os.component.disk.Disk;
import bupt.os.component.filesystem.CommonFile;
import bupt.os.component.memory.*;
import bupt.os.dto.req.ProcessCreateReqDTO;
import bupt.os.service.ProcessManageService;
import bupt.os.tools.DiskTool;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ExecutorService;

import static bupt.os.common.constant.InstructionConstant.A;
import static bupt.os.common.constant.InstructionConstant.M;
import static bupt.os.common.constant.ProcessStateConstant.CREATED;
import static bupt.os.common.constant.ProcessStateConstant.READY;
import static bupt.os.component.disk.Disk.BLOCK_SIZE;
import static bupt.os.component.memory.MMU.lruPageSwap;
import static bupt.os.component.memory.UserMemory.PAGE_SIZE;

@Service
public class ProcessManageServiceImpl implements ProcessManageService {

    private final CPUSimulator cpuSimulator= CPUSimulator.getInstance();
    private final Disk disk = Disk.getInstance();
    private final UserMemory userMemory = UserMemory.getInstance();
    private final ProtectedMemory protectedMemory = ProtectedMemory.getInstance();

    /**
     * 创建进程
     *
     * @param processCreateReqDTO 创建请求
     */
    @Override
    public void createProcess(ProcessCreateReqDTO processCreateReqDTO) {
        CommonFile commonFile = new CommonFile();
        commonFile.setFileName(processCreateReqDTO.getProcessName());
        String[] instructions = processCreateReqDTO.getInstructions();
        // 对于M 指令，会初始化作业文件的inode
        String[] strings1 = Arrays.stream(instructions).filter(inst -> M.equals(inst.charAt(0) + "")).toArray(String[]::new);
        String MInst = strings1[0];
        String[] parts = MInst.split(" ");
        int blockCount = Integer.parseInt(parts[1]);
        commonFile.setSize(blockCount * BLOCK_SIZE);
        commonFile.setBlockCount(blockCount);
        LinkedList<Integer> freeBlockNumbers = DiskTool.getFreeBlocks(disk, blockCount);
        commonFile.setBlockNumbers(freeBlockNumbers);
        // 将M 指令拆成对虚拟页号的访存指令，存进StringBuilder
        StringBuilder AInst = new StringBuilder();
        for (int i = 0; i < Objects.requireNonNull(freeBlockNumbers).size(); i++) {
            AInst.append(A).append(" ").append(i).append("#");
        }
        // 其他指令，存进StringBuilder
        String[] otherInst = Arrays.stream(instructions).filter(inst -> !M.equals(inst.charAt(0) + "")).toArray(String[]::new);
        StringBuilder sb = new StringBuilder(AInst);
        for (String inst : otherInst) {
            // #表示换行符
            sb.append(inst).append("#");
        }
        // 所有指令会写入分配给文件的block块中，从第一个块开始写
        Integer firstBlock = commonFile.getBlockNumbers().get(0);
        char[] data = disk.getBlocks()[firstBlock].getData();
        char[] arr = sb.toString().toCharArray();
        System.arraycopy(arr, 0, data, 0, arr.length);
        // 初始化文件创建时间
        commonFile.setCreateTime(LocalDateTime.now());
        // 将文件信息存进磁盘inode[]、inodeBitMap、blocks[]、blockBitmap
        int index = DiskTool.getINodeIndex(processCreateReqDTO.getProcessName());
        disk.setINodeByIndex(index, commonFile);
        disk.setINodeBitmapByIndex(index, true);
        disk.setBlockBitmapByBlockNumbers(freeBlockNumbers, true);
    }

    /**
     * 执行进程
     *
     * @param processName 进程名
     */
    @Override
    public void executeProcess(String processName) {
        // 1.先把作业文件加载进内存
        // 计算作业文件inode号
        int iNodeIndex = DiskTool.getINodeIndex(processName) - 1;
        CommonFile jobFile = (CommonFile) disk.getINodes()[iNodeIndex];
        // 初始化PCB
        PCB pcb = new PCB(iNodeIndex, processName, 0, jobFile.getSize(), CREATED, null, 0, -1, null);
        // 计算进程所需页数
        int requiredPageCount = jobFile.getSize() / PAGE_SIZE + 1;
        // lru算法获得进程可以使用的页号
        LinkedList<Integer> pageNumbers = lruPageSwap(requiredPageCount);
        // 将该进程vpn->ppn 映射放入进程页表
        HashMap<Integer, LinkedList<PageInfo>> processPageTable = protectedMemory.getProcessPageTable();
        LinkedList<PageInfo> pageTable = new LinkedList<>();
        for (int i = 0; i < requiredPageCount; i++) {
            Integer ppn = pageNumbers.get(i); // PhysicalPageNumber
            PageInfo pageInfo = new PageInfo(ppn, true, false, false);
            pageTable.add(pageInfo);
        }
        processPageTable.put(iNodeIndex, pageTable);
        // 磁盘块中作业文件复制进内存页
        int blocksPerPage = PAGE_SIZE / BLOCK_SIZE;
        LinkedList<Integer> blockNumbers = jobFile.getBlockNumbers();
        Page[] pages = userMemory.getPages();
        Block[] blocks = disk.getBlocks();
        for (int i = 0; i < requiredPageCount; i++) {
            for (int j = i * blocksPerPage, k = 0; j < (i + 1) * blocksPerPage && k < jobFile.getBlockCount(); j++, k++) {
                System.arraycopy(blocks[blockNumbers.get(j)].getData(), 0, pages[i].getData(), k * BLOCK_SIZE, BLOCK_SIZE);
            }
        }
        // 作业文件加载进内存页成为就绪进程
        pcb.setState(READY);
        Queue<PCB> readyQueue = protectedMemory.getReadyQueue();
        readyQueue.add(pcb); // 放进就绪队列
        HashMap<Integer, PCB> pcbTable = protectedMemory.getPcbTable();
        pcbTable.put(iNodeIndex, pcb);
        // 2.将进程指令封装成可执行任务提交给线程池
        int pageNumber = pageTable.get(0).getPageNumber();
        String[] strings = new String(pages[pageNumber].getData()).split("#");
        String[] instructions = Arrays.copyOf(strings, strings.length - 1);
        // 提交动作
        ProcessExecutionTask processExecutionTask = new ProcessExecutionTask(pcb, instructions);
        ExecutorService cpu = cpuSimulator.getExecutor();
        cpu.submit(processExecutionTask);
    }


}
