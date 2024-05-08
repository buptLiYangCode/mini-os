package bupt.os.component.filesystem.filesystem_wdh;

import bupt.os.component.memory.ly.FileInfoo;
import bupt.os.component.memory.ly.PCB;
import bupt.os.component.memory.ly.ProtectedMemory;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;

public class FileReader {
    private static FileReader instance;
    private final ConcurrentHashMap<FileNode, Semaphore> semaphoreTable;

    private final ProtectedMemory protectedMemory = ProtectedMemory.getInstance();
    private final FileSystem fileSystem = FileSystem.getInstance();

    public FileReader() {
        this.semaphoreTable = new ConcurrentHashMap<>();
    }

    public static FileReader getInstance() {
        if (instance == null) {
            synchronized (FileReader.class) {
                if (instance == null) {
                    instance = new FileReader();
                }
            }
        }
        return instance;
    }

    public boolean readFile(PCB pcb, FileNode fileNode, long readTime) throws InterruptedException {
        // 计算剩余时间片
        long remainingTime = 2000L - (System.currentTimeMillis() - pcb.getStartTime());

        Semaphore semaphore = semaphoreTable.getOrDefault(fileNode, new Semaphore(3));
        semaphoreTable.put(fileNode, semaphore);
        boolean acquired = false;
        while (System.currentTimeMillis() - pcb.getStartTime() < remainingTime) {
            if (semaphore.tryAcquire()) {
                // 获取许可
                acquired = true;
                System.out.println(fileNode.getName() + "剩余permits：" + semaphore.availablePermits());
                HashMap<FileNode, FileInfoo> fileInfoTable = protectedMemory.getFileInfoTable();
                FileInfoo fileInfoo = fileInfoTable.getOrDefault(fileNode, new FileInfoo(fileNode.getName(), fileSystem.getPath(), new ConcurrentLinkedQueue<>(), true));
                fileInfoTable.put(fileNode, fileInfoo);
                ConcurrentLinkedQueue<Integer> accessList = fileInfoo.getAccessList();
                try {
                    accessList.add(pcb.getPid());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                // 模拟读取文件
                Thread.sleep(readTime);
                // 释放许可
                semaphore.release();
                accessList.remove(pcb.getPid());
                break;
            }
            Thread.sleep(100);
        }

        return acquired;
    }
}
