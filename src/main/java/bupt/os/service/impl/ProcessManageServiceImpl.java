package bupt.os.service.impl;


import bupt.os.component.cpu.CPUSimulator;
import bupt.os.component.cpu.ProcessExecutionTask;
import bupt.os.component.disk.Block;
import bupt.os.component.disk.Disk;
import bupt.os.component.filesystem.CommonFile;
import bupt.os.component.memory.Memory;
import bupt.os.component.memory.Page;
import bupt.os.component.memory.PageTableEntry;
import bupt.os.component.process.PCB;
import bupt.os.dto.req.ProcessCreateReqDTO;
import bupt.os.service.ProcessManageService;
import bupt.os.tools.DiskTool;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;

import static bupt.os.common.constant.InstructionConstant.M;
import static bupt.os.common.constant.ProcessStateConstant.CREATED;
import static bupt.os.common.constant.ProcessStateConstant.READY;
import static bupt.os.component.disk.Disk.BLOCK_SIZE;
import static bupt.os.component.memory.Memory.PAGE_SIZE;

@Service
@RequiredArgsConstructor
public class ProcessManageServiceImpl implements ProcessManageService {

    private final CPUSimulator cpuSimulator;
    private final Disk disk;
    private final Memory memory;

    /**
     * 创建进程
     * @param processCreateReqDTO 创建请求
     */
    @Override
    public void createProcess(ProcessCreateReqDTO processCreateReqDTO) {
        CommonFile commonFile = new CommonFile();
        commonFile.setFileName(processCreateReqDTO.getProcessName());
        String[] instructions = processCreateReqDTO.getInstructions();
        // 对于M 指令，会初始化作业文件的inode
        String[] strings1 = Arrays.stream(instructions)
                .filter(inst -> M.equals(inst.charAt(0) + ""))
                .toArray(String[]::new);
        String MInst = strings1[0];
        String[] parts = MInst.split(" ");
        int blockCount = Integer.parseInt(parts[1]);
        commonFile.setSize(blockCount * BLOCK_SIZE);
        commonFile.setBlockCount(blockCount);
        LinkedList<Integer> freeBlockNumbers = DiskTool.getFreeBlocks(disk, blockCount);
        commonFile.setBlockNumbers(freeBlockNumbers);
        // 其他指令会写入分配给文件的block块中，从第一个块开始写
        String[] otherInst = Arrays.stream(instructions)
                .filter(inst -> !M.equals(inst.charAt(0) + ""))
                .toArray(String[]::new);
        Integer firstBlock = commonFile.getBlockNumbers().get(0);
        char[] data = disk.getBlocks()[firstBlock].getData();
        StringBuilder sb= new StringBuilder();
        for (String inst : otherInst) {
            // #表示换行符
            sb.append(inst).append("#");
        }
        char[] arr = sb.toString().toCharArray();
        System.arraycopy(arr, 0, data, 0, arr.length);
        // 初始化文件创建时间
        commonFile.setCreateTime(LocalDateTime.now());
        // 将文件信息存进磁盘inode[]、inodeBitMap、blocks[]、blockBitmap
        int index = DiskTool.getINodeIndex(disk, processCreateReqDTO.getProcessName());
        disk.setINodeByIndex(index, commonFile);
        disk.setINodeBitmapByIndex(index, true);
        if (freeBlockNumbers != null) {
            disk.setBlockBitmapByBlockNumbers(freeBlockNumbers, true);
        }
    }
    /**
     * 执行进程
     * @param processName 进程名
     */
    @Override
    public void executeProcess(String processName) {
        // 1.先把作业文件加载进内存
        // 计算作业文件inode号
        int iNodeIndex = DiskTool.getINodeIndex(disk, processName) - 1;
        CommonFile jobFile = (CommonFile) disk.getINodes()[iNodeIndex];
        // 初始化PCB
        PCB pcb = new PCB(iNodeIndex, processName, 0, jobFile.getSize(), CREATED, null, null, new LinkedList<>());
        // 计算进程所需页数
        int pagesNeeded = jobFile.getSize() / PAGE_SIZE + 1;
        // lru算法获得进程可以使用的页号
        LinkedList<Integer> pageNumbers = LRU(pagesNeeded);
        // 初始化pcb页表
        LinkedList<PageTableEntry> pageTable = pcb.getPageTable();
        for (int i = 0; i < pagesNeeded; i++) {
            Integer ppn = pageNumbers.get(i); // PhysicalPageNumber
            PageTableEntry pageTableEntry = new PageTableEntry(true, false, false, ppn);
            pageTable.add(pageTableEntry);
        }
        // 磁盘块中作业文件复制进内存页
        int blocksPerPage = PAGE_SIZE / BLOCK_SIZE;
        LinkedList<Integer> blockNumbers = jobFile.getBlockNumbers();
        Page[] pages = memory.getPages();
        Block[] blocks = disk.getBlocks();
        for (int i = 0; i < pagesNeeded; i++) {
            for (int j = i * blocksPerPage, k = 0; j < (i + 1) * blocksPerPage && k < jobFile.getBlockCount(); j++, k++) {
                System.arraycopy(blocks[blockNumbers.get(j)].getData(), 0, pages[i].getData(), k * BLOCK_SIZE, BLOCK_SIZE);
            }
        }
        // 作业文件加载进内存页成为就绪进程
        pcb.setState(READY);
        // 2.将进程封装成可执行任务提交给线程池
        int pageNumber = pageTable.get(0).getPageNumber();
        String[] strings = new String(pages[pageNumber].getData()).split("#");
        String[] instructions = Arrays.copyOf(strings, strings.length - 1);
        ProcessExecutionTask processExecutionTask = new ProcessExecutionTask(processName, instructions);
        ExecutorService cpu = cpuSimulator.getExecutor();
        cpu.execute(processExecutionTask);
    }


    private LinkedList<Integer> LRU(int pagesNeeded) {
        LinkedList<Integer> pageNumbers = new LinkedList<>();
        for (int i = 0; i < pagesNeeded; i++) {
            pageNumbers.add(i);
        }
        return pageNumbers;
    }
}
