package bupt.os.component.timer;

import bupt.os.component.interrupt.InterruptRequestLine;
import bupt.os.component.memory.protected_.ProtectedMemory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class Timer {
    private final ProtectedMemory protectedMemory = ProtectedMemory.getInstance();
    private final ConcurrentHashMap<Long, InterruptRequestLine> irlTable = protectedMemory.getIrlTable();

    @Scheduled(fixedRate = 600) // 每隔600ms执行一次
    public void sendInterruptRequest() {
        Set<Long> keySet = irlTable.keySet();
        for (Long threadId : keySet) {
            InterruptRequestLine irl = irlTable.get(threadId);
            if(irl != null)
                irl.offer("TIMER_INTERRUPT");
        }
    }
}
