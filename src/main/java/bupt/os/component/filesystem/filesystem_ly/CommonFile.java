package bupt.os.component.filesystem.filesystem_ly;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.LinkedList;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommonFile implements INode{
    // 文件名
    private String fileName;
    // 文件可读、可写
    private String flags;
    // 文件大小，单位为B
    private int size;
    // 分配block数
    private int blockCount;
    // 磁盘分配方式：索引分配
    private LinkedList<Integer> blockNumbers;

    // 创建时间
    private LocalDateTime createTime;
    // 修改时间
    private LocalDateTime updateTime;
    // 最近访问时间
    private LocalDateTime accessTime;
}
