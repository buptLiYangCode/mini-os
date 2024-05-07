package bupt.os.service;

import bupt.os.component.memory.lyq.BitMapEntry;

import java.util.List;

public interface MemoryManageService {
    List<BitMapEntry> queryAllMemoryInfo();
}
