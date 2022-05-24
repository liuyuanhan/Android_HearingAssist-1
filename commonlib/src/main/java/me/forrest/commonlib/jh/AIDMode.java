package me.forrest.commonlib.jh;

import androidx.annotation.NonNull;

// 枚举类型每个常量在JVM中只有一个实例 如果都是CONVERSATION类型的枚举，如果改变了md,其它的等于CONVERSATION的枚举都改变了。
public class AIDMode implements Cloneable {
    // 模式1 ~ 模式4
    public static final byte CONVERSATION = 1; // DI0 = 0
    public static final byte RESTAURANT   = 2; // DI0 = 1
    public static final byte OUTDOOR      = 3; // DI0 = 2
    public static final byte MUSIC        = 4; // DI0 = 3
    public static final byte UNKNOWN      = -1;

    private String deviceName;
    private byte   mode;   // 模式号
    private byte   volume; // 音量大小
    private byte   type;   // 类型：读取返回类型/主动上报类型 Read_Success Report_Success

    @NonNull
    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    public AIDMode(byte mode) {
        this.mode = mode;
    }

    public AIDMode(String deviceName, byte mode, byte volume, byte type) {
        this.deviceName = deviceName;
        this.mode = mode;
        this.volume = volume;
        this.type = type;
    }

    public byte getVolume() {
        return this.volume;
    }

    public void setVolume(byte volume) {
        this.volume = volume;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public byte getMode() {
        return mode;
    }

    public void setMode(byte mode) {
        this.mode = mode;
    }

    public int getType() {
        return type;
    }

    public void setType(byte type) {
        this.type = type;
    }

    public byte getDI0() {
        byte DI0 = -1;
        switch (mode) {
            case CONVERSATION:
                DI0 = 0;
                break;
            case RESTAURANT:
                DI0 = 1;
                break;
            case OUTDOOR:
                DI0 = 2;
                break;
            case MUSIC:
                DI0 = 3;
                break;
        }
        return DI0;
    }


    @Override
    public String toString() {
        return "AIDMode{" +
                "deviceName='" + deviceName + '\'' +
                ", mode=" + mode +
                ", volume=" + volume +
                ", type=" + type +
                '}';
    }
}
