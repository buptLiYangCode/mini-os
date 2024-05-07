package bupt.os.component.memory.ly;

import bupt.os.component.disk.filesystem_ly.OpenFileInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;

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
    // 本次执行开始的时间戳，UTC 时间 1970 年 1 月 1 日 00:00:00 以来经过的毫秒数
    private long startTime;
    private String[] instructions;

    // 文件描述符表
    private HashMap<Integer, OpenFileInfo> fileDescriptorTable;
}
