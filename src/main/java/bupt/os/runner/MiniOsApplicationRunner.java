package bupt.os.runner;

import bupt.os.component.cpu.CPUSimulator;
import bupt.os.component.device.DevicesSimulator;
import bupt.os.component.filesystem.filesystem_ly.Disk;
import bupt.os.component.filesystem.filesystem_wdh.FileSystem;
import bupt.os.component.memory.ly.DeviceInfo;
import bupt.os.component.memory.ly.ProtectedMemory;
import bupt.os.component.memory.lyq.MemoryManagement;
import bupt.os.component.memory.lyq.MemoryManagementImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.LinkedList;

import static bupt.os.common.constant.DeviceStateConstant.DEVICE_READY;

/**
 * 项目启动时完成对其他Component的初始化
 */
@Component
@RequiredArgsConstructor
public class MiniOsApplicationRunner implements ApplicationRunner {
    private final CPUSimulator cpuSimulator = CPUSimulator.getInstance();
    private final Disk disk = Disk.getInstance();
    private final ProtectedMemory protectedMemory = ProtectedMemory.getInstance();
    private final DevicesSimulator devicesSimulator = DevicesSimulator.getInstance();
    // 文件系统初始化
    private final FileSystem fileSystem = FileSystem.getInstance();

    @Override
    public void run(ApplicationArguments args) throws Exception {

        // 内存初始化
        MemoryManagementImpl memoryManagement = new MemoryManagementImpl();
        MemoryManagementImpl.setMode(MemoryManagement.LRU);
        memoryManagement.InitMemory();

        // 添加默认设备
        devicesSimulator.addDevice("K1");
        devicesSimulator.addDevice("K2");
        devicesSimulator.addDevice("K3");
        devicesSimulator.addDevice("P1");

        LinkedList<DeviceInfo> deviceInfoTable = protectedMemory.getDeviceInfoTable();
        DeviceInfo deviceInfo1 = new DeviceInfo(1, "K1", "K", DEVICE_READY, new LinkedList<>());
        deviceInfoTable.add(deviceInfo1);
        DeviceInfo deviceInfo2 = new DeviceInfo(2, "K2", "K", DEVICE_READY, new LinkedList<>());
        deviceInfoTable.add(deviceInfo2);
        DeviceInfo deviceInfo3 = new DeviceInfo(3, "K3", "K", DEVICE_READY, new LinkedList<>());
        deviceInfoTable.add(deviceInfo3);
        DeviceInfo deviceInfo4 = new DeviceInfo(4, "P1", "P", DEVICE_READY, new LinkedList<>());
        deviceInfoTable.add(deviceInfo4);

    }

}