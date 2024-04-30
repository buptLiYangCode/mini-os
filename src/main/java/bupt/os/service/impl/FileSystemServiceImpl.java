package bupt.os.service.impl;


import bupt.os.service.FileSystemService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FileSystemServiceImpl implements FileSystemService {

    @Override
    public String mkfile(String fileName) {
        return null;
    }

    @Override
    public String mkdir(String directoryName) {
        return null;
    }

    @Override
    public String rmdir(String directoryName) {
        return null;
    }

    @Override
    public String rmfile(String fileName) {
        return null;
    }

    @Override
    public List<String> ls() {
        return null;
    }

    @Override
    public String struct() {
        return null;
    }

    @Override
    public String cat() {
        return null;
    }
}