package bupt.os.service.impl;


import bupt.os.component.disk.Disk;
import bupt.os.component.filesystem.CommonFile;
import bupt.os.component.filesystem.Directory;
import bupt.os.component.filesystem.DirectoryEntry;
import bupt.os.service.FileSystemService;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Optional;

import static bupt.os.common.constant.FileTypeConstant.COMMON_FILE;
import static bupt.os.common.constant.FileTypeConstant.DIRECTORY;
import static bupt.os.component.disk.Disk.BLOCK_SIZE;

@Service
public class FileSystemServiceImpl implements FileSystemService {
    private final Disk disk = Disk.getInstance();

    @Override
    public String getFileData(String path) {
        String[] strings = path.split("/");
        strings = Arrays.stream(strings)
                .filter(s -> !s.equals(""))
                .toArray(String[]::new);
        Directory rootDirectory = (Directory) disk.getINodes()[2];
        LinkedList<DirectoryEntry> directoryEntries = rootDirectory.getEntries();
        for (int i = 0; i < strings.length - 1; i++) {
            String finalFileName = strings[i];
            Optional<Integer> first = directoryEntries.stream()
                    .filter(entry -> entry.getFileType().equals(DIRECTORY))
                    .filter(entry -> entry.getFilename().equals(finalFileName))
                    .map(DirectoryEntry::getINodeNumber)
                    .findFirst();
            int iNodeNumber = -1;
            if (first.isPresent())
                iNodeNumber = first.get();
            else
                System.out.println("文件路径错误");
            Directory directory = (Directory) disk.getINodes()[iNodeNumber];
            directoryEntries = directory.getEntries();
        }
        String fileName = strings[strings.length - 1];
        Optional<Integer> first = directoryEntries.stream()
                .filter(entry -> entry.getFileType().equals(COMMON_FILE))
                .filter(entry -> entry.getFilename().equals(fileName))
                .map(DirectoryEntry::getINodeNumber)
                .findFirst();
        int iNodeNumber = -1;
        if (first.isPresent())
            iNodeNumber = first.get();
        else
            System.out.println("文件不存在");
        CommonFile commonFile = (CommonFile) disk.getINodes()[iNodeNumber];
        StringBuilder sb = new StringBuilder();

        LinkedList<Integer> blockNumbers = commonFile.getBlockNumbers();
        int blockCount = commonFile.getBlockCount();
        // 一个文件占n个块，前n-1个块不会有 “”
        for (int i = 0; i < blockCount - 1; i++) {
            char[] data = disk.getBlocks()[blockNumbers.get(i)].getData();
            sb.append(Arrays.toString(data));
        }
        // 最后一个块中会有大量 “”
        char[] data = disk.getBlocks()[blockNumbers.get(blockCount - 1)].getData();
        sb.append(new String(data, 0, commonFile.getSize() % BLOCK_SIZE));
        return sb.toString();
    }
}