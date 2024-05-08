package bupt.os.controller;

import bupt.os.common.result.Result;
import bupt.os.common.result.Results;
import bupt.os.component.filesystem.filesystem_wdh.FileNode;
import bupt.os.component.memory.ly.FileInfoo;
import bupt.os.service.FileSystemService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class FileController {
    private final FileSystemService fileSystemService;

    @PostMapping("/mini-os/file-system/user-inst")
    public Result<String> dealWithInst(@RequestParam String inst) {
        String message = fileSystemService.dealWithInst(inst);
        return Results.success(message);
    }

    @GetMapping("/mini-os/file-system/file-tree")
    public Result<String> printFileTree() {
        String message = fileSystemService.printFileTree();
        return Results.success(message);
    }

    @GetMapping("/mini-os/file-system/file")
    public Result<FileNode> getFile(@RequestParam String filePath) {
        return Results.success(fileSystemService.getFile(filePath));
    }

    /**
     * 文件的互斥访问
     * @return 作为可访问资源的文件
     */
    @GetMapping("/mini-os/file-system/file-shared-access")
    public Result<List<FileInfoo>> fileSharedAccess() {
        return Results.success(fileSystemService.fileSharedAccess());
    }

}
