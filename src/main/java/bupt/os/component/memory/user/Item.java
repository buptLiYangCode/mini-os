package bupt.os.component.memory.user;


import java.util.Objects;


public class Item {
    /**
     * 页号
     */
    private int pageIndex;

    /**
     * 物理块号
     */
    private byte block;

    /**
     * 状态位，0表示不在内存中，1表示在内存中
     */
    private byte state;

    /**
     * 访问字段
     */
    private byte access;

    /**
     * 修改位，0表示未修改，1表示修改
     */
    private byte change;

    /**
     * 外存地址
     */
    private int disk;

    public Item(int pageIndex, int disk, byte change, byte access, byte state, byte block) {
        this.pageIndex = pageIndex;
        this.disk = disk;
        this.change = change;
        this.access = access;
        this.state = state;
        this.block = block;
    }

    public int getPageIndex() {
        return pageIndex;
    }

    public void setPageIndex(int pageIndex) {
        this.pageIndex = pageIndex;
    }

    public int getDisk() {
        return disk;
    }

    public void setDisk(int disk) {
        this.disk = disk;
    }

    public byte getChange() {
        return change;
    }

    public void setChange(byte change) {
        this.change = change;
    }

    public byte getAccess() {
        return access;
    }

    public void setAccess(byte access) {
        this.access = access;
    }

    public byte getState() {
        return state;
    }

    public void setState(byte state) {
        this.state = state;
    }

    public byte getBlock() {
        return block;
    }

    public void setBlock(byte block) {
        this.block = block;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Item item = (Item) o;
        return pageIndex == item.pageIndex &&
                block == item.block &&
                state == item.state &&
                access == item.access &&
                change == item.change &&
                disk == item.disk;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pageIndex, block, state, access, change, disk);
    }
}