package bupt.os.service;

import bupt.os.component.filesystem.FileNode;
import bupt.os.component.memory.protected_.FileInfoo;

import java.util.List;

public interface FileSystemService {

    String dealWithInst(String inst);

    String printFileTree();

    FileNode getFile(String filePath);

    List<FileInfoo> fileSharedAccess();
}
