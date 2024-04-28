package bupt.os.controller;

import bupt.os.common.result.Result;
import bupt.os.common.result.Results;
import bupt.os.dto.resp.DeviceQueryAllRespDTO;
import bupt.os.service.DeviceManageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
public class DeviceController {

    private final DeviceManageService deviceManageService;
    /**
     * 查询所有设备信息
     * @return info
     */
    @GetMapping("/mini-os/device/query-all")
    public Result<DeviceQueryAllRespDTO> queryAllProcessInfo() {
        return Results.success(deviceManageService.queryAllDeviceInfo());
    }
}
