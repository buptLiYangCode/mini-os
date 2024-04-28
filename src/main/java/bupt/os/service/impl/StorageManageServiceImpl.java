package bupt.os.service.impl;

import bupt.os.component.memory.PageSwapInfo;
import bupt.os.component.memory.ProtectedMemory;
import bupt.os.component.memory.UserMemory;
import bupt.os.dto.resp.MemoryQueryAllRespDTO;
import bupt.os.service.StorageManageService;
import org.springframework.stereotype.Service;

import java.util.LinkedList;

@Service
public class StorageManageServiceImpl implements StorageManageService {

    private final UserMemory userMemory = UserMemory.getInstance();
    private final ProtectedMemory protectedMemory = ProtectedMemory.getInstance();

    @Override
    public MemoryQueryAllRespDTO queryAllMemoryInfo() {

        LinkedList<PageSwapInfo> pageSwapInfoList = protectedMemory.getAllPageInfo();
        return new MemoryQueryAllRespDTO(pageSwapInfoList);
    }
}
