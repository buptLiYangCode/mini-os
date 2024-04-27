package bupt.os.controller;

import bupt.os.common.result.Result;
import bupt.os.common.result.Results;
import bupt.os.dto.req.ProcessCreateReqDTO;
import bupt.os.dto.req.ProcessExecuteReqDTO;
import bupt.os.dto.resp.ProcessQueryAllRespDTO;
import bupt.os.service.ProcessManageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
public class ProcessController {

    private final ProcessManageService processManageService;

    /**
     * 创建进程
     *
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
     *
     * @param processExecuteReqDTO 进程名
     * @return ok
     */
    @PostMapping("/mini-os/process/execute")
    public Result<Void> executeProcess(@RequestBody ProcessExecuteReqDTO processExecuteReqDTO) {
        String processName = processExecuteReqDTO.getProcessName();
        log.info(processName + "----------------------------");
        processManageService.executeProcess(processName);
        return Results.success();
    }

    /**
     * 查询所有进程信息
     * @return info
     */
    @GetMapping("/mini-os/process/query-all")
    public Result<ProcessQueryAllRespDTO> queryAllProcessInfo() {
        return Results.success(processManageService.queryAllProcessInfo());
    }
}
