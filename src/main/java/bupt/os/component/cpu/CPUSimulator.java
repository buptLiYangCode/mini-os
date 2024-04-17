package bupt.os.component.cpu;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;

@Component
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CPUSimulator {

    // 创建固定大小为2的线程池
    private ExecutorService executor;


}
