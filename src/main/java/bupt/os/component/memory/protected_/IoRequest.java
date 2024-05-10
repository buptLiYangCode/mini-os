package bupt.os.component.memory.protected_;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class IoRequest {
    private PCB pcb;
    private long useTime;
    private long threadId;
}
