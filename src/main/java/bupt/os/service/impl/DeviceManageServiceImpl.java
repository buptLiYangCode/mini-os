package bupt.os.service.impl;

import bupt.os.component.device.DevicesSimulator;
import bupt.os.component.memory.protected_.DeviceInfo;
import bupt.os.component.memory.protected_.ProtectedMemory;
import bupt.os.dto.resp.DeviceQueryAllRespDTO;
import bupt.os.service.DeviceManageService;
import bupt.os.tools.CommonTool;
import org.springframework.stereotype.Service;

import java.util.LinkedList;

import static bupt.os.common.constant.DeviceStateConstant.DEVICE_READY;

@Service
public class DeviceManageServiceImpl implements DeviceManageService {

    private final ProtectedMemory protectedMemory = ProtectedMemory.getInstance();
    private final DevicesSimulator devicesSimulator = DevicesSimulator.getInstance();

    private final LinkedList<DeviceInfo> deviceInfoTable = protectedMemory.getDeviceInfoTable();


    @Override
    public DeviceQueryAllRespDTO queryAllDeviceInfo() {
        return new DeviceQueryAllRespDTO(deviceInfoTable);
    }

    @Override
    public void add(String deviceName) {
        devicesSimulator.addDevice(deviceName);
        deviceInfoTable.add(new DeviceInfo(CommonTool.getPid(deviceName), deviceName, deviceName.charAt(0)+"", DEVICE_READY, -1, new LinkedList<>()));
    }

    @Override
    public void delete(String deviceName) {
        devicesSimulator.shutdownDevice(deviceName);
        for (int i = 0; i < deviceInfoTable.size(); i++) {
            if(deviceInfoTable.get(i).getDeviceName().equals(deviceName)){
                deviceInfoTable.remove(i);
                break;
            }
        }
    }

    @Override
    public void shutdownAll() {
        devicesSimulator.shutdownAll();
    }

    @Override
    public void shutdown(String deviceName) {
        devicesSimulator.shutdownDevice(deviceName);
    }
}
