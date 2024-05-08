package bupt.os.component.filesystem.filesystem_wdh;

import bupt.os.component.memory.ly.FileInfoo;
import bupt.os.component.memory.ly.PCB;
import bupt.os.component.memory.ly.ProtectedMemory;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;

public class FileWriter {
    private static FileWriter instance;
    private final ConcurrentHashMap<FileNode, Semaphore> semaphoreTable;

    private final ProtectedMemory protectedMemory = ProtectedMemory.getInstance();
    private final FileSystem fileSystem = FileSystem.getInstance();

    public FileWriter() {
        this.semaphoreTable = new ConcurrentHashMap<>();
    }

    public static FileWriter getInstance() {
        if (instance == null) {
            synchronized (FileWriter.class) {
                if (instance == null) {
                    instance = new FileWriter();
                }
            }
        }
        return instance;
    }

    public boolean writeFile(PCB pcb, FileNode fileNode, long writeTime, String data) throws InterruptedException {

        Semaphore semaphore = semaphoreTable.getOrDefault(fileNode, new Semaphore(1));
        semaphoreTable.put(fileNode, semaphore);
        boolean acquired = false;
        while (System.currentTimeMillis() - pcb.getStartTime() < 2000L) {
            if (semaphore.tryAcquire()) {
                // 获取许可
                acquired = true;

                HashMap<FileNode, FileInfoo> fileInfoTable = protectedMemory.getFileInfoTable();
                FileInfoo fileInfoo = fileInfoTable.getOrDefault(fileNode, new FileInfoo(fileNode.getName(), fileSystem.getPath(), new ConcurrentLinkedQueue(), true));
                fileInfoTable.put(fileNode, fileInfoo);
                ConcurrentLinkedQueue<Integer> accessList = fileInfoo.getAccessList();
                accessList.add(pcb.getPid());
                // 设置文件模式为非共享
                fileInfoo.setShared(false);
                // data写入文件，模拟消耗的时间
                Thread.sleep(writeTime);
                fileNode.setContent(fileNode.getContent() + data);
                // 释放许可
                semaphore.release();
                fileInfoo.setShared(true);
                accessList.remove(pcb.getPid());
                break;
            }
            Thread.sleep(100);
        }

        return acquired;
    }
}

