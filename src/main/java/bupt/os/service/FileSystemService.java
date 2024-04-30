package bupt.os.service;

import java.util.List;

public interface FileSystemService {

    String mkfile(String fileName);

    String mkdir(String directoryName);

    String rmdir(String directoryName);

    String rmfile(String fileName);

    List<String> ls();

    String struct();

    String cat();
}
