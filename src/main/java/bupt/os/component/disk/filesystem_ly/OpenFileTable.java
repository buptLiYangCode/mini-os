package bupt.os.component.disk.filesystem_ly;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OpenFileTable {
    private HashMap<OpenFileInfo, INode> files;

}