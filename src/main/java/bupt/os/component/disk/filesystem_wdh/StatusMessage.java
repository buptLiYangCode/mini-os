package bupt.os.component.disk.filesystem_wdh;

public class StatusMessage {
    private boolean is_success;
    private String info;

    public StatusMessage(boolean is_success, String info){
        this.is_success = is_success;  //操作标志 : 操作是否成功
        this.info = info;   //操作信息 : 返回具体内容或错误信息
    }

    public boolean get_status(){
        return is_success;
    }

    public String get_info(){
        return info;
    }
}
