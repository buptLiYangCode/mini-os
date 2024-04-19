package bupt.os.component.device;

import lombok.Data;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 模拟多个设备，每个设备是一个固定大小为1的线程池
 */
@Data
public class DevicesSimulator {
    private static DevicesSimulator instance;
    private final ConcurrentHashMap<String, ExecutorService> deviceExecutors = new ConcurrentHashMap<>();

    private DevicesSimulator() {

    }

    public static synchronized DevicesSimulator getInstance() {
        if (instance == null) {
            instance = new DevicesSimulator();
        }
        return instance;
    }

    // 添加一个新设备到线程池
    public void addDevice(String deviceName) {
        deviceExecutors.computeIfAbsent(deviceName, k -> Executors.newSingleThreadExecutor());
    }

    // 提交一个任务到指定设备
    public void submitTask(String deviceName, Runnable task) {
        ExecutorService executor = deviceExecutors.get(deviceName);
        if (executor != null) {
            executor.submit(task);
        } else {
            System.out.println("设备不存在: " + deviceName);
        }
    }

    // 关闭特定设备的线程
    public void shutdownDevice(String deviceName) {
        ExecutorService executor = deviceExecutors.remove(deviceName);
        if (executor != null) {
            executor.shutdown();
        }
    }

    // 关闭所有设备
    public void shutdownAll() {
        for (Map.Entry<String, ExecutorService> entry : deviceExecutors.entrySet()) {
            entry.getValue().shutdown();
        }
    }
}

