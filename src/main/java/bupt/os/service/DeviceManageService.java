package bupt.os.service;

import bupt.os.dto.resp.DeviceQueryAllRespDTO;

public interface DeviceManageService {
    DeviceQueryAllRespDTO queryAllDeviceInfo();

    void add(String deviceName);

    void delete(String deviceName);

    void shutdownAll();

    void shutdown(String deviceName);
}
