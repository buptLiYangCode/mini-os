package bupt.os.component.memory.lyq;


import java.util.List;

//内存管理模块对外接口
public interface MemoryManagement {
    /**
     * 查询成功
     */
    public static final int SUCCESS = 0;

    /**
     * 缺页
     */
    public static final int PAGE_FAULT = -1;

    /**
     * 越界
     */
    public static final int BOUND_FAULT = -2;

    /**
     * 内存映射，表示当前页被PID为数组内容的进程所占有，第二项表示虚拟页号，如果是页表对应的页框，那么代表驻留集大小
     */
    public static final int[][] bitMap = new int[Memory.pageNums][2];


    /**
     * 使用Allocate之前需要Init()
     * 给进程分配指定大小的内存空间，并返回分配的页表地址，首先分配页表，然后分配几个页。页表和快表内容都要做对应修改
     * 目前暂不支持动态驻留集
     * 返回-1表示内存不够了
     * @param PID 进程PID
     * @param size 分配的内存大小，byte，暂时无用
     * @return 分配的页表内存地址
     */
    public int Allocate(int PID, int size);


    /**
     * 释放指定进程的内存空间。
     * @param register 待释放的进程的页表地址
     * @return 是否成功释放内存空间
     */
    public boolean Free(int register);


    /**
     * 从指定地址的内存中读取数据。默认读取4个字节的数据
     * 注意：根据返回值的不同，data有两种含义
     *
     * @param register     内存地址
     * @param logicAddress 进程逻辑地址
     * @param data 实际返回的数据,传入时不小于4byte,如果是发生了缺页的情况下，这里返回的是要被换走的页号，需要在中断完成后传回，转为int后即为要被换走的页号，无论传入的data数组多大，当
     *      *             返回值为页号时，前四个byte转为int即可
     * @return 读取结果结果有SUCCESS、PAGE_FAULT、BOUND_FAULT
     */
    public int Read(int register, int logicAddress,byte[] data);

    /**
     * 向指定地址的内存中写入数据。
     * 注意：根据返回值的不同，data有两种含义
     *
     * @param register     内存地址
     * @param logicAddress 进程逻辑地址
     * @param data 要写入的数据，不小于4byte，同理，如果缺页，会在这里返回4个byte的数据，转为int后即为要被换走的页号，无论传入的data数组多大，当
     *             返回值为页号时，前四个byte转为int即可
     * @return 写入是否成功
     */
    public int Write(int register, int logicAddress, byte[] data);



    /**
     * 获取内存的页面使用情况位图。
     *
     * @return 页使用情况位图，格式为Json字符串，具体格式为前端在飞书文档中所要求
     */
    public List<BitMapEntry> getPageUsageBitmap();


    /**
     * 初始化内存管理系统，内存系统启用之前必须调用一次
     */
    public void InitMemory();

    /**
     * 缺页处理完成后需要调用的函数
     *
     * @param register  页表寄存器
     * @param logicalAddress  要访问的逻辑地址
     * @param oldPage       要被换出的页号，由read()和write()返回的bytep[] data转为 int 得到
     */
    public void PageFaultProcess(int register , int logicalAddress , int oldPage);

}
