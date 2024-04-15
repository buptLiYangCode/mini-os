package bupt.os.service.impl;


import bupt.os.component.disk.Disk;
import bupt.os.component.filesystem.CommonFile;
import bupt.os.dto.req.ProcessCreateReqDTO;
import bupt.os.service.ProcessManageService;
import bupt.os.tools.DiskTool;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.LinkedList;

import static bupt.os.common.constant.InstructionConstant.M;
import static bupt.os.component.disk.Disk.BLOCK_SIZE;

@Service
@RequiredArgsConstructor
public class ProcessManageServiceImpl implements ProcessManageService {

    private final Disk disk;

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

    }

    /**
     * 执行进程
     * @param processName 进程名
     */
    @Override
    public void executeProcess(String processName) {

    }
}
