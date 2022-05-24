package com.upixels.jh.hearingassist.entity;

import com.chad.library.adapter.base.entity.JSectionEntity;

public class BLEDeviceEntity extends JSectionEntity {

    private final boolean isHeader;

    public int      section; // 第几个section 0 ...

    public String   header;
    public String   mac;
    public String   deviceName;
    public int      connectStatus;
    public String   devType;        // 设备类型
    public boolean  isScanned;      // 标记是否是扫描到的，用于区分是扫描到的还是本地保存的设备

    public BLEDeviceEntity(boolean isHeader) {
        this.isHeader = isHeader;
    }

    @Override
    public boolean isHeader() {
        return this.isHeader;
    }

    // 获取MAC地址的最后4个字符
    public String getLast4CharMac() {
        String last4CharMac = "";
        if(mac != null) {
            String[] strings = mac.split(":");
            int len = strings.length;
            if (len > 2) { last4CharMac = strings[len-2] + strings[len-1]; }
        }
        return last4CharMac;
    }

    @Override
    public String toString() {
        return "BLEDeviceEntity{" +
                "isHeader=" + isHeader +
                ", section=" + section +
                ", header='" + header + '\'' +
                ", mac='" + mac + '\'' +
                ", deviceName='" + deviceName + '\'' +
                ", connectStatus='" + connectStatus + '\'' +
                ", devType=" + devType +
                ", isScanned=" + isScanned +
                '}';
    }
}
