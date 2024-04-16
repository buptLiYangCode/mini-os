package bupt.os.runner;

import bupt.os.component.disk.Block;
import bupt.os.component.disk.Disk;
import bupt.os.component.filesystem.CommonFile;
import bupt.os.component.filesystem.Directory;
import bupt.os.component.filesystem.DirectoryEntry;
import bupt.os.component.filesystem.INode;
import bupt.os.tools.DiskTool;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.LinkedList;

import static bupt.os.common.constant.FileFlagConstant.RDONLY;
import static bupt.os.common.constant.FileTypeConstant.*;

/**
 * 项目启动时完成对其他Component的初始化
 */
@Component
@RequiredArgsConstructor
public class MiniOsApplicationRunner implements ApplicationRunner {
    private final Disk disk;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        // 初始化磁盘
        disk.setINodeBitmap(new boolean[200]);
        disk.setBlockBitmap(new boolean[200]);
        disk.setINodes(new INode[200]);
        Block[] blocks = new Block[200];
        for (int i = 0; i < blocks.length; i++) {
            blocks[i] = new Block();
            blocks[i].setData(new char[512]);
        }

        disk.setBlocks(blocks);
        // 创建根目录
        Directory rootDirectory = new Directory("/", new LinkedList<>());
        // 将根目录信息存进磁盘inode[]、inodeBitMap
        disk.setINodeByIndex(2, rootDirectory); // 根目录inode号为2
        disk.setINodeBitmapByIndex(2, true);
        // 创建 home 目录，放在根目录下
        Directory homeDirectory = new Directory("home", new LinkedList<>());
        int index = DiskTool.getFreeINode(disk, "home");
        DirectoryEntry directoryEntry = new DirectoryEntry(DIRECTORY, "home", index);
        rootDirectory.getEntries().add(directoryEntry);
        // 将 home 目录信息存进磁盘inode[]、inodeBitMap
        disk.setINodeByIndex(index, homeDirectory);
        disk.setINodeBitmapByIndex(index, true);
        // 在 home 目录下创建文件test.txt
        LinkedList<Integer> freeBlockNumbers = DiskTool.getFreeBlocks(disk, 1);
        CommonFile commonFile = new CommonFile("test.txt", RDONLY, 9, 1, freeBlockNumbers, LocalDateTime.now(), null, null);
        String data = "asdfghjkl";
        index = DiskTool.getFreeINode(disk, "test.txt");
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