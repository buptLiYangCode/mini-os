package bupt.os.component.scheduler;

import bupt.os.component.memory.protected_.PCB;
import bupt.os.component.memory.protected_.ProtectedMemory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;


@Component
public class ScheduledTask {
    private final ProtectedMemory protectedMemory = ProtectedMemory.getInstance();
    private final ConcurrentLinkedQueue<PCB> readyQueue = protectedMemory.getReadyQueue();

    @Scheduled(fixedRate = 13000) // 每隔8000ms执行一次
    public void function() {
        List<PCB> pcbList1 = readyQueue.stream()
                .filter(p -> p.getPriority() == 1)
                .toList();
        for (PCB pcb : pcbList1) {
            pcb.setPriority(3);
        }
        List<PCB> pcbList2 = readyQueue.stream()
                .filter(p -> p.getPriority() == 2)
                .toList();
        for (PCB pcb : pcbList2) {
            pcb.setPriority(3);
        }
    }
}