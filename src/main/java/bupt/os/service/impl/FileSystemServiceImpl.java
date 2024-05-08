package bupt.os.service.impl;


import bupt.os.component.filesystem.filesystem_wdh.FileSystem;
import bupt.os.service.FileSystemService;
import org.springframework.stereotype.Service;

@Service
public class FileSystemServiceImpl implements FileSystemService {

    private final FileSystem fileSystem = FileSystem.getInstance();
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
}