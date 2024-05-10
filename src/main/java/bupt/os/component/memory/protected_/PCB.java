package bupt.os.component.memory.protected_;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class PCB {
    // 进程id
    private int pid;
    // 进程名
    private String processName;
    // 指令寄存器，保存当前正在执行指令地址
    private int ir;
    // 进程大小，单位为B
    private int size;
    // 进程状态
    private String state;
    // 进程页表存储在内存中的页号
    private int register;
    // 剩余可执行时间，单位ms
    private long remainingTime;
    // 预期运行时间
    private long expectedTime;
    // 进程优先级
    private int priority;
    // 指令集
    private String[] instructions;

}
