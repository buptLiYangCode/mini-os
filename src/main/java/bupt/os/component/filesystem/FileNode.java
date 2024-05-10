package bupt.os.component.filesystem;


import java.util.ArrayList;
import java.util.List;

public class FileNode {
    private String name;
    private boolean isDirectory;   //是否为目录
    private FileNode father;  //父目录
    private List<FileNode> children;  //目录中的内容
    private String content;  //若为文件，文件内容

    public FileNode(String name, boolean isDirectory, FileNode father) {
        this.name = name;
        this.isDirectory = isDirectory;
        this.father = father;
        if(isDirectory){
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
    public void setContent(String content) {
        this.content = content;
    }

    public boolean isDirectory() {
        return isDirectory;
    }

    public FileNode getFather() {
        return father;
    }

    public List<FileNode> getChildren() {
        return children;
    }

    public void deleteChildren(){
        children.clear();
        children = null;
    }


    public void deleteChild(FileNode child){
        for(FileNode Child : children){
            if(Child == child){
                children.remove(Child);
                break;
            }
        }
    }

    public void addChild(FileNode child) {
        children.add(child);
    }

}