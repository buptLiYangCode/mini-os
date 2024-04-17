package bupt.os.component.process;

import bupt.os.component.filesystem.INode;
import bupt.os.component.filesystem.OpenFileEntry;
import bupt.os.component.memory.PageTableEntry;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.LinkedList;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
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
    // 工作目录,进程当前所在的目录，影响着进程对文件的操作
    private INode iNode;

    // 文件描述符表
    private HashMap<Integer, OpenFileEntry> fileDescriptorTable;
    // 页表，VPN -> PPN
    private LinkedList<PageTableEntry> pageTable;

}
