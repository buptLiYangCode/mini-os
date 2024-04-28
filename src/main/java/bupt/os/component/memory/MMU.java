package bupt.os.component.memory;

import java.util.HashMap;
import java.util.LinkedList;

/**
 * 内存管理单元：不存在物理实体，也就没有实例对象，方法全定义成静态。不必采用单例模式
 */
public class MMU {
    private static final ProtectedMemory protectedMemory = ProtectedMemory.getInstance();

    private static final LinkedList<PageSwapInfo> allPageInfo = protectedMemory.getAllPageInfo();
    private static final HashMap<Integer, LinkedList<PageInfo>> processPageTable = protectedMemory.getProcessPageTable();


    /**
     * 访问页，如果页不在内存，返回false
     *
     * @param pcb pcb
     * @param vpn 虚拟页号
     * @return 页是否在内存
     */
    public static boolean accessPage(PCB pcb, int vpn) {
        boolean isPageFault = false;
        PageInfo pageInfo = processPageTable.get(pcb.getPid()).get(vpn);
        if (!pageInfo.isPresent()) {
            System.out.println("进程" + pcb.getProcessName() + "vpn：" + vpn + "ppn：" + pageInfo.getPageNumber() + "不在内存中");
            isPageFault = true;
        } else {
            PageSwapInfo pageSwapInfo = allPageInfo.get(pageInfo.getPageNumber());
            pageSwapInfo.setLastAccessTime(System.currentTimeMillis());
        }
        return isPageFault;
    }


    /**
     * 采用lru算法获取页号
     *
     * @return 页号
     */
    public static int lruPageSwap() {
        LinkedList<PageSwapInfo> allPageInfo = protectedMemory.getAllPageInfo();
        // 选出1个lastAccessTime 最小的页
        int resNumber = -1;
        long latestAccessTime = System.currentTimeMillis();
        for (int i = 0; i < allPageInfo.size(); i++) {
            PageSwapInfo pageSwapInfo = allPageInfo.get(i);
            if (pageSwapInfo.getLastAccessTime() < latestAccessTime) {
                latestAccessTime = pageSwapInfo.getLastAccessTime();
                resNumber = i;
            }
        }

        return resNumber;
    }
}
