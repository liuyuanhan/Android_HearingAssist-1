package me.forrest.commonlib.jh;

import android.util.Log;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableEmitter;
import me.forrest.commonlib.util.BLEUtil;

public class BTProtocolD58B {

    private final static String TAG = "BTProtocolD58B";

    public enum D58BProgram {
        //        String deviceName, byte programe, byte volume
        CUSTOM   ("", (byte)0, (byte)0),
        STANDARD ("", (byte)1, (byte)0),
        DENOISE  ("", (byte)2, (byte)0),
        OUTDOOR  ("", (byte)3, (byte)0),
        UNKNOWN  ("", (byte)0, (byte)0);

        public String deviceName;
        public byte program;
        public byte volume;
        public byte type = BTProtocol.Read_Success; // 数据类型

        D58BProgram(String deviceName, byte program, byte volume) {
            this.deviceName = deviceName;
            this.program = program;
            this.volume = volume;
        }

        // D58B的模式转为对应的program值
        public byte D58BProgram2prog() {
            switch (this) {
                case CUSTOM:
                    return 0;
                case STANDARD:
                    return 1;
                case DENOISE:
                    return 2;
                case OUTDOOR:
                    return 3;
                case UNKNOWN:
                    return -1;
            }
            return 0;
        }

        // 通过program的值，生成D58BProgram
        public static D58BProgram prog2D58BProgram(String deviceName, byte prog, byte type) {
            D58BProgram d58BProgram = UNKNOWN;
            switch (prog) {
                case 0:
                    d58BProgram = CUSTOM;
                    break;
                case 1:
                    d58BProgram = STANDARD;
                    break;
                case 2:
                    d58BProgram = DENOISE;
                    break;
                case 3:
                    d58BProgram = OUTDOOR;
            }
            d58BProgram.deviceName = deviceName;
            d58BProgram.type = type;
            return d58BProgram;
        }

        // 切换模式时，需要将场景模式转为 program进行设置
        public static byte SceneMode2prog(SceneMode sceneMode) {
            switch (sceneMode) {
                case CONVERSATION:
                    return 0;
                case RESTAURANT:
                    return 1;
                case OUTDOOR:
                    return 2;
                case MUSIC:
                    return 3;
            }
            return 0;
        }

        // 转为SceneMode 便于和W3统一处理
        public SceneMode D58BProgram2SceneMode() {
            SceneMode sceneMode = SceneMode.CONVERSATION;
            switch (this) {
                case CUSTOM:
                    sceneMode = SceneMode.CONVERSATION;
                    break;
                case STANDARD:
                    sceneMode = SceneMode.RESTAURANT;
                    break;
                case DENOISE:
                    sceneMode = SceneMode.OUTDOOR;
                    break;
                case OUTDOOR:
                    sceneMode = SceneMode.MUSIC;
                    break;
            }
            sceneMode.setDeviceName(this.deviceName);
            sceneMode.setType(this.type);
            return sceneMode;
        }
    }

    public final static BTProtocolD58B share = new BTProtocolD58B();

    private BTProtocolD58B() {
        initObservable();
    }

    // 获取音量等级
    public void getVolumeLevel(String mac) {
        BLEUtil.getInstance().readCharacteristic(mac, BLEUtil.D58B_User_Interface_Service_UUID, BLEUtil.D58B_Volume_Level_Char_UUID);
    }

    // 设置音量等级
    public void setVolumeLevel(String mac, byte volume) {
        byte[] data = new byte[]{volume};
        BLEUtil.getInstance().writeCharacteristic(mac, BLEUtil.D58B_User_Interface_Service_UUID, BLEUtil.D58B_Volume_Level_Char_UUID, data);
    }

    // 获取当前Program
    public void getProgram(String mac) {
        BLEUtil.getInstance().readCharacteristic(mac, BLEUtil.D58B_User_Interface_Service_UUID, BLEUtil.D58B_Program_Char_UUID);
    }

    // 设置当前Program
    public void setProgram(String mac, byte prog) {
        byte[] data = new byte[] {prog};
        BLEUtil.getInstance().writeCharacteristic(mac, BLEUtil.D58B_User_Interface_Service_UUID, BLEUtil.D58B_Program_Char_UUID, data);
    }

    // 获取电量等级
    public void getBatteryLevel(String mac) {
        BLEUtil.getInstance().readCharacteristic(mac, BLEUtil.D58B_User_Interface_Service_UUID, BLEUtil.D58B_Battery_Level_Char_UUID);
    }

    // 获取设备设置
    public void getDeviceSettings(String mac) {
        BLEUtil.getInstance().readCharacteristic(mac, BLEUtil.D58B_User_Interface_Service_UUID, BLEUtil.D58B_Device_Settings_Char_UUID);
    }

    // 设置命令接口
    public void setCmdInterface(String mac, byte cmd) {
        byte[] data = new byte[] {cmd};
        BLEUtil.getInstance().writeCharacteristic(mac, BLEUtil.D58B_User_Interface_Service_UUID, BLEUtil.D58B_Cmd_Interface_Char_UUID, data);
    }

    // 音频算法接口
//    func readRPS(name: String) {
//        BLEUtil.share.read(name: name, serviceUUIDString: D58B_RPS_Service_UUID, charUUIDString: D58B_RPS_Read_Char_UUID)
//    }

    public void writeRPS(String mac, byte[] data) {
        BLEUtil.getInstance().writeCharacteristic(mac, BLEUtil.D58B_RPS_Service_UUID, BLEUtil.D58B_RPS_Write_Char_UUID, data);
    }

    public void unlockRPS(String mac) {
        byte[] data = new byte[] {0x45, 0x23, (byte) 0xA8, (byte) 0xB1, 0x67};
        BLEUtil.getInstance().writeCharacteristic(mac, BLEUtil.D58B_RPS_Service_UUID, BLEUtil.D58B_RPS_Unlock_UUID, data);
    }

    // 读取DSP program File, program = 0x00~0x04, 先发命令，然后获取数据
    public void readDSPProgramFile(String mac, byte program) {
        writeRPS(mac, new byte[] {0x20, program});
    }

    public void writeDSPProgramFile(String mac, BTProtocol.ModeFileContent modeFile, byte program) {
        byte[] data = new byte[14];
        data[0]  = (byte) 0x21;
        data[1]  = (byte) program; // !! 编号0 为自定义模式
        data[2]  = (byte) ((modeFile.NC2 << 1) | (modeFile.PG2 >> 2));                                              // D1
        data[3]  = (byte) ((modeFile.NC1 & 0x07) | (modeFile.PG1 << 3) | (modeFile.PG2 << 6));                      // D0
        data[4]  = (byte) (modeFile.EQ3 | (modeFile.EQ4 << 4));                                                     // D3
        data[5]  = (byte) (modeFile.EQ1 | (modeFile.EQ2 << 4));                                                     // D2
        data[6]  = (byte) (modeFile.EQ7 | (modeFile.EQ8 << 4)) ;                                                    // D5
        data[7]  = (byte) (modeFile.EQ5 | (modeFile.EQ6 << 4));                                                     // D4
        data[8]  = (byte) (modeFile.EQ11 | (modeFile.EQ12 << 4));                                                   // D7
        data[9]  = (byte) (modeFile.EQ9 | (modeFile.EQ10 << 4));                                                    // D6
        data[10] = (byte) ((modeFile.CR3 >> 2) | (modeFile.CR4 << 1) | (modeFile.CT << 4) | (modeFile.Expan << 7)); // D9
        data[11] = (byte) (modeFile.CR1 | (modeFile.CR2 << 3) | ((modeFile.CR3 & 0x03) << 6));                      // D8
        data[12] = (byte) (modeFile.NC4 | (modeFile.MPO>>2) | modeFile.NR << 1);                                    // D11
        data[13] = (byte) ((modeFile.MPO << 6) | modeFile.NC3);                                                     // D10
        writeRPS(mac, data);
    }

    // 检查包的完整性，并解析，提供反馈
    public boolean checkAndPraseFeedback(String uuidString, String name, byte[] frame, byte type) {
//        if checkFeedback(name: name, frame: frame) {
        //        parseFeedback(name: name, frame: frame)
//        }
        String _uuidString = uuidString;
        if (uuidString.length() > 4) {
            _uuidString = uuidString.substring(4, 8);
        }
        if (_uuidString.equals(BLEUtil.D58B_Program_Char_UUID) && (type == BTProtocol.Read_Success || type == BTProtocol.Report_Success)) {
            Log.d(TAG, "program " + name + "," + frame[0]);
            if (d58BProgramObservableEmitter != null) d58BProgramObservableEmitter.onNext(D58BProgram.prog2D58BProgram(name, frame[0], type));
            return true;

        } else if (_uuidString.equals(BLEUtil.D58B_Volume_Level_Char_UUID) && (type == BTProtocol.Read_Success || type == BTProtocol.Report_Success)) {
            Log.d(TAG, "volume " + name + "," + frame[0]);
            if (volumeObservableEmitter != null) volumeObservableEmitter.onNext(name + ":" + frame[0]);
            return true;

        } /* else if _uuidString == D58B_Battery_Level_Char_UUID {
            if name.hasSuffix("-L") {
                BLEUtil.share.rLeftBattery.accept(Int(frame[0]))
            } else if name.hasSuffix("-R") {
                BLEUtil.share.rRightBattery.accept(Int(frame[0]))
            }
            return true

        } */ else if (_uuidString.equals(BLEUtil.D58B_RPS_Read_Char_UUID)) { // 读取到DSP program File  20 00 ff xx xx ...
            if (frame[0] == (byte) 0x20 && frame[1] < (byte) 5 && frame[2] == (byte) 0xFF) {
                BTProtocol.ModeFileContent fileContent = new BTProtocol.ModeFileContent();

                // 用于转为无符号数处理
                int d0  = frame[4]  & 0xff;  // 小端转大端 16位
                int d1  = frame[3]  & 0xff;
                int d2  = frame[6]  & 0xff;
                int d3  = frame[5]  & 0xff;
                int d4  = frame[8]  & 0xff;
                int d5  = frame[7]  & 0xff;
                int d6  = frame[10] & 0xff;
                int d7  = frame[9]  & 0xff;
                int d8  = frame[12] & 0xff;
                int d9  = frame[11] & 0xff;
                int d10 = frame[14] & 0xff;
                int d11 = frame[13] & 0xff;

                fileContent.mode = D58BProgram.prog2D58BProgram(name, frame[1], type).D58BProgram2SceneMode();

                fileContent.NC1 = (byte) (d0 & 0x07);
                fileContent.NC2 = (byte) (d1 >> 1);
                fileContent.PG1 = (byte) ((d0 >> 3) & 0x07);
                fileContent.PG2 = (byte) ((d0 >> 6) | ((d1 & 0x01) << 2));
                fileContent.EQ1 = (byte) (d2 & 0x0F);
                fileContent.EQ2 = (byte) (d2 >> 4);
                fileContent.EQ3 = (byte) (d3 & 0x0F);
                fileContent.EQ4 = (byte) (d3 >> 4);
                fileContent.EQ5 = (byte) (d4 & 0x0F);
                fileContent.EQ6 = (byte) (d4 >> 4);
                fileContent.EQ7 = (byte) (d5 & 0x0F);
                fileContent.EQ8 = (byte) (d5 >> 4);
                fileContent.EQ9 = (byte) (d6 & 0x0F);
                fileContent.EQ10 = (byte) (d6 >> 4);
                fileContent.EQ11 = (byte) (d7 & 0x0F);
                fileContent.EQ12 = (byte) (d7 >> 4);
                fileContent.CR1 = (byte) (d8 & 0x7);  // d8 0000 0___ 后三位
                fileContent.CR2 = (byte) ((d8 >> 3) & 0x07); // d8 00__ _000
                fileContent.CR3 = (byte) (((d9 << 2) | (d8 >> 6)) & 0x07);
                fileContent.CR4 = (byte) ((d9 >> 1) & 0x07);
                fileContent.CT  = (byte) ((d9 >> 4) & 0x07);
                fileContent.Expan = (byte) (d9 >> 7);
                fileContent.MPO = (byte) (((d11 << 2) | d10 >> 6) & 0x07);
                fileContent.NR = (byte) ((d11 >> 1) & 0x03);
                fileContent.NC3 = (byte) (d10 & 0x3F);
                fileContent.NC4 = (byte) (d11 & 0xF8);
                Log.d(TAG, "[BTProtocolD58B] 读 蓝牙模块 成功 DSP模式文件: " + fileContent);
                if (modeFileContentObservableEmitter != null) modeFileContentObservableEmitter.onNext(fileContent);
                return true;

            // 写入成功 21 00 FF xx ...... (00:表示使用0为自定义模式文件编号)
            } else if (frame[0] == (byte) 0x21 && (frame[1] == (byte)0 || frame[1] == (byte)3) && frame[2] == (byte)0xFF) {
                Log.d(TAG, "[BTProtocolD58B] 写 蓝牙模块 成功 DSP模式文件 " + name);
                if (writeDSPFeedbackObservableEmitter != null) writeDSPFeedbackObservableEmitter.onNext(name + "," + true);
                return true;
            }
        }
        return false;
    }



    private boolean initObservableEnable;
    public Observable<D58BProgram> d58BProgramObservable;                // 被观察者 模式
    private ObservableEmitter<D58BProgram> d58BProgramObservableEmitter;
    public Observable<String> volumeObservable;                          // 被观察者 音量 name:volume
    private ObservableEmitter<String> volumeObservableEmitter;
    public Observable<String> ctlFeedbackObservable;                 // 被观察者 控制命令反馈
    private ObservableEmitter<String> ctlFeedbackObservableEmitter;
    public Observable<String> writeDSPFeedbackObservable;               // 被观察者 写DSP命令反馈
    private ObservableEmitter<String> writeDSPFeedbackObservableEmitter;
    public Observable<String> writeFeedbackObservable;               // 被观察者 写命令反馈
    private ObservableEmitter<String> writeFeedbackObservableEmitter;
    public Observable<BTProtocol.ModeFileContent> modeFileContentObservable;    // 被观察者 模式文件
    private ObservableEmitter<BTProtocol.ModeFileContent> modeFileContentObservableEmitter;
    public Observable<String> deviceVersionObservable;               // 被观察者 版本号
    private ObservableEmitter<String> deviceVersionObservableEmitter;
    public Observable<String> ledTypeObservable;                     // 被观察者 LED设置类型
    private ObservableEmitter<String> ledTypeObservableEmitter;

    // 初始化被观察者
    private void initObservable() {
        if (initObservableEnable) return;
        initObservableEnable = true;
        d58BProgramObservable = Observable.create(emitter -> d58BProgramObservableEmitter = emitter);
        volumeObservable = Observable.create(emitter -> volumeObservableEmitter = emitter);
        writeDSPFeedbackObservable = Observable.create(emitter -> writeDSPFeedbackObservableEmitter = emitter);

        ctlFeedbackObservable = Observable.create(emitter -> ctlFeedbackObservableEmitter = emitter);
        writeFeedbackObservable = Observable.create(emitter -> writeFeedbackObservableEmitter = emitter);
        modeFileContentObservable = Observable.create(emitter -> modeFileContentObservableEmitter = emitter);
        deviceVersionObservable = Observable.create(emitter -> deviceVersionObservableEmitter = emitter);
        ledTypeObservable = Observable.create(emitter -> ledTypeObservableEmitter = emitter);
    }
}
