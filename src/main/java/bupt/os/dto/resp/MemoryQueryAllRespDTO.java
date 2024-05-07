package bupt.os.dto.resp;

import bupt.os.component.memory.lyq.BitMapEntry;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MemoryQueryAllRespDTO {
    List<BitMapEntry> list;
}
