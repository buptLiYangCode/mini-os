package bupt.os.component.filesystem;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;

public class FileSystem {
    private static final FileReader fileReader = FileReader.getInstance();
    private static final FileWriter fileWriter = FileWriter.getInstance();

    private FileNode root;
    private FileNode now_node;
    private String now_path;

    public FileNode getRoot(){
        return root;
    }

    public FileNode getNode(){
        return now_node;
    }

    private static FileSystem instance;
    private FileSystem() {
        root = new FileNode("/", true, null); //根目录
        now_path = "/";
        now_node = root;
    }

    public static FileSystem getInstance() {
        if (instance == null) {
            synchronized (FileSystem.class) {
                if (instance == null) {
                    instance = new FileSystem();
                }
            }
        }
        return instance;
    }

    public String getPath() {  //获取当前目录的路径
        return now_path;
    }

    public boolean isRepeated(String name, FileNode currentNode){  //检查是否重复
        for(FileNode child : currentNode.getChildren()){
            if(child.getName().equals(name))
                return true;
        }
        return false;
    }

    public boolean isExist(String path){   //检查路径是否存在

        if(path.charAt(0) != '/'){
            if(Objects.equals(now_path, "/")){
                path = now_path + path;
            }
            else{
                path = now_path + "/" + path;
            }
        }
        String[] parts = path.split("/");
        FileNode currentNode = root;
        boolean is_exist = false;
        for (int i = 1; i < parts.length; i++) {
            is_exist = false;
            for (FileNode child : currentNode.getChildren()) {
                if (child.getName().equals(parts[i])) {
                    is_exist = true;
                    currentNode = child;
                    break;
                }
            }
            if(!is_exist){
                break;
            }
        }
        return is_exist;
    }

    public String upperPath(){
        if(now_node == root){
            return "/";
        }
        if(now_node.getFather() == root){
            return "/";
        }
        StringBuilder upper_path = new StringBuilder();
        FileNode cur_node = now_node.getFather();
        while(cur_node.getFather()!= null){
            upper_path.insert(0, "/" + cur_node.getName());
            cur_node = cur_node.getFather();
        }
        return upper_path.toString();
    }

    public String cd(String path){   //cd操作:跳转到对应目录    参数 : 路径 ——> '..':上级路径  '/~~~'：绝对路径  'dir/~~~':相对路径
        if(Objects.equals(path, "..")){
            if(now_path.equals("/")){
                return now_path;
            }else{
                now_path = upperPath();
                now_node = now_node.getFather();
                return now_path;
            }
        }
        if(path.charAt(0) != '/'){
            if(Objects.equals(now_path, "/")){
                path = now_path + path;
            }
            else{
                path = now_path + "/" + path;
            }
        }
        if(!isExist(path)){
            return "No such file or directory";
        }
        String[] parts = path.split("/");
        FileNode currentNode = root;
        for (int i = 1; i < parts.length; i++) {
            for (FileNode child : currentNode.getChildren()) {
                if (child.getName().equals(parts[i])) {
                    currentNode = child;
                    break;
                }
            }
        }
        if(!currentNode.isDirectory()){
            return "Is a file";
        }
        now_node = currentNode;
        now_path = path;
        return now_path;
    }

    public String ls(){   //展示当前目录下的所有文件和目录
        StringBuilder contents = new StringBuilder();
        for (FileNode child : now_node.getChildren()){
            contents.append(child.getName()).append(" ");
        }
        return contents.toString();
    }

    public String cat(String path){   //展示对应文件的内容
        if(path.equals("/")){
            return "Is a directory";
        }
        if(path.charAt(0) != '/'){
            if(Objects.equals(now_path, "/")){
                path = now_path + path;
            }
            else{
                path = now_path + "/" + path;
            }
        }
        if (!isExist(path)){
            return "No such file or directory";
        }
        String[] parts = path.split("/");
        FileNode currentNode = root;
        for (int i = 1; i < parts.length; i++) {
            for (FileNode child : currentNode.getChildren()) {
                if (child.getName().equals(parts[i])) {
                    currentNode = child;
                    break;
                }
            }
        }
        if(currentNode.isDirectory()){
            return "Is a directory";
        }else{
            return currentNode.getContent();
        }
    }

    public String makedir(String path) {   //创建一个目录: 相对路径 / 绝对路径
        String[] parts = path.split("/"); ;
        FileNode currentNode;
        int i;
        if(path.charAt(0) != '/'){
            currentNode = now_node;
            i = 0;
        }
        else{
            i = 1;
            currentNode = root;
        }
        for (; i < parts.length; i++) {
            boolean found = false;
            for (FileNode child : currentNode.getChildren()) {
                if (child.getName().equals(parts[i])) {
                    currentNode = child;
                    found = true;
                    break;
                }
            }

            if (!found) {
                FileNode newNode = new FileNode(parts[i], true, currentNode);
                currentNode.addChild(newNode);
                currentNode = newNode;
            }
        }
        return "";
    }

    public String touch(String path) {   //创建一个文件: 相对路径 / 绝对路径
        String[] parts = path.split("/"); ;
        FileNode currentNode;
        int i;
        if(path.charAt(0) != '/'){
            currentNode = now_node;
            i = 0;
        }
        else{
            i = 1;
            currentNode = root;
        }
        for (; i < parts.length - 1; i++) {
            boolean found = false;
            for (FileNode child : currentNode.getChildren()) {
                if (child.getName().equals(parts[i])) {
                    currentNode = child;
                    found = true;
                    break;
                }
            }
            if (!found) {
                FileNode newNode = new FileNode(parts[i], true, currentNode);
                currentNode.addChild(newNode);
                currentNode = newNode;
            }
        }
        if(!isRepeated(parts[parts.length - 1], currentNode)){
            FileNode newNode = new FileNode(parts[parts.length - 1], false, currentNode);
            currentNode.addChild(newNode);
        }
        ConcurrentHashMap<FileNode, Semaphore> semaphoreTable1 = fileReader.getSemaphoreTable();
        semaphoreTable1.put(getFile(path), new Semaphore(3));
        ConcurrentHashMap<FileNode, Semaphore> semaphoreTable2 = fileWriter.getSemaphoreTable();
        semaphoreTable2.put(getFile(path), new Semaphore(1));
        return "";
    }


    public String rmdir(String path){
        if(isExist(path)){
            if(path.charAt(0) != '/'){
                if(Objects.equals(now_path, "/")){
                    path = now_path + path;
                }
                else{
                    path = now_path + "/" + path;
                }
            }
            String[] parts = now_path.split("/");
            FileNode currentNode = root;
            for (int i = 1; i < parts.length; i++) {
                for (FileNode child : currentNode.getChildren()) {
                    if (child.getName().equals(parts[i])) {
                        currentNode = child;
                        break;
                    }
                }
            }
            currentNode = null;
        }
        return "";
    }

    public String rmfile(String path){
        if(path.equals("/"))
        {
            root.deleteChildren();
        }
        if(isExist(path)){
            if(path.charAt(0) != '/'){
                if(Objects.equals(now_path, "/")){
                    path = now_path + path;
                }
                else{
                    path = now_path + "/" + path;
                }
            }
            String[] parts = path.split("/");
            FileNode currentNode = root;
            for (String part : parts) {
                for (FileNode child : currentNode.getChildren()) {
                    if (child.getName().equals(part)) {
                        currentNode = child;
                        break;
                    }
                }
            }
            FileNode fatherNode = currentNode.getFather();
            for (FileNode child : fatherNode.getChildren()) {
                if (child == currentNode) {
                    fatherNode.deleteChild(child);
                    break;
                }
            }
        }
        return "";
    }

    public String getStruct(FileNode node, int depth){   //获取当前目录的文件结构 -- 采用树的后根遍历，递归调用
        if(!node.isDirectory()){
            return "\n" + "├───" + "───".repeat(depth - 1) + node.getName();
        }
        else{
            if(node.getChildren() == null)
            {
                return "\n" + "├───".repeat(depth) + node.getName();
            }
            StringBuilder struct_info = new StringBuilder();
            for(FileNode child : node.getChildren()){
                struct_info.append(getStruct(child, depth + 1));
            }
            return "\n" + "├" +"───".repeat(depth) + node.getName() + struct_info;
        }
    }

    public String fileTree(){
        return getStruct(root, 0);
    }

    public String nowTree(){
        return getStruct(now_node, 0);
    }

    public FileNode getFile(String path){
        if(path.charAt(0) != '/'){
            if(Objects.equals(now_path, "/")){
                path = now_path + path;
            }
            else{
                path = now_path + "/" + path;
            }
        }
        if(!isExist(path)){
            return null;
        }
        String[] parts = path.split("/");
        FileNode currentNode = root;
        for (int i = 1; i < parts.length; i++) {
            for (FileNode child : currentNode.getChildren()) {
                if (child.getName().equals(parts[i])) {
                    currentNode = child;
                    break;
                }
            }
        }
        return currentNode;
    }
}