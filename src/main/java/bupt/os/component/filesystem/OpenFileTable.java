package bupt.os.component.filesystem;

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