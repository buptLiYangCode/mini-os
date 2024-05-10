package bupt.os.service;

import bupt.os.dto.req.ProcessCreateReqDTO;
import bupt.os.dto.resp.ProcessQueryAllRespDTO;

public interface ProcessManageService {
    void createProcess(ProcessCreateReqDTO processCreateReqDTO);

    void executeProcess(String processName);

    ProcessQueryAllRespDTO queryAllProcessInfo();

    void switchStrategy(String strategy);
}
