package bupt.os.runner;

import bupt.os.component.cpu.CPUSimulator;
import bupt.os.component.device.DevicesSimulator;
import bupt.os.component.filesystem.FileSystem;
import bupt.os.component.interrupt.InterruptRequestLine;
import bupt.os.component.memory.protected_.DeviceInfo;
import bupt.os.component.memory.protected_.ProtectedMemory;
import bupt.os.component.memory.user.MemoryManagement;
import bupt.os.component.memory.user.MemoryManagementImpl;
import bupt.os.component.scheduler.ProcessScheduler;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;

import static bupt.os.common.constant.DeviceStateConstant.DEVICE_READY;

/**
 * 项目启动时完成对其他Component的初始化
 */
@Component
@RequiredArgsConstructor
public class MiniOsApplicationRunner implements ApplicationRunner {
    private final CPUSimulator cpuSimulator = CPUSimulator.getInstance();
    private final ProtectedMemory protectedMemory = ProtectedMemory.getInstance();
    private final DevicesSimulator devicesSimulator = DevicesSimulator.getInstance();
    private final FileSystem fileSystem = FileSystem.getInstance();
    // 初始化表
    private final HashMap<Long, InterruptRequestLine> irlTable = protectedMemory.getIrlTable();


    @Override
    public void run(ApplicationArguments args) throws Exception {
        ExecutorService cpuSimulatorExecutor = cpuSimulator.getExecutor();
        for (int i = 0; i < 4; i++) {
            cpuSimulatorExecutor.submit(() -> {
                irlTable.put(Thread.currentThread().getId(), new InterruptRequestLine());
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
        }
        // 让CPU4个核心都运行空线程
        for (int i = 0; i < 4; i++) {
            ProcessScheduler.spanWait(cpuSimulatorExecutor);
        }
        // 内存初始化
        MemoryManagementImpl memoryManagement = new MemoryManagementImpl();
        MemoryManagementImpl.setMode(MemoryManagement.MODE_LFU);
        memoryManagement.InitMemory();

        // 添加默认设备
        devicesSimulator.addDevice("K1");
        devicesSimulator.addDevice("K2");
        devicesSimulator.addDevice("K3");
        devicesSimulator.addDevice("P1");

        LinkedList<DeviceInfo> deviceInfoTable = protectedMemory.getDeviceInfoTable();
        DeviceInfo deviceInfo1 = new DeviceInfo(1, "K1", "K", DEVICE_READY, -1, new LinkedList<>());
        deviceInfoTable.add(deviceInfo1);
        DeviceInfo deviceInfo2 = new DeviceInfo(2, "K2", "K", DEVICE_READY, -1, new LinkedList<>());
        deviceInfoTable.add(deviceInfo2);
        DeviceInfo deviceInfo3 = new DeviceInfo(3, "K3", "K", DEVICE_READY, -1, new LinkedList<>());
        deviceInfoTable.add(deviceInfo3);
        DeviceInfo deviceInfo4 = new DeviceInfo(4, "P1", "P", DEVICE_READY, -1, new LinkedList<>());
        deviceInfoTable.add(deviceInfo4);

    }

}