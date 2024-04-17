package bupt.os.tools;

import bupt.os.component.disk.Disk;

import java.util.LinkedList;
import java.util.Random;


public class DiskTool {

    // 根据文件、目录名计算哈希值 % iNodeBitmap大小，得出文件、目录的inode号
    public static int getINodeIndex(Disk disk, String fileName) {
        boolean[] iNodeBitmap = disk.getINodeBitmap();
        int hashCode = fileName.hashCode(); // 获取文件名的哈希值
        int index = Math.abs(hashCode) % iNodeBitmap.length; // 取模得出索引

        // 查找第一个未被使用的 inode 号
        while (iNodeBitmap[index]) {
            index = (index + 1) % iNodeBitmap.length; // 线性探测下一个索引
        }

        return index;
    }

    // 在 blockBitmap[] 中随即找出 k 个值为 false 的索引
    public static LinkedList<Integer> getFreeBlocks(Disk disk, int k) {
        boolean[] blockBitmap = disk.getBlockBitmap();
        // 如果需要的空闲块数超过了总块数，直接返回null
        if (k > blockBitmap.length) {
            return null;
        }

        // 存储空闲块的索引
        LinkedList<Integer> freeBlocks = new LinkedList<>();

        // 获取所有空闲块的索引
        for (int i = 0; i < blockBitmap.length; i++) {
            if (!blockBitmap[i]) {
                freeBlocks.add(i);
            }
        }

        // 如果空闲块数量不足k个，返回null
        if (freeBlocks.size() < k) {
            return null;
        }

        // 随机选择k个空闲块的索引
        LinkedList<Integer> result = new LinkedList<>();
        Random rand = new Random();
        while (k > 0) {
            int randomIndex = rand.nextInt(freeBlocks.size());
            result.add(freeBlocks.remove(randomIndex));
            k--;
        }

        return result;
    }
}
