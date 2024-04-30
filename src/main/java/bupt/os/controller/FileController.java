package bupt.os.controller;

import bupt.os.common.result.Result;
import bupt.os.common.result.Results;
import bupt.os.service.FileSystemService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class FileController {
    private final FileSystemService fileSystemService;


    /**
     * 在当前目录创建文件
     * @param fileName 文件名
     * @return message
     */
    @PostMapping("/mini-os/file-system/mkfile")
    public Result<String> mkfile(@RequestParam String fileName) {
        String message = fileSystemService.mkfile(fileName);
        return Results.success(message);
    }

    /**
     * 在当前目录创建子目录
     * @param directoryName 目录名
     * @return message
     */
    @PostMapping("/mini-os/file-system/mkdir")
    public Result<String> mkdir(@RequestParam String directoryName) {
        String message = fileSystemService.mkdir(directoryName);
        return Results.success(message);
    }

    /**
     * 删除目录
     * @param directoryName 目录名
     * @return message
     */
    @DeleteMapping("/mini-os/file-system/rmdir")
    public Result<String> rmdir(@RequestParam String directoryName) {
        String message = fileSystemService.rmdir(directoryName);
        return Results.success(message);
    }

    /**
     * 创建目录
     * @param fileName 文件名
     * @return message
     */
    @DeleteMapping("/mini-os/file-system/rmfile")
    public Result<String> rmfile(@RequestParam String fileName) {
        String message = fileSystemService.rmfile(fileName);
        return Results.success(message);
    }

    /**
     * 通过遍历当前节点的子节点列表，可以列出目录中的所有文件和子目录。
     * @return 文件列表
     */
    @GetMapping("/mini-os/file-system/ls")
    public Result<List<String>> ls() {
        List<String> list = fileSystemService.ls();
        return Results.success(list);
    }

    /**
     * 如果目标节点是一个文件，可以获取并返回文件的内容。
     * @return 文件内容
     */
    @GetMapping("/mini-os/file-system/cat")
    public Result<String> cat() {
        String contents = fileSystemService.cat();
        return Results.success(contents);
    }

    /**
     * 递归遍历文件系统的节点，并返回一个表示文件结构的字符串。
     * @return 文件结构字符串
     */
    @GetMapping("/mini-os/file-system/struct")
    public Result<String> struct() {
        String struct = fileSystemService.struct();
        return Results.success(struct);
    }

}
