package bupt.os.component.memory.ly;


import bupt.os.component.filesystem.filesystem_wdh.FileNode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.LinkedList;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResourceInfo {
    // 预期的文件资源
    HashMap<FileNode, Integer> expectedFileResource;
    // 预期的设备资源
    HashMap<String, Integer> expectedDeviceResource;
    // 当前文件资源
    HashMap<FileNode, Integer> currFileResource;
    // 当前设备资源
    HashMap<String, LinkedList<String>> currDeviceResource;
}
