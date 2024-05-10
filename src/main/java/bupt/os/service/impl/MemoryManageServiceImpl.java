package bupt.os.service.impl;

import bupt.os.component.memory.user.BitMapEntry;
import bupt.os.component.memory.user.MemoryManagementImpl;
import bupt.os.service.MemoryManageService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MemoryManageServiceImpl implements MemoryManageService {

private final MemoryManagementImpl mmu = new MemoryManagementImpl();

    @Override
    public List<BitMapEntry> queryAllMemoryInfo() {
        return mmu.getPageUsageBitmap();
    }
}
