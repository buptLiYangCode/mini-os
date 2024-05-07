package bupt.os.controller;

import bupt.os.common.result.Result;
import bupt.os.common.result.Results;
import bupt.os.component.memory.lyq.BitMapEntry;
import bupt.os.dto.resp.MemoryQueryAllRespDTO;
import bupt.os.service.MemoryManageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedList;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
public class MemoryController {
    private final MemoryManageService memoryManageService;

    /**
     * 查询所有页信息
     * @return info
     */
    @GetMapping("/mini-os/memory/query-all")
    public Result<MemoryQueryAllRespDTO> queryAllMemoryInfo() {
        List<BitMapEntry> bitMapEntries = memoryManageService.queryAllMemoryInfo();
        MemoryQueryAllRespDTO memoryQueryAllRespDTO = new MemoryQueryAllRespDTO(bitMapEntries);
        List<Integer> list = new LinkedList<>();
        list.add(1);
        list.add(1);
        list.add(1);
        return Results.success(memoryQueryAllRespDTO);
    }
}
