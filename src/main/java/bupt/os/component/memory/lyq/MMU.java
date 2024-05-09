package bupt.os.component.memory.lyq;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
public class MMU {
    /**
     * TLB，页表的链表，共十个页表，每个页表有128个页表项，每次访问的时候先访问TLB，然后修改TLB，注意TLB的同步
     */
    public static final ArrayList<Map<Integer, Item>> TLB = new ArrayList<>();

    /**
     * TLB和页表的访问差值
     */
    public static int[] Delta = new int[Memory.pageNums];

    /**
     * 初始化TLB
     */
    public static void Init() {
        for (int i = 0; i < 10; i++) {
            Map<Integer, Item> PageMap = new HashMap<>();
            TLB.add(PageMap);
        }
    }

    public static void FreeTLB(int register) {

        Map<Integer, Item> itemMap = TLB.get(register);
        itemMap.clear();
    }


    /**
     * 完成虚实地址转换
     *
     * @param register       页表寄存器，用于查找物理地址
     * @param logicalAddress 逻辑地址，高22位是页号，低10位是页内偏移
     * @param block          物理块号，实际的返回值是这个
     * @param startOffset    页内偏移，这个也是返回值，缺页的时候，block返回的实际上是要调走的页号
     * @param Modify         是否修改
     * @return 转换结果
     */
    public static int translateAddress(int register, int logicalAddress, AtomicInteger block, AtomicInteger startOffset, int Modify) {

        int pageNumber = (logicalAddress >> 10) & 0x3FFFFF; // 高22位是页号
        int offset = logicalAddress & 0x3FF; // 低10位是页内偏移

        //地址越界
        if (pageNumber > Memory.pageSize / 8) {
            return MemoryManagement.BOUND_FAULT;
        }

        //先查询TLB，TLB有则更新TLB，然后直接返回，否则查询页表，然后返回，否则查询页表的同时更新页表和TLB，只有被访问过的页面才会进入TLB
        Map<Integer, Item> PageMap = TLB.get(register);

        //TLB查询成功  else失败
        if (PageMap.get(pageNumber) != null) {
            PageMap.forEach((key, value) -> {
                value.setAccess((byte) (value.getAccess() + 1));
            });
            PageMap.get(pageNumber).setAccess((byte) 0);
            PageMap.get(pageNumber).setChange((byte) Modify);


//            System.out.println("TLB:");
//            for (Item item : PageMap.values()) {
//                System.out.println(item.getPageIndex() + " " + item.getBlock() + " " + item.getAccess());
//            }


            Delta[register] = Delta[register] + 1;

//            System.out.println("Delta:" + Delta[register]);

            block.set(PageMap.get(pageNumber).getBlock());

        } else {
            //失败首先要查页表，然后同步页表和TLB，同步结束之后，如果没查到，需要换页，需要同时对页表和TLB进行处理处理函数放在LRU中，最终在Interface中调用

            //获取页表
            byte[] pageTableArray = Memory.readPage(register, 0, Memory.pageSize);
            ArrayList<Item> pageTable = new PageTable(pageTableArray).getPageTable();


            //更新页表
            for (Item item : pageTable) {
                if (item.getState() == PageTable.IN_MEMORY) {
                    if (PageMap.get(item.getPageIndex()) != null) {
//                        System.out.println("test:"+item.getPageIndex());
                        item.setAccess((byte) (PageMap.get(item.getPageIndex()).getAccess() + 1));
                        PageMap.get(item.getPageIndex()).setAccess(item.getAccess());

                    } else {
//                        System.out.println("test:" + item.getPageIndex());
//                        System.out.println("test:" + item.getAccess());
                        item.setAccess((byte) (item.getAccess() + Delta[register] + 1));
//                        System.out.println("test:" + item.getAccess());

                    }
                }
            }

//            System.out.println("页表：");
//            for (Item item : pageTable) {
//                if (item.getBlock() != -1 && item.getState() == PageTable.IN_MEMORY)
//                    System.out.println(item.getPageIndex() + " " + item.getBlock() + " " + item.getAccess());
//            }
//
//            System.out.println("TLB:");
//            for (Item item : PageMap.values()) {
//                System.out.println(item.getPageIndex() + " " + item.getBlock() + " " + item.getAccess());
//            }

            Delta[register] = 0;

            //判断是否缺页，这一步无论结果如何，都要将最新的页表写回内存
            if (pageTable.get(pageNumber).getState() == PageTable.OUT_MEMORY) {
                //缺页处理，分配相关数据后交给LRU处理,前面的所有参数继续传入就行了，LRU会判断哪一页需要换出以及怎么修改TLB

                //这一步的页表和快表修改放在调页结束后
                ArrayList<Item> copyList = new ArrayList<>(pageTable);

//                for (Item item : copyList) {
//                    if (item.getBlock() != -1 && item.getState() == 1)
//                        System.out.println(item.getPageIndex() + " " + item.getAccess());
//                }

                block.set(LRU.findPage(copyList, register));
                PageTable.UpdatePageTable(register, pageTable);
//                System.out.println("找到的页"+block.get());
                return MemoryManagement.PAGE_FAULT;
            } else {

                //不缺页处理，更新页表和TLB即可
                pageTable.get(pageNumber).setAccess((byte) 0);
                pageTable.get(pageNumber).setChange((byte) Modify);
                updatePageMap(pageTable.get(pageNumber), PageMap);
                block.set((int) pageTable.get(pageNumber).getBlock());
            }

            PageTable.UpdatePageTable(register, pageTable);
        }

        startOffset.set(offset);
        return MemoryManagement.SUCCESS;
    }

    /**
     * 完成虚实地址转换
     *
     * @param register       页表寄存器，用于查找物理地址
     * @param logicalAddress 逻辑地址，高22位是页号，低10位是页内偏移
     * @param block          物理块号，实际的返回值是这个
     * @param startOffset    页内偏移，这个也是返回值，缺页的时候，block返回的实际上是要调走的页号
     * @param Modify         是否修改
     * @return 转换结果
     */
    public static int translateAddressLFU(int register, int logicalAddress, AtomicInteger block, AtomicInteger startOffset, int Modify) {

        int pageNumber = (logicalAddress >> 10) & 0x3FFFFF; // 高22位是页号
        int offset = logicalAddress & 0x3FF; // 低10位是页内偏移

        //地址越界
        if (pageNumber > Memory.pageSize / 8) {
            return MemoryManagement.BOUND_FAULT;
        }

        //先查询TLB，TLB有则更新TLB，然后直接返回，否则查询页表，然后返回，否则查询页表的同时更新页表和TLB，只有被访问过的页面才会进入TLB
        Map<Integer, Item> PageMap = TLB.get(register);

        //TLB查询成功  else失败
        if (PageMap.get(pageNumber) != null) {

            PageMap.get(pageNumber).setAccess((byte) (PageMap.get(pageNumber).getAccess()+1));
            PageMap.get(pageNumber).setChange((byte) Modify);


//            System.out.println("TLB:");
//            for (Item item : PageMap.values()) {
//                System.out.println(item.getPageIndex() + " " + item.getBlock() + " " + item.getAccess());
//            }


            Delta[register] = Delta[register] + 1;

//            System.out.println("Delta:" + Delta[register]);

            block.set(PageMap.get(pageNumber).getBlock());

        } else {
            //失败首先要查页表，然后同步页表和TLB，同步结束之后，如果没查到，需要换页，需要同时对页表和TLB进行处理处理函数放在LRU中，最终在Interface中调用

            //获取页表
            byte[] pageTableArray = Memory.readPage(register, 0, Memory.pageSize);
            ArrayList<Item> pageTable = new PageTable(pageTableArray).getPageTable();


            //更新页表
            for (Item item : pageTable) {
                if (item.getState() == PageTable.IN_MEMORY) {
                    if (PageMap.get(item.getPageIndex()) != null) {
//                        System.out.println("test:"+item.getPageIndex());
                        item.setAccess((byte) (PageMap.get(item.getPageIndex()).getAccess()));
                    } else {
//                        System.out.println("test:" + item.getPageIndex());
//                        System.out.println("test:" + item.getAccess());
//                        item.setAccess((byte) (item.getAccess() + Delta[register] + 1));
//                        System.out.println("test:" + item.getAccess());

                    }
                }
            }

//            System.out.println("页表：");
//            for (Item item : pageTable) {
//                if (item.getBlock() != -1 && item.getState() == PageTable.IN_MEMORY)
//                    System.out.println(item.getPageIndex() + " " + item.getBlock() + " " + item.getAccess());
//            }
//
//            System.out.println("TLB:");
//            for (Item item : PageMap.values()) {
//                System.out.println(item.getPageIndex() + " " + item.getBlock() + " " + item.getAccess());
//            }

            Delta[register] = 0;

            //判断是否缺页，这一步无论结果如何，都要将最新的页表写回内存
            if (pageTable.get(pageNumber).getState() == PageTable.OUT_MEMORY) {
                //缺页处理，分配相关数据后交给LRU处理,前面的所有参数继续传入就行了，LRU会判断哪一页需要换出以及怎么修改TLB

                //这一步的页表和快表修改放在调页结束后
                ArrayList<Item> copyList = new ArrayList<>(pageTable);

//                for (Item item : copyList) {
//                    if (item.getBlock() != -1 && item.getState() == 1)
//                        System.out.println(item.getPageIndex() + " " + item.getAccess());
//                }

                block.set(LFU.findPage(copyList, register));
                PageTable.UpdatePageTable(register, pageTable);
//                System.out.println("找到的页"+block.get());
                return MemoryManagement.PAGE_FAULT;
            } else {

                //不缺页处理，更新页表和TLB即可
                pageTable.get(pageNumber).setAccess((byte) (pageTable.get(pageNumber).getAccess()+1));
                pageTable.get(pageNumber).setChange((byte) Modify);
                updatePageMapLFU(pageTable.get(pageNumber), PageMap);
                block.set((int) pageTable.get(pageNumber).getBlock());
            }

            PageTable.UpdatePageTable(register, pageTable);
        }

        startOffset.set(offset);
        return MemoryManagement.SUCCESS;
    }


    /**
     * 更新PageMap中的内容。
     * 如果pageMap的大小小于3，则直接插入Item。
     * 否则，在pageMap中找到access值最大的item，然后用传入的Item替换掉原来的。
     *
     * @param item    要插入或替换的Item对象
     * @param pageMap 要更新的PageMap
     */
    public static void updatePageMap(Item item, Map<Integer, Item> pageMap) {
        if (pageMap.size() < 3) {
            // 如果pageMap中的条目数量小于3，则直接插入Item
            pageMap.put(item.getPageIndex(), item);
        } else {
            // 找到access值最大的item
            Item maxAccessItem = null;
            byte maxAccess = Byte.MIN_VALUE; // 初始化最大值为最小值

            for (Item currentItem : pageMap.values()) {
                if (currentItem.getAccess() > maxAccess) {
                    maxAccessItem = currentItem;
                    maxAccess = currentItem.getAccess();
                }
            }

            // 如果找到了access值最大的item，则用传入的Item替换掉原来的
            if (maxAccessItem != null) {
                pageMap.remove(maxAccessItem.getPageIndex());
                pageMap.put(item.getPageIndex(), item);
            }
        }
    }

    /**
     * 更新PageMap中的内容。要删除掉原来的
     * 如果pageMap的大小小于3，则直接插入Item。
     *
     * @param oldItem 旧的，如果有要删除
     * @param newItem 新的
     * @param pageMap 要更新的PageMap
     */
    public static void updatePageMap(Item oldItem, Item newItem, Map<Integer, Item> pageMap) {
        pageMap.values().removeIf(currentItem -> currentItem.getPageIndex() == oldItem.getPageIndex());
        if (pageMap.size() < 3) {

            // 如果pageMap中的条目数量小于3，则直接插入Item
            pageMap.put(newItem.getPageIndex(), newItem);
        } else {
            // 找到access值最大的item
            Item maxAccessItem = null;
            byte maxAccess = Byte.MIN_VALUE; // 初始化最大值为最小值

            for (Item currentItem : pageMap.values()) {
                if (currentItem.getAccess() > maxAccess) {
                    maxAccessItem = currentItem;
                    maxAccess = currentItem.getAccess();
                }
            }

            // 如果找到了access值最大的item，则用传入的Item替换掉原来的
            if (maxAccessItem != null) {
                pageMap.remove(maxAccessItem.getPageIndex());
                pageMap.put(newItem.getPageIndex(), newItem);
            }
        }
    }


    /**
     * 更新PageMap中的内容。
     * 如果pageMap的大小小于3，则直接插入Item。
     * 否则，在pageMap中找到access值最大的item，然后用传入的Item替换掉原来的。
     *
     * @param item    要插入或替换的Item对象
     * @param pageMap 要更新的PageMap
     */
    public static void updatePageMapLFU(Item item, Map<Integer, Item> pageMap) {
        if (pageMap.size() < 3) {
            // 如果pageMap中的条目数量小于3，则直接插入Item
            pageMap.put(item.getPageIndex(), item);
        } else {
            // 找到access值最大的item
            Item minAccessItem = null;
            byte minAccess = Byte.MAX_VALUE; // 初始化最大值为最小值

            for (Item currentItem : pageMap.values()) {
                if (currentItem.getAccess() < minAccess) {
                    minAccessItem = currentItem;
                    minAccess = currentItem.getAccess();
                }
            }

            // 如果找到了access值最大的item，则用传入的Item替换掉原来的
            if (minAccessItem != null) {
                pageMap.remove(minAccessItem.getPageIndex());
                pageMap.put(item.getPageIndex(), item);
            }
        }
    }

    /**
     * 更新PageMap中的内容。要删除掉原来的
     * 如果pageMap的大小小于3，则直接插入Item。
     *
     * @param oldItem 旧的，如果有要删除
     * @param newItem 新的
     * @param pageMap 要更新的PageMap
     */
    public static void updatePageMapLFU(Item oldItem, Item newItem, Map<Integer, Item> pageMap) {
        pageMap.values().removeIf(currentItem -> currentItem.getPageIndex() == oldItem.getPageIndex());

        if (pageMap.size() < 3) {

            // 如果pageMap中的条目数量小于3，则直接插入Item
            pageMap.put(newItem.getPageIndex(), newItem);
        } else {
            // 找到access值最大的item
            Item minAccessItem = null;
            byte minAccess = Byte.MAX_VALUE; // 初始化最大值为最小值

            for (Item currentItem : pageMap.values()) {
                if (currentItem.getAccess() < minAccess) {
                    minAccessItem = currentItem;
                    minAccess = currentItem.getAccess();
                }
            }

            // 如果找到了access值最大的item，则用传入的Item替换掉原来的
            if (minAccessItem != null) {
                pageMap.remove(minAccessItem.getPageIndex());
                pageMap.put(newItem.getPageIndex(), newItem);
            }
        }
    }
}

