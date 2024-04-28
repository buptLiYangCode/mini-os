package bupt.os.controller;

import bupt.os.common.result.Result;
import bupt.os.common.result.Results;
import bupt.os.dto.resp.MemoryQueryAllRespDTO;
import bupt.os.service.StorageManageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
public class StorageController {
    private final StorageManageService storageManageService;

    /**
     * 查询所有进程信息
     * @return info
     */
    @GetMapping("/mini-os/memory/query-all")
    public Result<MemoryQueryAllRespDTO> queryAllMemoryInfo() {
        return Results.success(storageManageService.queryAllMemoryInfo());
    }
}
