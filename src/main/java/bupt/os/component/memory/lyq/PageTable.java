package bupt.os.component.memory.lyq;

import java.util.ArrayList;

public class PageTable {
    public final static byte IN_MEMORY = 1;
    public final static byte OUT_MEMORY = 0;

    public final static int NOT_MODIFY = 0;
    public final static int MODIFY = 1;


    /**
     * 页表
     */
    private ArrayList<Item> pageTable = new ArrayList<>();

    public PageTable(byte[] table) {
        if (table == null || table.length != Memory.pageSize) {
            throw new IllegalArgumentException("Invalid table size");
        }

        int pageIndex = 0;

        for (int i = 0; i < table.length; i += 8) {
            // 解析每个单位的数据
            byte block = table[i];
            byte state = table[i + 1];
            byte access = table[i + 2];
            byte change = table[i + 3];
            int disk = ((table[i + 4] & 0xFF) << 24) |
                    ((table[i + 5] & 0xFF) << 16) |
                    ((table[i + 6] & 0xFF) << 8) |
                    (table[i + 7] & 0xFF);

            // 创建 Item 对象并添加到 pageTable 中p
            Item item = new Item(pageIndex, disk, change, access, state, block);
            pageTable.add(item);

            // 更新 pageIndex
            pageIndex++;
        }
    }

    public PageTable() {
        int pageIndex = 0;
        for (int i = 0; i < Memory.pageSize / 8; i++) {
            // 创建 Item 对象并添加到 pageTable 中p
            Item item = new Item(pageIndex, 0, (byte) 0, (byte) 0, (byte) 0, (byte) -1);
            pageTable.add(item);
            // 更新 pageIndex
            pageIndex++;
        }
    }

    public static void UpdatePageTable(int register, ArrayList<Item> pageTable) {
        // 计算需要的总字节数
        int totalBytes = pageTable.size() * 8;
        byte[] byteArray = new byte[totalBytes];

//        System.out.println("准备找到了：");
//        for (Item item : pageTable) {
//            if (item.getBlock() != -1 && item.getState() == PageTable.IN_MEMORY)
//                System.out.println(item.getPageIndex() + " " + item.getBlock() + " " + item.getAccess());
//        }

        // 遍历pageTable，将每个Item对象转换为对应的字节并添加到byteArray中
        for (int i = 0; i < pageTable.size(); i++) {
            Item item = pageTable.get(i);
            int startIndex = i * 8; // 每个Item对象占据8个字节
            byteArray[startIndex] = item.getBlock();
            byteArray[startIndex + 1] = item.getState();
            byteArray[startIndex + 2] = item.getAccess();
            byteArray[startIndex + 3] = item.getChange();
            // 将disk字段拆分为四个字节写入byteArray
            int disk = item.getDisk();
            byteArray[startIndex + 4] = (byte) ((disk >> 24) & 0xFF);
            byteArray[startIndex + 5] = (byte) ((disk >> 16) & 0xFF);
            byteArray[startIndex + 6] = (byte) ((disk >> 8) & 0xFF);
            byteArray[startIndex + 7] = (byte) (disk & 0xFF);
        }
        Memory.writePage(register, 0, byteArray);
    }


    public ArrayList<Item> getPageTable() {
        return pageTable;
    }


}