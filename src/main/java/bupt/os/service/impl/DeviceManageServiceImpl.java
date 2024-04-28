package bupt.os.service.impl;

import bupt.os.component.memory.ProtectedMemory;
import bupt.os.dto.resp.DeviceQueryAllRespDTO;
import bupt.os.service.DeviceManageService;
import org.springframework.stereotype.Service;

@Service
public class DeviceManageServiceImpl implements DeviceManageService {

    private final ProtectedMemory protectedMemory = ProtectedMemory.getInstance();
    @Override
    public DeviceQueryAllRespDTO queryAllDeviceInfo() {
        return new DeviceQueryAllRespDTO(protectedMemory.getDeviceInfoTable());
    }
}
