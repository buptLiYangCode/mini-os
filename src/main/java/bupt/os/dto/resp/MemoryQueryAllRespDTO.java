package bupt.os.dto.resp;

import bupt.os.component.memory.PageSwapInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.LinkedList;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MemoryQueryAllRespDTO {
    LinkedList<PageSwapInfo> pageSwapInfoList;
}
