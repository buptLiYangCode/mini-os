package bupt.os.component.cpu;

import lombok.Data;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


@Data
public class CPUSimulator {
    private static volatile CPUSimulator instance;

    // 创建固定大小为2的线程池
    private ExecutorService executor;

    // 私有构造函数，防止外部实例化
    private CPUSimulator() {
        executor = Executors.newFixedThreadPool(1);
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
