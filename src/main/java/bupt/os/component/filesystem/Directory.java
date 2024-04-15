package bupt.os.component.filesystem;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.LinkedList;

/**
 * 在文件系统中，目录视为特殊的文件
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Directory implements INode {

    // 目录名
    private String DirectoryName;
    // 根目录下条目，
    private LinkedList<DirectoryEntry> entries;

}
