package bupt.os.runner;

import bupt.os.component.cpu.CPUSimulator;
import bupt.os.component.disk.Block;
import bupt.os.component.disk.Disk;
import bupt.os.component.filesystem.CommonFile;
import bupt.os.component.filesystem.Directory;
import bupt.os.component.filesystem.DirectoryEntry;
import bupt.os.component.filesystem.INode;
import bupt.os.component.memory.Memory;
import bupt.os.component.memory.Page;
import bupt.os.tools.DiskTool;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.concurrent.Executors;

import static bupt.os.common.constant.FileFlagConstant.RDONLY;
import static bupt.os.common.constant.FileTypeConstant.COMMON_FILE;
import static bupt.os.common.constant.FileTypeConstant.DIRECTORY;
import static bupt.os.component.disk.Disk.BLOCK_SIZE;
import static bupt.os.component.disk.Disk.TOTAL_BLOCKS;
import static bupt.os.component.memory.Memory.PAGE_SIZE;
import static bupt.os.component.memory.Memory.TOTAL_PAGES;

/**
 * 项目启动时完成对其他Component的初始化
 */
@Component
@RequiredArgsConstructor
public class MiniOsApplicationRunner implements ApplicationRunner {
    private final CPUSimulator cpuSimulator;
    private final Disk disk;
    private final Memory memory;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        // 初始化CPU
        cpuSimulator.setExecutor(Executors.newFixedThreadPool(2));
        // 初始化内存
        Page[] pages = new Page[TOTAL_PAGES];
        for (int i = 0; i < TOTAL_PAGES; i++) {
            pages[i] = new Page();
            pages[i].setData(new char[PAGE_SIZE]);
        }
        memory.setPages(pages);
        // 初始化磁盘
        disk.setINodeBitmap(new boolean[TOTAL_BLOCKS]);
        disk.setBlockBitmap(new boolean[TOTAL_BLOCKS]);
        disk.setINodes(new INode[TOTAL_BLOCKS]);
        Block[] blocks = new Block[TOTAL_BLOCKS];
        for (int i = 0; i < TOTAL_BLOCKS; i++) {
            blocks[i] = new Block();
            blocks[i].setData(new char[BLOCK_SIZE]);
        }
        disk.setBlocks(blocks);
        // 创建根目录
        Directory rootDirectory = new Directory("/", new LinkedList<>());
        // 将根目录信息存进磁盘inode[]、inodeBitMap
        disk.setINodeByIndex(2, rootDirectory); // 根目录inode号为2
        disk.setINodeBitmapByIndex(2, true);
        // 创建 home 目录，放在根目录下
        Directory homeDirectory = new Directory("home", new LinkedList<>());
        int index = DiskTool.getINodeIndex(disk, "home");
        DirectoryEntry directoryEntry = new DirectoryEntry(DIRECTORY, "home", index);
        rootDirectory.getEntries().add(directoryEntry);
        // 将 home 目录信息存进磁盘inode[]、inodeBitMap
        disk.setINodeByIndex(index, homeDirectory);
        disk.setINodeBitmapByIndex(index, true);
        // 在 home 目录下创建文件test.txt
        LinkedList<Integer> freeBlockNumbers = DiskTool.getFreeBlocks(disk, 1);
        CommonFile commonFile = new CommonFile("test.txt", RDONLY, 9, 1, freeBlockNumbers, LocalDateTime.now(), null, null);
        String data = "asdfghjkl";
        index = DiskTool.getINodeIndex(disk, "test.txt");
        directoryEntry = new DirectoryEntry(COMMON_FILE, "test.txt", index);
        homeDirectory.getEntries().add(directoryEntry);
        // 将文件信息存进磁盘inode[]、inodeBitMap、blocks[]、blockBitmap
        disk.setINodeByIndex(index, commonFile);
        disk.setINodeBitmapByIndex(index, true);
        if (freeBlockNumbers != null) {
            disk.setBlockBitmapByBlockNumbers(freeBlockNumbers, true);
            disk.setBlocksByBlockNumbers(freeBlockNumbers, data);// 随便往被占用的块写入一些数据
        }
    }

}