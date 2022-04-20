package com.upixels.jh.hearingassist.entity;

import com.chad.library.adapter.base.entity.SectionEntity;

public class RecordEntity implements SectionEntity {

    public boolean isEmptyFlag; // 如何没有数据时，显示一个空的item图片
    public boolean isSelected; // 标记是否选中
    public String filename;

    public RecordEntity(boolean isEmptyFlag, boolean isSelected, String filename) {
        this.isEmptyFlag = isEmptyFlag;
        this.isSelected = isSelected;
        this.filename = filename;
    }

    @Override
    public boolean isHeader() {
        return false;
    }

    @Override
    public int getItemType() {
        return SectionEntity.NORMAL_TYPE;
    }
}
