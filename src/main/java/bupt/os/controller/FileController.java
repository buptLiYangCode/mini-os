package bupt.os.controller;

import bupt.os.common.result.Result;
import bupt.os.common.result.Results;
import bupt.os.service.FileSystemService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class FileController {
    private final FileSystemService fileSystemService;

    @PostMapping("/mini-os/file-system")
    public Result<String> dealWithInst(@RequestParam String inst) {
        String message = fileSystemService.dealWithInst(inst);
        return Results.success(message);
    }

}
