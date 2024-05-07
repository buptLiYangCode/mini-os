package bupt.os.component.memory.lyq;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class MemoryManagementImpl implements MemoryManagement {

    private static void InitBitMap() {
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
        int result = MMU.translateAddress(register, logicAddress, block, startOffset, 0);

        if (result == BOUND_FAULT) return result;
        else if (result == PAGE_FAULT) {
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
        int result = MMU.translateAddress(register, logicAddress, block, startOffset, 0);

        if (result == BOUND_FAULT) return result;
        else if (result == PAGE_FAULT) {
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
    public List<BitMapEntry> getPageUsageBitmap() {
        BitMapEntry[] entries = new BitMapEntry[Memory.pageNums];
        List<BitMapEntry> rt = new ArrayList<>();


        for (int i = 0; i < bitMap.length; i++) {
            rt.add(new BitMapEntry(bitMap[i][0], bitMap[i][1]));
//            entries[i] = new BitMapEntry(bitMap[i][0], bitMap[i][1]);
        }

//        Gson gson = new Gson();

        return rt;
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
//        System.out.println("lululu:"+pageNumber);

        Item oldItem = Table.get(oldPage);
//        System.out.println("lululu:"+oldItem.getPageIndex());
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

        System.out.println("换出第"+oldPage+"页，换入"+pageNumber+"页");

        PageTable.UpdatePageTable(register, Table);
        byte[] pageTableArray1 = Memory.readPage(register, 0, Memory.pageSize);
        ArrayList<Item> Table1 = new PageTable(pageTableArray1).getPageTable();
//        System.out.println("suck:");
//        for (Item item : Table1) {
//            if (item.getBlock() != -1 && item.getState() == PageTable.IN_MEMORY)
//                System.out.println(item.getPageIndex() + " " + item.getBlock() + " " + item.getAccess());
//        }


        //修改TLB
        MMU.updatePageMap(oldItem, newItem, MMU.TLB.get(register));

        //修改Memory和当前页
        bitMap[block][1] = pageNumber;


    }
}
