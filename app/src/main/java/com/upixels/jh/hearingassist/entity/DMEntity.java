package com.upixels.jh.hearingassist.entity;

import com.chad.library.adapter.base.entity.JSectionEntity;

public class DMEntity extends JSectionEntity {

    private final boolean isHeader;

    public int     section;      // 第几个section 0 ...
    public String  header;
    public boolean isFord;
    public String  filename;

    public String  itemName;  // 帮助和选项界面 每项的名字
    public String  itemSub;   // 子title 显示版本号
    public boolean showRedPointer; // 是否显示提示红点

    public String  language;  // 语言
    public boolean checked;   // 是否选中

    public DMEntity(boolean isHeader) {
        this.isHeader = isHeader;
    }

    public DMEntity(boolean isHeader, String filename, int section) {
        this.isHeader = isHeader;
        this.filename = filename;
        this.section = section;
    }

    public DMEntity(boolean isHeader, String itemName) {
        this.isHeader = isHeader;
        this.itemName = itemName;
    }

    public DMEntity(boolean isHeader, String itemName, String itemSub) {
        this.isHeader = isHeader;
        this.itemName = itemName;
        this.itemSub = itemSub;
    }

    public DMEntity(boolean isHeader, String itemName, boolean checked) {
        this.isHeader = isHeader;
        this.itemName = itemName;
        this.checked = checked;
    }

    public DMEntity(String filename, boolean checked) {
        this.isHeader = false;
        this.filename = filename;
        this.checked = checked;
    }

    @Override
    public boolean isHeader() {
        return this.isHeader;
    }

    @Override
    public String toString() {
        return "DMEntity{" +
                "isHeader=" + isHeader +
                ", section=" + section +
                ", header='" + header + '\'' +
                ", isFord=" + isFord +
                ", filename='" + filename + '\'' +
                '}';
    }
}
