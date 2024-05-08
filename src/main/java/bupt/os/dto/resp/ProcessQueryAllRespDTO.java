package bupt.os.dto.resp;

import bupt.os.component.memory.ly.PCB;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProcessQueryAllRespDTO {
    // 正在执行的进程id
    List<PCB> runningQueue;

    List<Integer> waitingQueue;

    List<Integer> readyQueue;
}
