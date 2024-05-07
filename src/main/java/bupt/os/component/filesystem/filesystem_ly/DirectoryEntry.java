package bupt.os.component.filesystem.filesystem_ly;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 目录条目
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DirectoryEntry {
    // 目录下的条目分两类：子目录，普通文件
    private String fileType;
    // 文件名
    private String filename;
    // 文件对应inode号
    private int iNodeNumber;

}