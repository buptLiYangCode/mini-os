package bupt.os.component.device;

import bupt.os.component.interrupt.InterruptRequestLine;
import bupt.os.component.memory.protected_.DeviceInfo;
import bupt.os.component.memory.protected_.IoRequest;
import bupt.os.component.memory.protected_.ProtectedMemory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.LinkedList;

import static bupt.os.common.constant.DeviceStateConstant.DEVICE_READY;
import static bupt.os.common.constant.DeviceStateConstant.DEVICE_WORKING;
import static java.lang.Thread.sleep;

/**
 * 每隔设备都有一个设备驱动程序，定期检查它管理的设备队列
 * mini-os模拟时，DeviceDrivers会定时检查所有设备队列
 */
@Component
@Slf4j
public class DeviceDrivers {
    private final ProtectedMemory protectedMemory = ProtectedMemory.getInstance();
    private final DevicesSimulator devicesSimulator = DevicesSimulator.getInstance();

    private final HashMap<Long, InterruptRequestLine> irlTable = protectedMemory.getIrlTable();


    @Scheduled(fixedRate = 100) // 每隔100ms执行一次
    public void checkAllDeviceQueue() {
        // 获取设备信息表
        LinkedList<DeviceInfo> deviceInfoTable = protectedMemory.getDeviceInfoTable();
        for (DeviceInfo deviceInfo : deviceInfoTable) {
            IoRequest peekIoRequest = deviceInfo.getIoRequestQueue().peek();
            if (deviceInfo.getDeviceState().equals(DEVICE_READY) && peekIoRequest != null) {
                IoRequest ioRequest = deviceInfo.getIoRequestQueue().poll();
                deviceInfo.setPid(ioRequest.getPcb().getPid());
                // 将io请求发送给对应设备处理
                devicesSimulator.submitTask(deviceInfo.getDeviceName(), () -> {
                    try {
                        deviceInfo.setDeviceState(DEVICE_WORKING);
                        try {
                            sleep(ioRequest.getUseTime());
                        } catch (InterruptedException e) {
                            System.out.println(deviceInfo.getDeviceName() + "出现问题");
                        }
                        log.info(deviceInfo.getDeviceName() + "工作" + ioRequest.getUseTime() + "ms");
                        deviceInfo.setDeviceState(DEVICE_READY);
                        // 硬件设备发送IO操作完成 中断信号 到irl
                        InterruptRequestLine irl = irlTable.get(ioRequest.getThreadId());
                        irl.offer("IO_INTERRUPT-" + ioRequest.getPcb().getPid());
                        System.out.println("IRL+++" + irl.peek());

                        System.out.println(irlTable);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }
        }
    }
}