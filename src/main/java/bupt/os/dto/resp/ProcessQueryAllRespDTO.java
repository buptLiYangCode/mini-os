package bupt.os.dto.resp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProcessQueryAllRespDTO {
    // 正在执行的进程id
    int pid;
    // 当前执行的指令
    String currInst;
    // 当前进程占用CPU的起始时间
    Long startTime;

    List<Integer> waitingQueue;

    List<Integer> readyQueue;
}
