package bupt.os.component.memory;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class IoRequest {
    private PCB pcb;
    private long useTime;
}
