package bupt.os.component.device;

import bupt.os.component.memory.DeviceInfo;
import bupt.os.component.memory.IoRequest;
import bupt.os.component.memory.ProtectedMemory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.LinkedList;

import static bupt.os.common.constant.DeviceState.DEVICE_READY;
import static bupt.os.common.constant.DeviceState.DEVICE_WORKING;
import static java.lang.Thread.sleep;

/**
 * 每隔设备都有一个设备驱动程序，定期检查它管理的设备队列
 * mini-os模拟时，DeviceDrivers会定时检查所有设备队列
 */
@Component
public class DeviceDrivers {
    private final LinkedList<DeviceInfo> deviceInfoTable = ProtectedMemory.getInstance().getDeviceInfoTable();
    private final DevicesSimulator devicesSimulator = DevicesSimulator.getInstance();

    @Scheduled(fixedRate = 100) // 每隔100ms执行一次
    public void checkAllDeviceQueue() {
        for (DeviceInfo deviceInfo : deviceInfoTable) {
            IoRequest ioRequest = deviceInfo.getIoRequestQueue().peek();
            if (deviceInfo.getDeviceState().equals(DEVICE_READY) && ioRequest != null) {
                // 将io请求发送给对应设备处理
                devicesSimulator.submitTask(deviceInfo.getDeviceName(), () -> {
                    try {
                        sleep(ioRequest.getUseTime());
                    } catch (InterruptedException e) {
                        System.out.println(deviceInfo.getDeviceName() + "出现问题");
                    }
                });
                deviceInfo.setDeviceState(DEVICE_WORKING);
            }
        }
    }
}