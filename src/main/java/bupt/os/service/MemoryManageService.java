package bupt.os.service;

import bupt.os.component.memory.user.BitMapEntry;

import java.util.List;

public interface MemoryManageService {
    List<BitMapEntry> queryAllMemoryInfo();
}
