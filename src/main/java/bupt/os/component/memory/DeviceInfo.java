package bupt.os.component.memory;

import lombok.Data;

import java.util.LinkedList;

@Data
public class DeviceInfo {
    // 设备id
    private int did;
    // 设备名
    private String deviceName;
    // 设备类型
    private String deviceType;
    // 设备状态
    private String deviceState;
    // 进程发送的IO请求队列
    private LinkedList<IoRequest> ioRequestQueue;
}
