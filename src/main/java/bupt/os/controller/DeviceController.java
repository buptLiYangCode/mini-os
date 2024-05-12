package bupt.os.controller;

import bupt.os.common.result.Result;
import bupt.os.common.result.Results;
import bupt.os.dto.resp.DeviceQueryAllRespDTO;
import bupt.os.service.DeviceManageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Slf4j
public class DeviceController {

    private final DeviceManageService deviceManageService;

    /**
     * 查询所有设备信息
     *
     * @return info
     */
    @GetMapping("/mini-os/device/query-all")
    public Result<DeviceQueryAllRespDTO> queryAllDeviceInfo() {
        return Results.success(deviceManageService.queryAllDeviceInfo());
    }

    @PostMapping("/mini-os/device/add")
    public Result<Void> add(@RequestParam String deviceName) {
        deviceManageService.add(deviceName);
        return Results.success();
    }

    @DeleteMapping("/mini-os/device/delete")
    public Result<Void> delete(@RequestParam String deviceName) {
        deviceManageService.delete(deviceName);
        return Results.success();
    }

    @PostMapping("/mini-os/device/shutdown-all")
    public Result<Void> shutdownAll() {
        deviceManageService.shutdownAll();
        return Results.success();
    }
    @PostMapping("/mini-os/device/shutdown")
    public Result<Void> shutdown(@RequestParam String deviceName) {
        deviceManageService.shutdown(deviceName);
        return Results.success();
    }
}
