package bupt.os.component.cpu;

import lombok.Data;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

@Data
public class CPUSimulator {
    private static volatile CPUSimulator instance;

    // 创建固定大小为x的线程池
    private ExecutorService executor;
    public int cpuCount;


    // 私有构造函数，防止外部实例化
    private CPUSimulator() {
        executor = Executors.newFixedThreadPool(4);
        cpuCount = ((ThreadPoolExecutor) executor).getCorePoolSize();
    }

    // 获取单例实例的静态方法
    public static CPUSimulator getInstance() {
        if (instance == null) {
            synchronized (CPUSimulator.class) {
                if (instance == null) {
                    instance = new CPUSimulator();
                }
            }
        }
        return instance;
    }
}
