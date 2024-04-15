package bupt.os.controller;

import bupt.os.common.result.Result;
import bupt.os.common.result.Results;
import bupt.os.service.FileSystemService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class FileController {
    private final FileSystemService fileSystemService;
    @GetMapping("mini-os/file")
    public Result<String> getFileData(@RequestParam String path) {
        String fileData = fileSystemService.getFileData(path);
        return Results.success(fileData);
    }
}
