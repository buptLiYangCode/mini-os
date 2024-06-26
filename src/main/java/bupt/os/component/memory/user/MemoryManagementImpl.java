package bupt.os.component.memory.user;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

public class MemoryManagementImpl implements MemoryManagement {

    /**
     * 置换策略，LRU=0,LFU=1
     */
    public static int MODE = 0;


    /**
     * 访问统计
     */
    public static int[] AccessCnt = new int[10];

    /**
     * 缺页统计
     */
    public static int[] PageFaultCnt = new int[10];

    private static void InitBitMap() {

        Arrays.fill(AccessCnt, 0);

        Arrays.fill(PageFaultCnt, 0);

        for (int[] Ints : bitMap) {
            Arrays.fill(Ints, -1);
        }
    }


    @Override
    public int Allocate(int PID, int size) {
        //找到页表所在的页，修改bitmap
        int freeBlock = Memory.findFreeBlock(0, 10);

        if (freeBlock == -1) return freeBlock;

        bitMap[freeBlock][0] = PID;
        bitMap[freeBlock][1] = 4;


        //申请驻留集，修改页表，然后吧页表写回内存
        PageTable pageTable = new PageTable();
        ArrayList<Item> Table = pageTable.getPageTable();
        for (int i = 0; i < 4; i++) {
            int newBlock = Memory.findFreeBlock(10, Memory.pageNums);
            bitMap[newBlock][0] = PID;
            Table.get(i).setBlock((byte) newBlock);
        }


        PageTable.UpdatePageTable(freeBlock, Table);
        return freeBlock;
    }

    @Override
    public boolean Free(int register) {
        //获取页表
        byte[] bytes = Memory.readPage(register, 0, Memory.pageSize);
        PageTable pageTable = new PageTable(bytes);
        ArrayList<Item> table = pageTable.getPageTable();

        //释放TLB
        MMU.FreeTLB(register);

        //释放内存
        for (Item item : table) {
            if (item.getBlock() != -1) {
                Memory.freeBlock(item.getBlock());
                bitMap[item.getBlock()][0] = -1;
                bitMap[item.getBlock()][1] = -1;
            }
        }

        Memory.freeBlock(register);
        bitMap[register][0] = -1;
        bitMap[register][1] = -1;
        return true;
    }

    @Override
    public int Read(int register, int logicAddress, byte[] data) {
        //完成虚实地址转换
        AtomicInteger block = new AtomicInteger(0);
        AtomicInteger startOffset = new AtomicInteger(0);

        //判断是否需要增加或者减少页
        if (AccessCnt[register] == 20) {
            AccessCnt[register] = 0;
            if (PageFaultCnt[register] > 10 && bitMap[register][1] < 6) {
                System.out.println("增加驻留集");
                byte[] bytes = Memory.readPage(register, 0, Memory.pageSize);
                PageTable pageTable = new PageTable(bytes);
                ArrayList<Item> Table = pageTable.getPageTable();

                int newBlock = Memory.findFreeBlock(10, Memory.pageNums);
                bitMap[newBlock][0] = bitMap[register][0];
                bitMap[register][1] = bitMap[register][1] + 1;
                for (Item item : Table) {
                    if (item.getState() == 0) {
                        item.setBlock((byte) newBlock);
                    }
                }
                PageTable.UpdatePageTable(register, Table);

            } else if (PageFaultCnt[register] < 5 && bitMap[register][1] > 4) {

                byte[] bytes = Memory.readPage(register, 0, Memory.pageSize);
                PageTable pageTable = new PageTable(bytes);
                ArrayList<Item> Table = pageTable.getPageTable();

                ArrayList<Item> CopyList = new ArrayList<>(Table);

                int page = -1;
                if (MODE == MODE_LFU) {
                    page = LFU.findPage(CopyList, register);
                } else if (MODE == MODE_LRU) {
                    page = LRU.findPage(CopyList, register);
                }
                Item item = Table.get(page);

                Memory.freeBlock(item.getBlock());
                bitMap[item.getBlock()][0] = -1;
                bitMap[item.getBlock()][1] = -1;

                item.setBlock((byte) -1);
                item.setState((byte) 0);

                PageTable.UpdatePageTable(register, Table);
                bitMap[register][1] = bitMap[register][1] - 1;
            }
            PageFaultCnt[register] = 0;
        }

        AccessCnt[register] = AccessCnt[register] + 1;

        int result = 0;
        if (MODE == 0) {
            result = MMU.translateAddress(register, logicAddress, block, startOffset, 0);
        } else {
            result = MMU.translateAddressLFU(register, logicAddress, block, startOffset, 0);
        }

        if (result == BOUND_FAULT) return result;
        else if (result == PAGE_FAULT) {
            PageFaultCnt[register] = PageFaultCnt[register] + 1;
            AccessCnt[register] = AccessCnt[register] - 1;
            int rt = block.get();
            data[0] = (byte) (rt >>> 24); // 无符号右移24位，得到最高字节
            data[1] = (byte) (rt >>> 16); // 无符号右移16位，得到次高字节
            data[2] = (byte) (rt >>> 8);  // 无符号右移8位，得到第三字节
            data[3] = (byte) rt;          // 最低字节
            return PAGE_FAULT;
        } else {
            byte[] rt = Memory.readPage(block.get(), startOffset.get(), 4);
            System.arraycopy(rt, 0, data, 0, rt.length);
            return SUCCESS;
        }

    }


    @Override
    public int Write(int register, int logicAddress, byte[] data) {
        //完成虚实地址转换
        AtomicInteger block = new AtomicInteger(0);
        AtomicInteger startOffset = new AtomicInteger(0);

        //判断是否需要增加或者减少页
        if (AccessCnt[register] == 20) {
            AccessCnt[register] = 0;
            if (PageFaultCnt[register] > 10 && bitMap[register][1] < 6) {
                System.out.println("增加驻留集");
                byte[] bytes = Memory.readPage(register, 0, Memory.pageSize);
                PageTable pageTable = new PageTable(bytes);
                ArrayList<Item> Table = pageTable.getPageTable();

                int newBlock = Memory.findFreeBlock(10, Memory.pageNums);
                bitMap[newBlock][0] = bitMap[register][0];
                bitMap[register][1] = bitMap[register][1] + 1;
                for (Item item : Table) {
                    if (item.getState() == 0) {
                        item.setBlock((byte) newBlock);
                    }
                }
                PageTable.UpdatePageTable(register, Table);

            } else if (PageFaultCnt[register] < 5 && bitMap[register][1] > 4) {
                System.out.println("减少驻留集");
                byte[] bytes = Memory.readPage(register, 0, Memory.pageSize);
                PageTable pageTable = new PageTable(bytes);
                ArrayList<Item> Table = pageTable.getPageTable();

                ArrayList<Item> CopyList = new ArrayList<>(Table);

                int page = -1;
                if (MODE == MODE_LFU) {
                    page = LFU.findPage(CopyList, register);
                } else if (MODE == MODE_LRU) {
                    page = LRU.findPage(CopyList, register);
                }
                Item item = Table.get(page);

                Memory.freeBlock(item.getBlock());
                bitMap[item.getBlock()][0] = -1;
                bitMap[item.getBlock()][1] = -1;

                item.setBlock((byte) -1);
                item.setState((byte) 0);

                PageTable.UpdatePageTable(register, Table);
                bitMap[register][1] = bitMap[register][1] - 1;
            }
            PageFaultCnt[register] = 0;
        }

        AccessCnt[register] = AccessCnt[register] + 1;

        int result = 0;
        if (MODE == 0) {
            result = MMU.translateAddress(register, logicAddress, block, startOffset, 0);
        } else {
            result = MMU.translateAddressLFU(register, logicAddress, block, startOffset, 0);
        }

        if (result == BOUND_FAULT) return result;
        else if (result == PAGE_FAULT) {
            PageFaultCnt[register] = PageFaultCnt[register] + 1;
            AccessCnt[register] = AccessCnt[register] - 1;

            int rt = block.get();
            data[0] = (byte) (rt >>> 24); // 无符号右移24位，得到最高字节
            data[1] = (byte) (rt >>> 16); // 无符号右移16位，得到次高字节
            data[2] = (byte) (rt >>> 8);  // 无符号右移8位，得到第三字节
            data[3] = (byte) rt;          // 最低字节
            return PAGE_FAULT;
        } else {

            Memory.writePage(block.get(), startOffset.get(), data);
            return SUCCESS;
        }
    }


    @Override
    public ArrayList<BitMapEntry> getPageUsageBitmap() {
        ArrayList<BitMapEntry> entries = new ArrayList<>();
        for (int i = 0; i < bitMap.length; i++) {
            entries.add(new BitMapEntry(bitMap[i][0], bitMap[i][1]));
        }
        return entries;
    }


    @Override
    public void InitMemory() {
        Memory.initializePhysicBlock();
        MMU.Init();
        InitBitMap();
    }

    @Override
    public void PageFaultProcess(int register, int logicalAddress, int oldPage) {
        //修改页表
        byte[] pageTableArray = Memory.readPage(register, 0, Memory.pageSize);
        ArrayList<Item> Table = new PageTable(pageTableArray).getPageTable();
        int pageNumber = (logicalAddress >> 10) & 0x3FFFFF;
        System.out.println("换出第" + oldPage + "页，换入" + pageNumber + "页");

        Item oldItem = Table.get(oldPage);
        oldItem.setState((byte) 0);
        oldItem.setAccess((byte) 0);
        oldItem.setChange((byte) 0);
        int block = oldItem.getBlock();
        oldItem.setBlock((byte) -1);

        Item newItem = Table.get(pageNumber);
        newItem.setState((byte) 1);
        newItem.setAccess((byte) 0);

        oldItem.setBlock(newItem.getBlock());
        newItem.setBlock((byte) block);


        PageTable.UpdatePageTable(register, Table);
        byte[] pageTableArray1 = Memory.readPage(register, 0, Memory.pageSize);
        ArrayList<Item> Table1 = new PageTable(pageTableArray1).getPageTable();

        //修改TLB
        if (MODE == 0) {
            MMU.updatePageMap(oldItem, newItem, MMU.TLB.get(register));
        } else {
            MMU.updatePageMapLFU(oldItem, newItem, MMU.TLB.get(register));
        }

        //修改Memory和当前页
        bitMap[block][1] = pageNumber;
    }

    /**
     * 设置内存置换策略
     *
     * @param mode 模式，LRU=0，LFU=1
     */
    public static void setMode(int mode) {
        MODE = mode;
    }
}