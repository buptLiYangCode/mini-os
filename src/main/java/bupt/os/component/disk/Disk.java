package bupt.os.component.disk;

import bupt.os.component.filesystem.INode;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.util.LinkedList;

/**
 *
 */
@Component
@Data
public class Disk {
    // 磁盘大小，例如128KB
    public static final int DISK_SIZE = 128 * 1024;
    // 块数
    public static final int TOTAL_BLOCKS = 256;
    // 块大小，例如512B
    public static final int BLOCK_SIZE = 512;

    // inode位图，占一个块
    private boolean[] iNodeBitmap;
    // block位图
    private boolean[] blockBitmap;
    // inode数组，200个inode占用50个block，指向200个block
    private INode[] iNodes;
    // block数组
    private Block[] blocks;

    // 根据index设置inode
    public void setINodeByIndex(int index, INode iNode) {
        this.iNodes[index] = iNode;
    }

    // 根据index设置inodeBitmap
    public void setINodeBitmapByIndex(int index, boolean isUsed) {
        this.iNodeBitmap[index] = isUsed;
    }

    public void setBlockBitmapByBlockNumbers(LinkedList<Integer> blockNumbers, boolean isUsed) {
        for (Integer number : blockNumbers) {
            this.blockBitmap[number] = isUsed;
        }
    }

    /**
     * 创建文件时，向磁盘块中写入数据
     *
     * @param blockNumbers 待写入的磁盘块号
     * @param data         写入的数据
     */
    public void setBlocksByBlockNumbers(LinkedList<Integer> blockNumbers, String data) {
        char[] dataArray = data.toCharArray();
        int dataIndex = 0;

        for (Integer number : blockNumbers) {
            Block block = this.blocks[number];
            char[] blockData = new char[BLOCK_SIZE];
            int len = Math.min((dataArray.length - dataIndex), BLOCK_SIZE);
            System.arraycopy(dataArray, dataIndex, blockData, 0, len);
            block.setData(blockData);
            dataIndex += len;
        }
    }
}
