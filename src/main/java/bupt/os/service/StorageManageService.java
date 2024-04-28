package bupt.os.service;

import bupt.os.dto.resp.MemoryQueryAllRespDTO;

public interface StorageManageService {
    MemoryQueryAllRespDTO queryAllMemoryInfo();
}
