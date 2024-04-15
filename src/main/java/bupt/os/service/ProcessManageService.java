package bupt.os.service;

import bupt.os.dto.req.ProcessCreateReqDTO;

public interface ProcessManageService {
    void createProcess(ProcessCreateReqDTO processCreateReqDTO);

    void executeProcess(String processName);
}
