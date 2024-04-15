package bupt.os.dto.req;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProcessCreateReqDTO {
    private String processName;
    private String[] instructions;
}
