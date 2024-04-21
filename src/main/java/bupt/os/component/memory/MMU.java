package bupt.os.component.memory;

import bupt.os.component.disk.Disk;

import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import static bupt.os.component.disk.Disk.BLOCK_SIZE;

/**
 * 内存管理单元：不存在物理实体，也就没有实例对象，方法全定义成静态。不必采用单例模式
 */
public class MMU {
    private static final Disk disk = Disk.getInstance();
    private static final UserMemory userMemory = UserMemory.getInstance();
    private static final ProtectedMemory protectedMemory = ProtectedMemory.getInstance();

    /**
     * 访问页，如果页不在内存，返回false
     * @param pcb pcb
     * @param vpn 虚拟页号
     * @return 页是否在内存
     */
    public static boolean accessPage(PCB pcb, int vpn) {
        boolean isPageFault = false;
        HashMap<Integer, LinkedList<PageInfo>> processPageTable = protectedMemory.getProcessPageTable();
        PageInfo pageInfo = processPageTable.get(pcb.getPid()).get(vpn);
        if (!pageInfo.isPresent()) {
            System.out.println("进程" + pcb.getProcessName() + "vpn：" + vpn + "ppn：" + pageInfo.getPageNumber() + "不在内存中");
            isPageFault = true;
        }
        return isPageFault;
    }

    /**
     * 将指定块加载进内存
     * @param blockNumbers 块号
     */
    public static Integer loadPageIntoMemory(LinkedList<Integer> blockNumbers) {
        // 先使用页置换算法，选出一个可以存放磁盘数据的页
        Integer availablePageNumber = lruPageSwap(1).get(0);
        // 写入页
        char[][] blocks = disk.getBlocks();
        char[][] pages = userMemory.getPages();
        for (int i = 0; i < blockNumbers.size(); i++) {
            System.arraycopy(blocks[i], 0, pages[availablePageNumber], i * BLOCK_SIZE, BLOCK_SIZE);
        }
        return availablePageNumber;
    }


    /**
     * 采用lru算法获取页号
     * @param requiredPageCount 需要页的数量
     * @return 页号集合
     */
    public static LinkedList<Integer> lruPageSwap(int requiredPageCount) {
        LinkedList<PageSwapInfo> allPagesInfo = protectedMemory.getAllPagesInfo();
        // 选出k个最小lastAccessTime 最小的
        List<Integer> availablePageNumbers = allPagesInfo.stream()
                .sorted(Comparator.comparingLong(PageSwapInfo::getLastAccessTime))
                .limit(requiredPageCount)
                .map(PageSwapInfo::getPpn)
                .toList();

        return new LinkedList<>(availablePageNumbers);
    }
}
