package bupt.os.component.memory.user;


import java.util.ArrayList;
//发生缺页中断，根据页表寄存器确定要换出哪一页，并将页表寄存器和虚拟地址转给中断处理程序，中断处理程序在给回来，完成缺页后的写入
public class LRU {

    /**
     * 根据页表和寄存器查找页面。
     * 如果驻留集中有空页，则返回第空页的索引。
     * 否则，如果驻留集中没有空页，则返回驻留集中访问次数最大的页面的索引。
     * @param pageTable 页表，存储了页面的信息
     * @param register  页表寄存器
     * @return 如果驻留集中有空页，则返回第一个空页的索引；如果驻留集中没有空页，则返回驻留集中访问次数最大的页面的索引；如果页表为空或驻留集中没有页面，则返回-1
     */
    public static int findPage(ArrayList<Item> pageTable, int register) {
        int pid = MemoryManagement.bitMap[register][0];
        int block = -1;

        //驻留集中有空，空的页号为i
        for (int i = 0; i < MemoryManagement.bitMap.length; i++) {
            if (MemoryManagement.bitMap[i][0] == pid && MemoryManagement.bitMap[i][1] == -1) {
                block = i;
                break;
            }
        }

        int maxAccessIndex = -1; // 默认情况下，如果没有符合条件的Item，则返回-1
        byte maxAccessValue = -1; // 记录找到的最大的access字段值

        if (block == -1) {
            for (int i = 0; i < pageTable.size(); i++) {
                Item item = pageTable.get(i);
                if (item.getState() == PageTable.IN_MEMORY && item.getAccess() > maxAccessValue) {
                    maxAccessIndex = i;
                    maxAccessValue = item.getAccess();
                }
            }
        } else {
            for (int i = 0; i < pageTable.size(); i++) {
                Item item = pageTable.get(i);
                if (item.getBlock() == block) {
                    maxAccessIndex = i;
                }
            }
        }
        return maxAccessIndex;
    }
}