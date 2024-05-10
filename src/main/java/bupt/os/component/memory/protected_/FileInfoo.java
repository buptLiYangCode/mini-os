package bupt.os.component.memory.protected_;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.concurrent.ConcurrentLinkedQueue;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FileInfoo {
    private String fileName;
    private String filePath;
    private ConcurrentLinkedQueue<Integer> accessList;
    private boolean shared;
}
