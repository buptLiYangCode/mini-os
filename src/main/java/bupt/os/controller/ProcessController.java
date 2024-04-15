package bupt.os.controller;

import bupt.os.dto.req.ProcessCreateReqDTO;
import bupt.os.common.result.Result;
import bupt.os.common.result.Results;
import bupt.os.service.ProcessManageService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ProcessController {

    private final ProcessManageService processManageService;

    /**
     * 创建进程
     * @param processCreateReqDTO 创建请求
     * @return ok
     */
    @PostMapping("/mini-os/process/create")
    public Result<Void> createProcess(@RequestBody ProcessCreateReqDTO processCreateReqDTO) {
        processManageService.createProcess(processCreateReqDTO);
        return Results.success();
    }

    /**
     * 执行进程
     * @param processName 进程名
     * @return ok
     */
    @PostMapping("/mini-os/process/execute")
    public Result<Void> executeProcess(@RequestParam String processName) {
        processManageService.executeProcess(processName);
        return Results.success();
    }
}
