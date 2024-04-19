package bupt.os.component.timer;

import bupt.os.component.interrupt.InterruptRequestLine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class Timer {

    @Scheduled(fixedRate = 600) // 每隔600ms执行一次
    public void sendInterruptRequest() {
        InterruptRequestLine interruptRequestLine = InterruptRequestLine.getInstance();
        interruptRequestLine.put("TIMER_INTERRUPT");
    }
}