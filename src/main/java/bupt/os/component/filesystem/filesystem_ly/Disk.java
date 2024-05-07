package bupt.os.component.filesystem.filesystem_ly;

import lombok.Data;
import org.springframework.stereotype.Component;

import java.util.LinkedList;

/**
 *
 */
@Component
@Data
public class Disk {
    private static volatile Disk instance;

    // 磁盘大小，例如128KB
    public static final int DISK_SIZE = 128 * 1024;
    // 块数
    public static final int TOTAL_BLOCKS = 200;
    // 块大小，例如512B
    public static final int BLOCK_SIZE = 512;

    // inode位图，占一个块
    private boolean[] iNodeBitmap;
    // block位图
    private boolean[] blockBitmap;
    // inode数组，200个inode占用50个block，指向200个block
    private INode[] iNodes;
    // block数组
    private char[][] blocks;

    // 私有构造函数，防止外部实例化
    private Disk() {
        // 初始化各个数据结构
        iNodeBitmap = new boolean[TOTAL_BLOCKS];
        blockBitmap = new boolean[TOTAL_BLOCKS];
        iNodes = new INode[TOTAL_BLOCKS];
        blocks = new char[TOTAL_BLOCKS][BLOCK_SIZE];
    }

    // 获取单例实例的静态方法
    public static Disk getInstance() {
        if (instance == null) {
            synchronized (Disk.class) {
                if (instance == null) {
                    instance = new Disk();
                }
            }
        }
        return instance;
    }

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
        // 写入数据的下标
        int dataIndex = 0;
        for (Integer number : blockNumbers) {
            if (dataIndex >= dataArray.length)
                break;
            int len = Math.min(BLOCK_SIZE, dataArray.length - dataIndex);
            char[] block = blocks[number];
            System.arraycopy(dataArray, dataIndex, block, 0, len);
            dataIndex += BLOCK_SIZE;

        }
    }
}
