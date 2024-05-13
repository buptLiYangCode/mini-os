package bupt.os.runner;

import bupt.os.component.cpu.CPUSimulator;
import bupt.os.component.device.DevicesSimulator;
import bupt.os.component.filesystem.FileNode;
import bupt.os.component.filesystem.FileSystem;
import bupt.os.component.interrupt.InterruptRequestLine;
import bupt.os.component.memory.protected_.DeviceInfo;
import bupt.os.component.memory.protected_.PCB;
import bupt.os.component.memory.protected_.ProtectedMemory;
import bupt.os.component.memory.user.MemoryManagement;
import bupt.os.component.memory.user.MemoryManagementImpl;
import bupt.os.component.scheduler.ProcessScheduler;
import bupt.os.tools.CommonTool;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

import static bupt.os.common.constant.DeviceStateConstant.DEVICE_READY;
import static bupt.os.common.constant.ProcessStateConstant.CREATED;

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
    private final ConcurrentHashMap<Long, InterruptRequestLine> irlTable = protectedMemory.getIrlTable();


    @Override
    public void run(ApplicationArguments args) throws Exception {
        ExecutorService cpuSimulatorExecutor = cpuSimulator.getExecutor();
        for (int i = 0; i < 4; i++) {
            cpuSimulatorExecutor.submit(() -> {
                irlTable.put(Thread.currentThread().getId(), new InterruptRequestLine());
                try {
                    Thread.sleep(10);
                    System.out.println("初始化" + Thread.currentThread().getId());
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

        // 初始化文件
        String[] inst1 = {
                "M 4",
                "C 1000",
                "A 10240",
                "C 1000",
                "A 5120",
                "C 1000",
                "A 7168",
                "C 1000",
                "A 8192",
                "C 1000",
                "A 5120",
                "C 1000",
                "A 9216",
                "C 1000",
                "A 7168",
                "C 1000",
                "A 1024",
                "C 1000",
                "A 2048",
                "C 1000",
                "A 2048",
                "C 1000",
                "A 3072",
                "C 1000",
                "A 4096",
                "C 1000",
                "A 6144",
                "C 1000",
                "A 6144",
                "C 1000",
                "A 6144",
                "C 1000",
                "A 5120",
                "C 1000",
                "A 4096",
                "C 1000",
                "A 2048",
                "C 1000",
                "A 1024",
                "C 1000",
                "A 1024",
                "C 1000",
                "A 3072",
                "C 1000",
                "A 2048",
                "C 1000",
                "A 4096",
                "C 1000",
                "A 7168",
                "C 1000",
                "A 9216",
                "C 1000",
                "A 2048",
                "C 1000",
                "A 6144",
                "C 1000",
                "A 5120",
                "C 1000",
                "A 1024",
                "C 1000",
                "A 9216",
                "C 1000",
                "A 2048",
                "C 1000",
                "A 6144",
                "C 1000",
                "A 5120",
                "C 1000",
                "A 1024",
                "C 1000",
                "A 9216",
                "C 1000",
                "A 2048",
                "C 1000",
                "A 6144",
                "C 1000",
                "A 5120",
                "C 1000",
                "A 1024",
                "C 1000",
                "A 9216",
                "C 1000",
                "A 2048",
                "C 1000",
                "A 4096",
                "Q"
        };
        String[] inst2 = {
                "M 10",
                "C 500",
                "A 1024",
                "C 1000",
                "A 6000",
                "C 1000",
                "A 1001",
                "C 1000",
                "A 2024",
                "C 1000",
                "A 2024",
                "C 1000",
                "A 1024",
                "C 1000",
                "A 1000",
                "C 1000",
                "A 8001",
                "C 1000",
                "A 4024",
                "C 1000",
                "A 2024",
                "C 1000",
                "A 1024",
                "C 1000",
                "D K1 2000",
                "D K2 2000",
                "C 500",
                "D K1 1000",
                "D K2 2000",
                "A 1",
                "A 0",
                "C 500",
                "D P1 600",
                "Q"
        };
        String[] inst3 = {
                "M 10",
                "C 1000",
                "D K1 2000",
                "D K2 2000",
                "C 500",
                "D K1 1000",
                "D K2 2000",
                "A 1",
                "A 0",
                "C 500",
                "D P1 600",
                "Q"
        };
        HashMap<Integer, PCB> pcbTable = protectedMemory.getPcbTable();
        int pid1 = CommonTool.getPid("process1");
        int pid2 = CommonTool.getPid("process2");
        int pid3 = CommonTool.getPid("process3");


        // 创建进程pcb，放进pcbTable
        PCB pcb1 = new PCB(pid1, "process1", 0, -1, CREATED, -1, -1, -1, 3, inst1);
        PCB pcb2 = new PCB(pid2, "process2", 0, -1, CREATED, -1, -1, -1, 3, inst2);
        PCB pcb3 = new PCB(pid3, "process3", 0, -1, CREATED, -1, -1, -1, 3, inst3);
        pcbTable.put(pid1, pcb1);
        pcbTable.put(pid2, pcb2);
        pcbTable.put(pid3, pcb3);

        // 写进文件系统
        fileSystem.touch("process1");
        FileNode file1 = fileSystem.getFile("process1");
        StringBuilder sb1 = new StringBuilder();
        for (String s : inst1) {
            sb1.append(s).append("#");
        }
        file1.setContent(sb1.toString());

        // 写进文件系统
        fileSystem.touch("process2");
        FileNode file2 = fileSystem.getFile("process2");
        StringBuilder sb2 = new StringBuilder();
        for (String s : inst2) {
            sb2.append(s).append("#");
        }
        file2.setContent(sb2.toString());


        // 写进文件系统
        fileSystem.touch("process3");
        FileNode file3 = fileSystem.getFile("process3");
        StringBuilder sb3 = new StringBuilder();
        for (String s : inst3) {
            sb3.append(s).append("#");
        }
        file3.setContent(sb3.toString());
    }

}