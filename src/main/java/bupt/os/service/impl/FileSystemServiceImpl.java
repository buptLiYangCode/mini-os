package bupt.os.service.impl;


import bupt.os.component.filesystem.filesystem_wdh.FileNode;
import bupt.os.component.filesystem.filesystem_wdh.FileSystem;
import bupt.os.component.memory.ly.FileInfoo;
import bupt.os.component.memory.ly.ProtectedMemory;
import bupt.os.service.FileSystemService;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

@Service
public class FileSystemServiceImpl implements FileSystemService {

    private final FileSystem fileSystem = FileSystem.getInstance();
    private final ProtectedMemory protectedMemory = ProtectedMemory.getInstance();
    @Override
    public String dealWithInst(String inst) {
        String[] strings = inst.split(" ");

        return switch (strings[0]) {
            case "cd" -> fileSystem.cd(strings[1]);
            case "ls" -> fileSystem.ls();
            case "cat" -> fileSystem.cat(strings[1]);
            case "touch" -> fileSystem.touch(strings[1]);
            case "makedir" -> fileSystem.makedir(strings[1]);
            case "rmfile" -> fileSystem.rmfile(strings[1]);
            case "rmdir" -> fileSystem.rmdir(strings[1]);
            case "nowTree" -> fileSystem.nowTree();
            // case "printFileTree" -> fileSystem.getStruct();
            // ...
            default -> "无法识别的指令";
        };
    }

    @Override
    public String printFileTree() {
        return fileSystem.fileTree();
    }

    @Override
    public FileNode getFile(String filePath) {
        return fileSystem.getFile(filePath);
    }

    @Override
    public List<FileInfoo> fileSharedAccess() {
        HashMap<FileNode, FileInfoo> fileInfoTable = protectedMemory.getFileInfoTable();
        Set<FileNode> fileNodes = fileInfoTable.keySet();
        List<FileInfoo> list = new LinkedList<>();
        for (FileNode fileNode : fileNodes) {
            list.add(fileInfoTable.get(fileNode));
        }
        return list;
    }
}