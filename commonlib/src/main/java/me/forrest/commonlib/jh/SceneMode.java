package me.forrest.commonlib.jh;

import me.forrest.commonlib.util.NumberUtil;

public enum SceneMode {
    //               md           DI0            volume
    CONVERSATION(   (byte)1,      (byte) 0,     (byte)0),
    RESTAURANT(     (byte)2,      (byte) 1,     (byte)0),
    OUTDOOR(        (byte)3,      (byte) 2,     (byte)0),
    MUSIC(          (byte)4,      (byte) 3,     (byte)0),
    UNKNOWN(        (byte)-1,     (byte) 4,     (byte)0);

    private String deviceName;
    private byte md;
    private byte mdToDI0; //读写模式文件时，模式对应的DI0值
    private byte volume;
    private byte type;  // Read_Success Report_Success

    SceneMode(byte md, byte mdToDI0, byte volume) {
        this.md = md;
        this.mdToDI0 = mdToDI0;
        this.volume = volume;
    }

    public byte getVolume() {
        return volume;
    }

    public SceneMode setVolume(byte volume) {
        this.volume = volume;
        return this;
    }

    public byte getMdToDI0() {
        return mdToDI0;
    }

    public void setMdToDI0(byte mdToDI0) {
        this.mdToDI0 = mdToDI0;
    }

    public byte getMd() {
        return md;
    }

    public void setMd(byte md) {
        this.md = md;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public byte getType() {
        return type;
    }

    public void setType(byte type) {
        this.type = type;
    }

    public static SceneMode mode(String deviceName, byte md, byte volume, byte type) {
        SceneMode sceneMode = UNKNOWN;
        if (md == 1) {
            sceneMode = CONVERSATION;
        } else if (md == 2) {
            sceneMode = RESTAURANT;
        } else if (md == 3) {
            sceneMode = OUTDOOR;
        } else if (md == 4) {
            sceneMode = MUSIC;
        }
        sceneMode.setDeviceName(deviceName);
        sceneMode.setVolume(volume);
        sceneMode.setType(type);
        return sceneMode;
    }

    @Override
    public String toString() {
        return "SceneMode{" +
                "deviceName='" + deviceName + '\'' +
                ", md=" + md +
                ", mdToDI0=" + mdToDI0 +
                ", volume=" + volume +
                ", type=0x" + NumberUtil.oneBytetoHex(type) +
                '}';
    }
}
