package bupt.os.component.disk.filesystem_wdh;


import java.util.ArrayList;
import java.util.List;

public class FileSystemNode {
    private String name;
    private boolean isDirectory;   //是否为目录
    private FileSystemNode father;  //父目录
    private List<FileSystemNode> children;  //目录中的内容
    private String content;  //若为文件，文件内容

    public FileSystemNode(String name, boolean isDirectory, FileSystemNode father) {
        this.name = name;
        this.isDirectory = isDirectory;
        this.father = father;
        if(isDirectory == true){
            this.children = new ArrayList<>();
            this.content = null;
        }else{
            this.children = null;
            this.content = "";
        }
    }

    public String getName() {
        return name;
    }

    public String getContent() {
        return content;
    }

    public boolean isDirectory() {
        return isDirectory;
    }

    public FileSystemNode getFather() {
        return father;
    }

    public List<FileSystemNode> getChildren() {
        return children;
    }

    public void deleteChildren(){
        children.clear();
        children = null;
    }


    public void deleteChild(FileSystemNode child){
        for(FileSystemNode Child : children){
            if(Child == child){
                children.remove(Child);
                break;
            }
        }
    }

    public void addChild(FileSystemNode child) {
        children.add(child);
    }

}