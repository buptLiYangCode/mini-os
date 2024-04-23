package bupt.os.component.interrupt;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * 硬件信号是存储在irl 物理线路上的，和内存无关
 */
@Slf4j
public class InterruptRequestLine {
    private static volatile InterruptRequestLine instance;
    private final ConcurrentLinkedQueue<String> concurrentLinkedQueue;

    private InterruptRequestLine() {
        concurrentLinkedQueue = new ConcurrentLinkedQueue<>();
    }

    public static InterruptRequestLine getInstance() {
        if (instance == null) {
            synchronized (InterruptRequestLine.class) {
                if (instance == null) {
                    instance = new InterruptRequestLine();
                }
            }
        }
        return instance;
    }

    public void offer(String item) {
        if (!item.equals("TIMER_INTERRUPT") || !concurrentLinkedQueue.contains("TIMER_INTERRUPT")) {
            concurrentLinkedQueue.offer(item);
        }
    }

    public String poll() {
        return concurrentLinkedQueue.poll();
    }

    public String peek() {
        return concurrentLinkedQueue.peek();
    }
}
