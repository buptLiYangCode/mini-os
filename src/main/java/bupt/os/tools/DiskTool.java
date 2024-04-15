package bupt.os.tools;

import bupt.os.component.disk.Disk;

import java.util.LinkedList;
import java.util.Random;


public class DiskTool {

    // 在 iNodeBitmap[] 中随机找一个值为 false 的索引
    public static int getFreeINode(Disk disk) {
        boolean[] iNodeBitmap = disk.getINodeBitmap();
        Random random = new Random();
        int index = random.nextInt(iNodeBitmap.length);
        while (iNodeBitmap[index]) {
            index = random.nextInt(iNodeBitmap.length);
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
