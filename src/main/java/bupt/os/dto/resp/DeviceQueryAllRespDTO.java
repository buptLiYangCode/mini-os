package bupt.os.dto.resp;

import bupt.os.component.memory.protected_.DeviceInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.LinkedList;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeviceQueryAllRespDTO {
    LinkedList<DeviceInfo> deviceInfoList;
}
