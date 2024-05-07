package bupt.os.component.memory.ly;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 物理页信息
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PageInfo {
    // 对应的物理页号，如果在内存中
    private int pageNumber;
    // 页是否在物理内存中
    private boolean present;
}
