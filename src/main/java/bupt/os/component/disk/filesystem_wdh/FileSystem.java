package bupt.os.component.disk.filesystem_wdh;


// ls cd mkdir rm cat

public class FileSystem {
    private FileSystemNode root;
    private FileSystemNode now_node;
    private String now_path;

    public FileSystemNode getRoot(){
        return root;
    }

    public FileSystemNode getNode(){
        return now_node;
    }

    public FileSystem() {
        root = new FileSystemNode("/", true, null); //根目录
        now_path = "/";
        now_node = root;
    }

    public String getPath() {  //获取当前目录的路径
        return now_path;
    }

    public boolean isExist(String path){   //检查路径是否存在

        if(path.charAt(0) != '/'){
            path = now_path + "/" + path;
        }
        String[] parts = path.split("/");
        FileSystemNode currentNode = root;
        boolean is_exist = false;
        for (int i = 1; i < parts.length; i++) {
            is_exist = false;
            for (FileSystemNode child : currentNode.getChildren()) {
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
        String upper_path = "";
        FileSystemNode cur_node = now_node.getFather();
        while(cur_node.getFather()!= null){
            upper_path = "/" + cur_node.getName() + upper_path;
            cur_node = cur_node.getFather();
        }
        return upper_path;
    }

    public StatusMessage cd(String path){   //cd操作:跳转到对应目录    参数 : 路径 ——> '..':上级路径  '/~~~'：绝对路径  'dir/~~~':相对路径
        if(path.equals("..")){
            if(now_path.equals("/")){
                return new StatusMessage(true, now_path);
            }else{
                now_path = upperPath();
                now_node = now_node.getFather();
                return new StatusMessage(true, now_path);
            }
        }

        if(path.charAt(0) != '/'){
            path = "/" + path;
        }
        if(isExist(path)){
            return new StatusMessage(false, "No such file or directory");
        }
        now_path = path;
        String[] parts = now_path.split("/");
        parts[0] = "/";
        now_node = root;
        for (int i = 1; i < parts.length; i++) {
            for (FileSystemNode child : now_node.getChildren()) {
                if (child.getName().equals(parts[i])) {
                    now_node = child;
                    break;
                }
            }
        }
        return new StatusMessage(true, now_path);
    }

    public String ls(){   //展示当前目录下的所有文件和目录
        String contents = "";
        for (FileSystemNode child : now_node.getChildren()){
            contents += child.getName() + " ";
        }
        System.out.println(contents);
        return contents;
    }

    public StatusMessage cat(String path){   //展示对应文件的内容
        if(path.equals("/")){
            return new StatusMessage(false, "Is a directory");
        }
        if(path.charAt(0) != '/'){
            path = now_path + "/" + path;
        }
        if (isExist(path)){
            return new StatusMessage(false, "No such file or directory");
        }
        String[] parts = now_path.split("/");
        FileSystemNode currentNode = root;
        for (int i = 1; i < parts.length; i++) {
            for (FileSystemNode child : currentNode.getChildren()) {
                if (child.getName().equals(parts[i])) {
                    currentNode = child;
                    break;
                }
            }
        }
        if(currentNode.isDirectory()){
            return new StatusMessage(false, "Is a directory");
        }else{
            return new StatusMessage(false, currentNode.getContent());
        }
    }

    public boolean mkdir(String path) {   //创建一个目录: 相对路径 / 绝对路径
        if(path.charAt(0) != '/'){
            String[] parts = path.split("/");
            FileSystemNode currentNode = now_node;
            for (int i = 0; i < parts.length; i++) {
                boolean found = false;
                for (FileSystemNode child : currentNode.getChildren()) {
                    if (child.getName().equals(parts[i])) {
                        currentNode = child;
                        found = true;
                        break;
                    }
                }

                if (!found) {
                    FileSystemNode newNode = new FileSystemNode(parts[i], true, currentNode);
                    currentNode.addChild(newNode);
                    currentNode = newNode;
                }
            }

        }

        else{
            String[] parts = path.split("/");
            FileSystemNode currentNode = root;

            for (int i = 1; i < parts.length; i++) { //跳过根目录
                boolean found = false;
                for (FileSystemNode child : currentNode.getChildren()) {
                    if (child.getName().equals(parts[i])) {
                        currentNode = child;
                        found = true;
                        break;
                    }
                }

                if (!found) {
                    FileSystemNode newNode = new FileSystemNode(parts[i], true, currentNode);
                    currentNode.addChild(newNode);
                    currentNode = newNode;
                }
            }
        }
        return true;
    }

    public boolean mkfile(String path) {   //创建一个文件: 相对路径 / 绝对路径
        if(path.charAt(0) != '/'){
            String[] parts = path.split("/");
            FileSystemNode currentNode = now_node;
            for (int i = 0; i < parts.length; i++) {
                boolean found = false;
                for (FileSystemNode child : currentNode.getChildren()) {
                    if (child.getName().equals(parts[i])) {
                        currentNode = child;
                        found = true;
                        break;
                    }
                }

                if (!found) {
                    FileSystemNode newNode = new FileSystemNode(parts[i], false, currentNode);
                    currentNode.addChild(newNode);
                    currentNode = newNode;
                }
            }

        }

        else{
            String[] parts = path.split("/");
            FileSystemNode currentNode = root;

            for (int i = 1; i < parts.length; i++) { //跳过根目录
                boolean found = false;
                for (FileSystemNode child : currentNode.getChildren()) {
                    if (child.getName().equals(parts[i])) {
                        currentNode = child;
                        found = true;
                        break;
                    }
                }

                if (!found) {
                    FileSystemNode newNode = new FileSystemNode(parts[i], false, currentNode);
                    currentNode.addChild(newNode);
                    currentNode = newNode;
                }
            }
        }
        return true;
    }


    public String getStruct(FileSystemNode node, int depth){   //获取当前目录的文件结构 -- 采用树的后根遍历，递归调用
        if(!node.isDirectory()){
            return "\n" + "\t".repeat(depth) + node.getName();
        }
        String struct_info = "";
        for(FileSystemNode child : node.getChildren()){
            struct_info += getStruct(child, depth + 1);
        }
        return "\n" + "\t".repeat(depth) + node.getName() + struct_info;
    }

}