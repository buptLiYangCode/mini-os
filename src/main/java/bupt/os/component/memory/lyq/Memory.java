package bupt.os.component.memory.lyq;


import java.util.Arrays;

public class Memory {
    /**
     * 页面数
     */
    public static final int pageNums = 64;

    /**
     * 页面大小，单位B
     */
    public static final int pageSize = 1024;

    /**
     * 内存数组
     */
    public static final byte[] byteArray = new byte[pageNums * pageSize];

    /**
     * 标记物理块的使用情况
     */
    public static final boolean[] PhysicBlock = new boolean[pageNums];


    /**
     * 将指定页的指定范围的内容写入模拟内存
     *
     * @param pageIndex   要写入的页索引
     * @param startOffset 写入内容的起始偏移量（以字节为单位）
     * @param data        要写入的数据数组
     */
    public static void writePage(int pageIndex, int startOffset, byte[] data) {
        if (pageIndex < 0 || pageIndex >= pageNums || startOffset < 0 || data == null || startOffset + data.length > pageSize) {
            throw new IllegalArgumentException("Invalid pageIndex, startOffset, or data");
        }
        System.arraycopy(data, 0, byteArray, pageIndex * pageSize + startOffset, data.length);
    }


    /**
     * 从指定页的指定偏移量读取指定长度的数据
     *
     * @param pageIndex   要读取的页索引
     * @param startOffset 起始偏移量（以字节为单位）
     * @param length      要读取的数据长度（以字节为单位）,不提供length的情况下默认位4字节
     * @return 读取到的数据数组
     */
    public static byte[] readPage(int pageIndex, int startOffset, int length) {
        if (pageIndex < 0 || pageIndex >= pageNums || startOffset < 0 || startOffset + length > pageSize) {
            throw new IllegalArgumentException("Invalid pageIndex, startOffset, or length");
        }
        byte[] result = new byte[length];
        System.arraycopy(byteArray, pageIndex * pageSize + startOffset, result, 0, length);
        return result;
    }

    /**
     * 修改指定页的指定偏移量处的数据
     *
     * @param pageIndex   要修改的页索引
     * @param startOffset 起始偏移量（以字节为单位）
     * @param newData     新数据数组
     */
    public static void modifyPage(int pageIndex, int startOffset, byte[] newData) {
        if (pageIndex < 0 || pageIndex >= pageNums || startOffset < 0 || newData == null || startOffset + newData.length >= pageSize) {
            throw new IllegalArgumentException("Invalid pageIndex, startOffset, or newData");
        }
        System.arraycopy(newData, 0, byteArray, pageIndex * pageSize + startOffset, newData.length);
    }


    /**
     * 清除指定页的指定偏移量处的数据
     *
     * @param pageIndex   要清除的页索引
     * @param startOffset 起始偏移量（以字节为单位）
     * @param length      要清除的数据长度（以字节为单位）
     */
    public static void clearPage(int pageIndex, int startOffset, int length) {
        if (pageIndex < 0 || pageIndex >= pageNums || startOffset < 0 || startOffset + length >= pageSize) {
            throw new IllegalArgumentException("Invalid pageIndex, startOffset, or length");
        }
        byte[] emptyArray = new byte[length];
        System.arraycopy(emptyArray, 0, byteArray, pageIndex * pageSize + startOffset, length);
    }

    /**
     * 初始化物理块数组，将所有内容全部设置为false。
     */
    public static void initializePhysicBlock() {
        Arrays.fill(PhysicBlock, false);
    }

    /**
     * 设置物理块数组中指定位置的值。
     * @param index 要设置的位置的索引
     * @param value 要设置的值，true表示占用，false表示空闲
     * @throws IllegalArgumentException 如果索引超出了数组的有效范围
     */
    public static void setPhysicBlock(int index, boolean value) {
        if (index >= 0 && index < PhysicBlock.length) {
            PhysicBlock[index] = value;
        } else {
            throw new IllegalArgumentException("Index out of bounds");
        }
    }

    /**
     * 在当前物理块数组中找到一个空闲的位置，将其修改为占用，并返回其索引。
     * @return 找到的空闲位置的索引，如果没有空闲位置则返回-1
     */
    public static int findFreeBlock(int start,int end) {
        for (int i = start; i < PhysicBlock.length&&i<end; i++) {
            if (!PhysicBlock[i]) {
                PhysicBlock[i] = true;
                return i;
            }
        }
        // 如果没有空闲位置，则返回-1表示没有找到空闲位置
        return -1;
    }

    /**
     * 释放指定的物理块，将相应位置的PhysicBlock数组的元素设置为false。
     * @param block 要释放的物理块的编号
     * @throws IllegalArgumentException 如果传入的物理块编号无效
     */
    public static void freeBlock(int block) {
        if (block >= 0 && block < PhysicBlock.length) {
            PhysicBlock[block] = false;
        } else {
            throw new IllegalArgumentException("Invalid block number");
        }
    }


}
