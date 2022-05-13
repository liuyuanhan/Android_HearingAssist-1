package me.forrest.commonlib.jh;

import android.util.Log;

import java.util.HashMap;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableEmitter;

public class BTProtocol {
    private final static String TAG = "BTProtocol";
    // 自定义模式的正确数据：fe 68 91 e 0 3 a9 d3 71 98 78 a6 8b 12 2 c2 0 4 12 16
    // NC1: 1, NC2: 105, NC3: 0, NC4: 0, PG1: 5, PG2: 6,
    // EQ1: 1, EQ2: 7,   EQ3: 8, EQ4: 9, EQ5: 8, EQ6: 7, EQ7: 6, EQ8: 10, EQ9: 11, EQ10: 8, EQ11: 2, EQ12: 1,
    // CR1: 2, CR2: 0,   CR3: 0, CR4: 1, MIC: 1, CT: 4,  MPO: 0, NR: 2

    // JH-W3项目DSP默认文件 2021-10-16
    public final static byte[] DSP_mode_file1  = new byte[] {(byte)0xA9, (byte)0xD3, (byte)0x71, (byte)0x98, (byte)0x78, (byte)0xA6, (byte)0x8B, (byte)0x01, (byte)0x02, (byte)0xC2, (byte)0x00, (byte)0x04};
    public final static byte[] DSP_mode_file2  = new byte[] {(byte)0xA9, (byte)0xD3, (byte)0x71, (byte)0x98, (byte)0x78, (byte)0xA6, (byte)0x8B, (byte)0x01, (byte)0x02, (byte)0xC2, (byte)0x00, (byte)0x04};
    public final static byte[] DSP_mode_file3  = new byte[] {(byte)0xB4, (byte)0xE9, (byte)0xA7, (byte)0xAA, (byte)0x79, (byte)0x66, (byte)0x16, (byte)0x00, (byte)0x4B, (byte)0xC4, (byte)0x41, (byte)0x06};
    public final static byte[] DSP_mode_file4  = new byte[] {(byte)0xA9, (byte)0xD9, (byte)0x80, (byte)0xAA, (byte)0x8A, (byte)0x87, (byte)0x78, (byte)0x07, (byte)0x53, (byte)0xB4, (byte)0x49, (byte)0x06};
    public final static byte[] DSP_global_file = new byte[] {(byte)0x00, (byte)0x98, (byte)0xD0, (byte)0x00, (byte)0xC4, (byte)0x07, (byte)0x1A, (byte)0x14, (byte)0x0C};
    // 啸叫DSP文件
    public final static byte[] DSP_mode_file_beep = new byte[] {(byte)0xB9, (byte)0x69, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0x42, (byte)0xC6, (byte)0x00, (byte)0x04};

    // EQ 与 增益
    public final static float[] EQ_GAIN_250 = new float[]{15f, 15f, 14f, 16f, 14.5f, 15f, 16.5f, 15.5f, 17f, 18.75f, 18f, 20f, 21f, 21.25f, 23f, 22.5f};
    public final static float[] EQ_GAIN_500 = new float[]{15f,15.25f, 17.75f, 20.5f, 22.25f, 25f, 26.75f, 29.25f, 31f, 33.75f, 35f, 37f, 39.75f, 40.5f, 43f, 45f};
    public final static float[] EQ_GAIN_1000 = new float[]{12.25f, 14.5f, 16.25f, 18.5f, 20.5f, 22.75f, 24.25f, 26f, 28.75f, 30.5f, 32.75f, 34.5f, 36.5f, 38.5f, 40.5f, 42.5f};
    public final static float[] EQ_GAIN_1500 = new float[]{12.25f, 14.25f, 16f, 17.25f, 20.25f, 22f, 24.75f, 26f, 27.5f, 31f, 32.25f, 33f, 36.25f, 37.75f, 40f, 42.5f};
    public final static float[] EQ_GAIN_2000 = new float[]{12.75f, 14.5f, 16f, 18f, 19.75f, 21.5f, 24f, 25.75f, 26.75f, 29f, 31f, 33.5f, 36.5f, 37.25f, 38.5f, 43f};
    public final static float[] EQ_GAIN_2500 = new float[]{18f, 20f, 23f, 26f, 27f, 28f, 30f, 32f, 35f, 39f, 40f, 41f, 43f, 46f, 47f, 50f};
    public final static float[] EQ_GAIN_3000 = new float[]{20.5f, 22.5f, 24.5f, 25.5f, 27.5f, 29f, 30.5f, 31.5f, 33.5f, 34f, 36f, 36f, 36.5f, 40f, 42.5f, 44.5f};
    public final static float[] EQ_GAIN_4000 = new float[]{5f, 6.5f, 8.5f, 10.5f, 12.75f, 15f, 16.25f, 18.75f, 19.25f, 22f, 24f, 25f, 30f, 29.5f, 31.5f, 32.75f};
    public final static float[] EQ_GAIN_6000 = new float[]{5f, 6.5f, 8.5f, 10.5f, 12.75f, 15f, 16.25f, 18.75f, 19.25f, 22f, 24f, 25f, 30f, 29.5f, 31.5f, 32.75f};

    public enum DirectionalMode {
         normal, TV, meeting, face_to_face, unknown;
    }

    // 根据MPO来获取Compression类型
    public enum Compression {
        No, Low, Medium, High, Unknown;
    }

    // 场景降噪
    public enum SceneNR {
        off(0), home(1), restaurant(2), street(3);

        public int rawValue;

        SceneNR(int rawValue) {
            this.rawValue = rawValue;
        }

        public static SceneNR scene(int nr) {
            switch (nr) {
                case 0:
                    return off;
                case 1:
                    return home;
                case 2:
                    return restaurant;
                case 3:
                    return street;
            }
            return off;
        }
    }

    // MARK: 模式文件的内容 参数是指 实际的助听值value 转为需要传递的 整数参数值， BLE传递时还需要对于到协议中的字节位上。
    public static class ModeFileContent {
        public SceneMode mode = SceneMode.UNKNOWN;

        public byte NC1;
        public byte NC2;
        public byte NC3;
        public byte NC4;
        public byte PG1;  // 前置增益MIC1
        public byte PG2;  // 前置增益MIC2
        public byte EQ1;  // 250
        public byte EQ2;  // 500
        public byte EQ3;  // 1000
        public byte EQ4;  // 1500
        public byte EQ5;  // 2000
        public byte EQ6;  // 2500
        public byte EQ7;  // 3000
        public byte EQ8;  // 3500
        public byte EQ9;  // 4000
        public byte EQ10; // 5000
        public byte EQ11; // 6000
        public byte EQ12; // 7000
        public byte CR1;
        public byte CR2;
        public byte CR3;
        public byte CR4;
        public byte Expan; // MIC 扩展
        public byte CT;    // 压缩阈值
        public byte MPO;
        public byte NR;

        // 方向性 转 传递参数 四种方向性 实际值是确定的，所以需要写入的参数值也是确定的，
        //           MIC1    MIC2  对应需要传递的前置增益参数PG1/PG2   EQ250   EQ500
        // 普通模式:    27      12    6/1                           -28(1)      -18(6)
        // TV模式:     27      12    6/1                           -22(4)      -10(10)
        // 会议模式:    27      24    6/5                           -28(1)      -14(8)
        // 一对一模式:  27      27    6/6                           -28(1)      -12(9)
        // BLE发送时按照协议放在对于的位上即可 《增加方向性功能说明》
        // 将方向模式转为需要传递的参数值
        public void directionalMode_to_PG(DirectionalMode dm) {
            switch (dm) {
                case normal:
                    this.PG1 = 6;
                    this.PG2 = 1;
                    this.EQ1 = 1;
                    this.EQ2 = 6;
                    break;
                case TV:
                    this.PG1 = 6;
                    this.PG2 = 1;
                    this.EQ1 = 4;
                    this.EQ2 = 10;
                    break;
                case meeting:
                    this.PG1 = 6;
                    this.PG2 = 5;
                    this.EQ1 = 1;
                    this.EQ2 = 8;
                    break;
                case face_to_face:
                    this.PG1 = 6;
                    this.PG2 = 6;
                    this.EQ1 = 1;
                    this.EQ2 = 9;
                    break;
            }
        }

        // 将PG参数值 转 实际方向模式
        public DirectionalMode PG_to_directionalMode()  {
            if (this.PG1 == 6 && this.PG2 == 1 && this.EQ1 == 1 && this.EQ2 == 6) {
                return DirectionalMode.normal;
            } else if (this.PG1 == 6 && this.PG2 == 1 && this.EQ1 == 4 && this.EQ2 == 10) {
                return DirectionalMode.TV;
            } else if (this.PG1 == 6 && this.PG2 == 5 && this.EQ1 == 1 &&this.EQ2 == 8) {
                return DirectionalMode.meeting;
            } else if (this.PG1 == 6 && this.PG2 == 6 && this.EQ1 == 1 && this.EQ2 == 9) {
                return DirectionalMode.face_to_face;
            } else {
                return DirectionalMode.unknown;
            }
        }

        public void D58B_directionalMode_to_PG(DirectionalMode dm) {
            switch (dm) {
                case normal:
                    this.PG1 = 4; // 21 15 -24 -16
                    this.PG2 = 2;
                    this.EQ1 = 3;
                    this.EQ2 = 7;
                    break;
                case TV:
                    this.PG1 = 5; // 24 12 -22 -10
                    this.PG2 = 1;
                    this.EQ1 = 4;
                    this.EQ2 = 10;
                    break;
                case meeting:     // 24 21 -28 -14
                    this.PG1 = 5;
                    this.PG2 = 4;
                    this.EQ1 = 1;
                    this.EQ2 = 8;
                    break;
                case face_to_face: // 24 24 -28 -12
                    this.PG1 = 5;
                    this.PG2 = 5;
                    this.EQ1 = 1;
                    this.EQ2 = 9;
                    break;
            }
        }


        // 传递参数(0~15) 转 实际EQ值(-30~0)
        public static byte EQ2Value(byte eq) {
            return (byte) (eq * 2 - 30);
        }

        // 实际EQ值 转 传递参数
        public static byte value2eq(byte value) {
            return (byte) ((value + 30)/2);
        }

        // 传递参数 转 压缩比
        public String CR2Value(byte cr) {
            if (cr == 0) {
                return "1 : 1";
            } else if (cr == 1) {
                return "1.14 : 1";
            } else if (cr == 2) {
                return "1.33 : 1";
            } else if (cr == 3) {
                return "1.6 : 1";
            } else if (cr == 4) {
                return "2 : 1";
            } else if (cr == 5) {
                return "2.56 : 1";
            } else if (cr == 6) {
                return "4 : 1";
            }
            return "";
        }

        // 压缩比 转 传递参数
        public byte value2CR(String value) {
            if (value.equals("1 : 1")) {
                return 0;
            } else if (value.equals("1.14 : 1")) {
                return 1;
            } else if (value.equals("1.33 : 1")) {
                return 2;
            } else if (value.equals("1.6 : 1")) {
                return 3;
            } else if (value.equals("2 : 1")) {
                return 4;
            } else if (value.equals("2.56 : 1")) {
                return 5;
            } else if (value.equals("4 : 1")) {
                return 6;
            }
            return 0;
        }

        // 传递参数 转 压缩阈值
        public String CT2Value(byte ct) {
            if (ct == 0) {
                return "40dB";
            } else if (ct == 1) {
                return "45dB";
            } else if (ct == 2) {
                return "50dB";
            } else if (ct == 3) {
                return "55dB";
            } else if (ct == 4) {
                return "60dB";
            } else if (ct == 5) {
                return "65dB";
            } else if (ct == 6) {
                return "70dB";
            }
            return "";
        }

        // 压缩阈值 转 传递参数
        public byte value2CT(String value) {
            if (value.equals("40dB")) {
                return 0;
            } else if (value.equals("45dB")) {
                return 1;
            } else if (value.equals("50dB")) {
                return 2;
            } else if (value.equals("55dB")) {
                return 3;
            } else if (value.equals("60dB")) {
                return 4;
            } else if (value.equals("65dB")) {
                return 5;
            } else if (value.equals("70dB")) {
                return 6;
            }
            return 0;
        }


        public Compression MPO2Compression() {
            if (MPO == 0) {
                return Compression.No;
            } else if (MPO == 1) {
                return Compression.Low;
            } else if (MPO == 2) {
                return Compression.Medium;
            } else if (MPO == 3) {
                return Compression.High;
            } else if (MPO == 4) {
                return Compression.High;
            } else if (MPO == 5) {
                return Compression.High;
            } else if (MPO == 6) {
                return Compression.High;
            }
            return Compression.Unknown;
        }

        // 通讯参数 转为 实际值
        public static int MPO2Value(byte mpo) {
            if (mpo == 0) {
                return 0;
            } else if (mpo == 1) {
                return -4;
            } else if (mpo == 2) {
                return -8;
            } else if (mpo == 3) {
                return -12;
            } else if (mpo == 4) {
                return -16;
            } else if (mpo == 5) {
                return -20;
            } else if (mpo == 6) {
                return -24;
            }
            return 0;
        }

        // MPO 转 传递参数
//    static func value2MPO(value: String) -> UInt8 {
//        if value == "MUO" {
//            return 0
//        } else if value == "-4" {
//            return 1
//        } else if value == "-8" {
//            return 2
//        } else if value == "-12" {
//            return 3
//        } else if value == "-16" {
//            return 4
//        } else if value == "-20" {
//            return 5
//        } else if value == "-24" {
//            return 6
//        }
//        return 0
//    }
        // MPO 转 传递参数
        public static byte value2MPO(int value) {
            if (value == 0) {
                return 0;
            } else if (value == -4) {
                return 1;
            } else if (value == -8) {
                return 2;
            } else if (value == -12) {
                return 3;
            } else if (value == -16) {
                return 4;
            } else if (value == -20) {
                return 5;
            } else if (value == -24) {
                return 6;
            }
            return 0;
        }

        // 传递参数 转 MIC
        public String Mic2Value(byte mic) {
            if (mic == 0) {
                return "OFF";
            } else if (mic == 1) {
                return "ON";
            }
            return "";
        }

        // MIC 转 传递参数
        public byte value2Mic(String value) {
            if (value.equals("OFF")) {
                return 0;
            } else if (value.equals("ON")) {
                return 1;
            }
            return 0;
        }

        // 传递参数 转 NR
        public String NR2Value(byte nr) {
            if (nr == 0) {
                return "OFF";
            } else if (nr == 1) {
                return "LOW";
            } else if (nr == 2) {
                return "Mid";
            } else if (nr == 3) {
                return "High";
            }
            return "";
        }

        public static void copyEQ(ModeFileContent out, ModeFileContent in, String version) {
            if (version.equals("V1")) {
                out.EQ1 = in.EQ1;
                out.EQ2 = in.EQ2;
                out.EQ3 = in.EQ3;
                out.EQ4 = in.EQ4;
                out.EQ5 = in.EQ5;
                out.EQ6 = in.EQ6;
                out.EQ7 = in.EQ7;
                out.EQ9 = in.EQ9;
                out.EQ8 = (byte) ((in.EQ7 + in.EQ9) / 2);
            } else if (version.equals("V2")) {
                out.EQ1 = in.EQ1;
                out.EQ2 = in.EQ2;
                out.EQ3 = in.EQ3;
                out.EQ4 = in.EQ4;
                out.EQ5 = in.EQ5;   // 2000
                out.EQ7 = in.EQ7;   // 3000
                out.EQ9 = in.EQ9;   // 4000
                out.EQ11 = in.EQ11; // 6000
                out.EQ6 = (byte) ((in.EQ5 + in.EQ7) / 2);   // 2500
                out.EQ8 = (byte) ((in.EQ7 + in.EQ9) / 2);   // 3500
            }
        }

        // NR 转 传递参数
        public byte value2NR(String value) {
            if (value.equals("OFF")) {
                return 0;
            } else if (value.equals("LOW")) {
                return 1;
            } else if (value.equals("Mid")) {
                return 2;
            } else if (value.equals("High")) {
                return 3;
            }
            return 0;
        }

        private static int findElementInArray(float element, float[] array) {
            int minIdx = 0;
            float minDiff = 100;
            if (element > array[array.length - 1]) {
                return array.length - 1;
            } else if (element < array[0]) {
                return 0;
            } else {
                for (int i=0; i<array.length-1; i++) {
                    float diff = Math.abs(array[i] - element);
                    if (diff == 0) {
                        return i;
                    } else if (diff < minDiff) {
                        minDiff = diff;
                        minIdx = i;
                    }
                }
                return minIdx;
            }
        }

        // 将听力测试结果转化DSP文件中的EQ值 这是 二分之一补偿公式 已经弃用了
//        static func heardTestToEQ(_ testResults:[Int: Int], _ dspModefile: inout ModeFileContent) {
//            var gain = Float(testResults[250]! - 20) / 2
//            dspModefile.EQ1 = UInt8(findElementInArray(element: gain, array: EQ_GAIN_250))
//
//            gain = Float(testResults[500]! - 20) / 2
//            dspModefile.EQ2 = UInt8(findElementInArray(element: gain, array: EQ_GAIN_500))
//
//            gain = Float(testResults[1000]! - 20) / 2
//            dspModefile.EQ3 = UInt8(findElementInArray(element: gain, array: EQ_GAIN_1000))
//
//            gain = Float(testResults[1500]! - 20) / 2
//            dspModefile.EQ4 = UInt8(findElementInArray(element: gain, array: EQ_GAIN_1500))
//
//            gain = Float(testResults[2000]! - 20) / 2
//            dspModefile.EQ5 = UInt8(findElementInArray(element: gain, array: EQ_GAIN_2000))
//
//            gain = Float(testResults[2500]! - 20) / 2
//            dspModefile.EQ6 = UInt8(findElementInArray(element: gain, array: EQ_GAIN_2500))
//
//            gain = Float(testResults[3000]! - 20) / 2
//            dspModefile.EQ7 = UInt8(findElementInArray(element: gain, array: EQ_GAIN_3000))
//
//            gain = Float(testResults[4000]! - 20) / 2
//            dspModefile.EQ9 = UInt8(findElementInArray(element: gain, array: EQ_GAIN_4000))
//
//            dspModefile.EQ8 = (dspModefile.EQ7 + dspModefile.EQ9)/2
//        }

        // 参考《JH-W3自动验配补偿说明-20201116-V1.00》 计算介入增益值
        // x = 0.05 × (HTL500 + HTL1k + HTL2k)
        // G250 = x + 0.31HTL250 + ITE
        private final static HashMap<Integer, Integer> ITE = new HashMap<Integer, Integer>() {{
            //[250:-1, 500:9, 750:13, 1000:16, 1500:14, 2000:14, 3000:15, 4000:13, 6000:5]
            put(250, -1); put(500, 9); put(750, 13); put(1000, 16);
            put(1500,14); put(2000,14); put(3000,15); put(4000,13); put(6000,5);
        }};

        // 参考《JH-W3自动验配补偿说明-20211223-V1.1-2》 计算介入增益值
        private final static HashMap<Integer, Integer> ITE_V11 = new HashMap<Integer, Integer>() {{
            //[250:-1, 500:9, 750:13, 1000:16, 1500:14, 2000:14, 3000:15, 4000:13, 6000:5]
            put(250, -1); put(500, 9); put(750, 10); put(1000, 12);
            put(1500, 6); put(2000,5); put(3000, 5); put(4000,  3); put(6000,4);
        }};

        // 深聋校正因子
        private final static HashMap<Integer, Integer> correction_factor_95 = new HashMap<Integer, Integer>() {{
            //[250:4,   500:3, 750:1, 1000:0, 1500:-1, 2000:-2, 3000:-2, 4000:-2, 6000:-2]
            put(250,4); put(500,3); put(750,1); put(1000,0);
            put(1500,-1); put(2000,-2); put(3000,-2); put(4000,-2); put(6000,-2);
        }};
        private final static HashMap<Integer, Integer> correction_factor_100 = new HashMap<Integer, Integer>() {{
            //250:6,  500:4, 750:2, 1000:0, 1500:-2, 2000:-3, 3000:-3, 4000:-3, 6000:-3]
            put(250,6);  put(500,4); put(750,2); put(1000,0);
            put(1500,-2); put(2000,-3); put(3000,-3); put(4000,-3); put(6000,-3);
        }};
        private final static HashMap<Integer, Integer> correction_factor_105 = new HashMap<Integer, Integer>() {{
            // 250:8,  500:5, 750:2, 1000:0, 1500:-3, 2000:-5, 3000:-5, 4000:-5, 6000:-5]
            put(250,8);  put(500,5); put(750,2); put(1000,0);
            put(1500,-3); put(2000,-5); put(3000,-5); put(4000,-5); put(6000,-5);
        }};
        private final static HashMap<Integer, Integer> correction_factor_110 = new HashMap<Integer, Integer>() {{
            //250:11, 500:7, 750:3, 1000:0, 1500:-3, 2000:-6, 3000:-6, 4000:-6, 6000:-6]
            put(250,11); put(500,7); put(750,3); put(1000,0);
            put(1500,-3); put(2000,-6); put(3000,-6); put(4000,-6); put(6000,-6);
        }};
        private final static HashMap<Integer, Integer> correction_factor_115 = new HashMap<Integer, Integer>() {{
            //[250:13, 500:8, 750:4, 1000:0, 1500:-4, 2000:-8, 3000:-8, 4000:-8, 6000:-8]
            put(250,13); put(500,8); put(750,4); put(1000,0);
            put(1500,-4); put(2000,-8); put(3000,-8); put(4000,-8); put(6000,-8);
        }};
        private final static HashMap<Integer, Integer> correction_factor_120 = new HashMap<Integer, Integer>() {{
            //[250:13, 500:9, 750:4, 1000:0, 1500:-5, 2000:-9, 3000:-9, 4000:-9, 6000:-9]
            put(250,13); put(500,9); put(750,4); put(1000,0);
            put(1500,-5); put(2000,-9); put(3000,-9); put(4000,-9); put(6000,-9);
        }};

        // HTL500 HTL1000 HTL2000: 测试到的500Hz、1000Hz、2000Hz的听阈值
        // HTL_X : 需要计算的听阈值
        // freq  : 需要计算的频率
        public static float insertionGain(int HTL500, int HTL1000, int HTL2000, int HTL_X, int freq) {
            float gain = 0.05f * (float)(HTL500 + HTL1000 + HTL2000) + 0.31f * (float)(HTL_X) + (float)(ITE_V11.get(freq)) - 6;
            if (HTL2000 >= 120) {
                gain = gain + (float)(correction_factor_120.get(freq));
            } else if (HTL2000 >= 115) {
                gain = gain + (float)(correction_factor_115.get(freq));
            } else if (HTL2000 >= 110) {
                gain = gain + (float)(correction_factor_110.get(freq));
            } else if (HTL2000 >= 105) {
                gain = gain + (float)(correction_factor_105.get(freq));
            } else if (HTL2000 >= 100) {
                gain = gain + (float)(correction_factor_100.get(freq));
            } else if (HTL2000 >= 95) {
                gain = gain + (float)(correction_factor_95.get(freq));
            }
            return gain;
        }

        // 250 500 1000 1500 2000 2500 3000 4000
        // 0    1    2    3   4    5    6    7
        public static void testResultToEQ(int[] testResults, ModeFileContent dspModeFile, String resultVersion) {
            float gain = insertionGain(testResults[1], testResults[2], testResults[4], testResults[0], 250);
            dspModeFile.EQ1 = (byte)findElementInArray(gain, EQ_GAIN_250);

            gain = insertionGain(testResults[1], testResults[2], testResults[4], testResults[1], 500);
            dspModeFile.EQ2 = (byte)findElementInArray(gain, EQ_GAIN_500);

            gain = insertionGain(testResults[1], testResults[2], testResults[4], testResults[2], 1000);
            dspModeFile.EQ3 = (byte)findElementInArray(gain, EQ_GAIN_1000);

            gain = insertionGain(testResults[1], testResults[2], testResults[4], testResults[3], 1500);
            dspModeFile.EQ4 = (byte)findElementInArray(gain, EQ_GAIN_1500);

            gain = insertionGain(testResults[1], testResults[2], testResults[4], testResults[4], 2000);
            dspModeFile.EQ5 = (byte)findElementInArray(gain, EQ_GAIN_2000);   // 2000

            gain = insertionGain(testResults[1], testResults[2], testResults[4], testResults[6], 3000);
            dspModeFile.EQ7 = (byte)findElementInArray(gain, EQ_GAIN_3000);   // 3000
            dspModeFile.EQ7 = dspModeFile.EQ7 > 12 ? 12 : dspModeFile.EQ7;    // 控制最大为12

            gain = insertionGain(testResults[1], testResults[2], testResults[4], testResults[7], 4000);
            dspModeFile.EQ9 = (byte)findElementInArray(gain, EQ_GAIN_4000);  // 4000
            dspModeFile.EQ9 = dspModeFile.EQ9 > 12 ? 12 : dspModeFile.EQ9;

            dspModeFile.EQ6 = (byte) ((dspModeFile.EQ5 + dspModeFile.EQ7)/2); // 2500
            dspModeFile.EQ6 = dspModeFile.EQ6 > 12 ? 12 : dspModeFile.EQ6;

            dspModeFile.EQ8 = (byte) ((dspModeFile.EQ7 + dspModeFile.EQ9)/2); // 3500
            dspModeFile.EQ8 = dspModeFile.EQ8 > 12 ? 12 : dspModeFile.EQ8;

            if (resultVersion.equals("V2")) {
                gain = insertionGain(testResults[1], testResults[2], testResults[4], testResults[7], 6000);
                dspModeFile.EQ11 = (byte)findElementInArray(gain, EQ_GAIN_6000);  // 6000
                dspModeFile.EQ11 = dspModeFile.EQ11 > 12 ? 12 : dspModeFile.EQ11;
            }
        }

        @Override
        public String toString() {
            return "ModeFileContent{" +
                    "mode=" + mode +
                    ", NC1=" + NC1 +
                    ", NC2=" + NC2 +
                    ", NC3=" + NC3 +
                    ", NC4=" + NC4 +
                    ", PG1=" + PG1 +
                    ", PG2=" + PG2 +
                    ", EQ1=" + EQ1 +
                    ", EQ2=" + EQ2 +
                    ", EQ3=" + EQ3 +
                    ", EQ4=" + EQ4 +
                    ", EQ5=" + EQ5 +
                    ", EQ6=" + EQ6 +
                    ", EQ7=" + EQ7 +
                    ", EQ8=" + EQ8 +
                    ", EQ9=" + EQ9 +
                    ", EQ10=" + EQ10 +
                    ", EQ11=" + EQ11 +
                    ", EQ12=" + EQ12 +
                    ", CR1=" + CR1 +
                    ", CR2=" + CR2 +
                    ", CR3=" + CR3 +
                    ", CR4=" + CR4 +
                    ", Expan=" + Expan +
                    ", CT=" + CT +
                    ", MPO=" + MPO +
                    ", NR=" + NR +
                    '}';
        }
    } //

    public final static BTProtocol share = new BTProtocol();

    private BTProtocol() {
        initObservable();
    }

    byte Head  = (byte)0xFE;
    byte Start = (byte)0x68;
    byte Rear  = (byte)0x16;

    // 获取DSP命令字 返回正确命令字 返回错误命令字
    public final static byte Read_CMD      =   (byte)0x11;
    public final static byte Read_Success  =   (byte)0x91;
    public final static byte Read_Error    =   (byte)0xD1;

    // 写DSP命令字 返回正确命令字 返回错误命名字
    public final static byte Write_CMD     =   (byte)0x14;
    public final static byte Write_Success =   (byte)0x94;
    public final static byte Write_Error   =   (byte)0xD4;

    // 控制DSP命令字
    byte Ctl_CMD =        (byte)0x1C;
    byte Ctl_Success =    (byte)0x9C;
    byte Ctl_Error =      (byte)0xDC;

    // DSP 主动上报命令字
    public final static byte Report_Success = (byte)0x9F;

    public byte mode2Volume; // 模式2的音量值

    // 用于监听助听器模式的变化 name:BLE设备名 mode:设备模式 type:返回值的类型，用于区别是 APP读取模式 和 DSP主动上报(Read_Success|Report_Success)
//    let rxSceneMode = PublishRelay<(name:String, mode:SceneMode, type: UInt8)>()
//    let rxDSPModeFile = PublishRelay<(name: String, modeFile: ModeFileContent)>()
//    let rxWriteFeedback = PublishRelay<(name:String, isSuccess:Bool)>()
//    let rxCrlFeedback = PublishRelay<(name:String, isSuccess:Bool)>() // 从
//    let rxVersionFeedback = PublishRelay<(name:String, version:String)>()

    // MARK: 基础读取命令 CMD = 0x11
    private byte[] baseReadCmd(byte DI0, byte DI1) {
        byte[] cmd = new byte[] {0,0,0,0,0,0,0,0};
        cmd[0] = (byte) 0xFE;
        cmd[1] = (byte) 0x68;
        cmd[2] = (byte) 0x11;
        cmd[3] = 2;
        cmd[4] = DI0;
        cmd[5] = DI1;
        cmd[6] = (byte)(cmd[1] + cmd[2] + cmd[3] + cmd[4] + cmd[5]);
        cmd[7] = 0x16;
        return cmd;
    }

    // 读取模式文件命令 modeFile: 要读取的模式文件 1 - 5 和 全局参数文件
    public byte[] buildCMD_ReadModeFile(byte mode) {
        byte DI1 = 0x03;
        byte DI0 = (byte)(mode - 1);
        return baseReadCmd(DI0, DI1);
    }

    // MARK: 读取 模式文件 命令
    public byte[] buildCMD_ReadModeFile(SceneMode mode) {
        byte md = 0;
        switch (mode) {
            case CONVERSATION:
                md = 0;
                break;
            case RESTAURANT:
                md = 1;
                break;
            case OUTDOOR:
                md = 2;
                break;
            case MUSIC:
                md = 3;
                break;
        }
        byte DI1 = 0x03;
        byte DI0 = md;
        return baseReadCmd(DI0, DI1);
    }

    // MARK: 读取模式音量档位命令 (00:当前模式 01:模式1 ... FF:当前运行状态)
    public byte[] buildCMD_ReadModeVolume(byte mode) {
        byte DI1 = 0x04;
        byte DI0 = mode;
        return baseReadCmd(DI0, DI1);
    }

    // MARK: 读取蓝牙软件版本号
    public byte[] buildCMD_ReadVersion() {
        byte DI1 = 0x08;
        byte DI0 = 0x12;
        return baseReadCmd(DI0, DI1);
    }

    // MARK: 读取LED设置状态
    public byte[] buildCMD_ReadLEDType() {
        byte DI1 = 0x08;
        byte DI0 = (byte) 0xA0;
        return baseReadCmd(DI0, DI1);
    }

    // MARK: 基础写命令 CMD = 0x14
    public byte[] baseWriteCmd(byte DI0, byte DI1, byte[] data, int dataLen) {
        byte[] cmd = new byte[8 + dataLen];
        cmd[0] = (byte) 0xFE;
        cmd[1] = (byte) 0x68;
        cmd[2] = (byte) 0x14;
        cmd[3] = (byte) (2 + dataLen);
        cmd[4] = DI0;
        cmd[5] = DI1;
        byte CS = (byte) (cmd[1] + cmd[2] + cmd[3] + cmd[4] + cmd[5]);
        int i = 6;
        for (byte d : data) {
            cmd[i] = d;
            i = i + 1;
            CS = (byte) (CS + d);
        }
        cmd[cmd.length-2] = CS;
        cmd[cmd.length-1] = 0x16;
        //let cmdstr = cmd.map { String($0, radix: 16) }
        //print("------------------------------------ 写命令: ",cmdstr)
        return cmd;
    }

    // MARK: 写模式文件命令 将模式文件中参数转化为协议字节序
    public byte[] buildCMD_WriteModeFile(ModeFileContent modeFile, SceneMode mode) {
        byte DI1 = 0x03;
        byte DI0 = mode.getMdToDI0();
        byte[] data = new byte[] {
                (byte)((byte)(modeFile.NC1 & (byte)0x07) | (modeFile.PG1 << 3) | (modeFile.PG2 << 6)),
                (byte)((byte)(modeFile.NC2 << 1) | (modeFile.PG2 >> 2)),
                (byte)(modeFile.EQ1 | (modeFile.EQ2 << 4)),
                (byte)(modeFile.EQ3 | (modeFile.EQ4 << 4)),   // d3
                (byte)(modeFile.EQ5 | (modeFile.EQ6 << 4)),   // d4
                (byte)(modeFile.EQ7 | (modeFile.EQ8 << 4)),   // d5
                (byte)(modeFile.EQ9 | (modeFile.EQ10 << 4)),  // d6
                (byte)(modeFile.EQ11 | (modeFile.EQ12 << 4)), // d7
                (byte)(modeFile.CR1 | (modeFile.CR2 << 3) | ((modeFile.CR3 & 0x03) << 6)), // d8
                (byte) ((byte)(modeFile.CR3 >> 2) | (modeFile.CR4 << 1) | (modeFile.CT << 4) | (modeFile.Expan << 7)), //d9
                (byte)((modeFile.MPO << 6) | modeFile.NC3),                   // d10
                (byte)(modeFile.NC4 | (modeFile.MPO>>2) | modeFile.NR << 1)};  // d11
        return baseWriteCmd(DI0, DI1, data, data.length);
    }

    // MARK: type: 0关闭LED显示 1开启LED显示
    public byte[] buildCMD_WriteLEDType(byte type) {
        byte DI1 = 0x08;
        byte DI0 = (byte) 0xA0;
        byte[] data = new byte[] { type };
        return baseWriteCmd(DI0, DI1, data, 1);
    }

    // MARK: 基础控制命令 CMD = 0x1C
    public byte[] baseCtlCmd(byte cmmd, byte value) {
        byte[] cmd = new byte[] {0,0,0,0,0,0,0,0};
        cmd[0] = (byte)0xFE;
        cmd[1] = (byte)0x68;
        cmd[2] = (byte)0x1C;
        cmd[3] = (byte)0x02;
        cmd[4] = cmmd;
        cmd[5] = value;
        cmd[6] = (byte)(cmd[1] + cmd[2] + cmd[3] + cmd[4] + cmd[5]);
        cmd[7] = (byte)0x16;
        //let cmdstr = cmd.map { String($0, radix: 16) }
        // print("------------------------------------ 控制命令: ",cmdstr)
        return cmd;
    }

    // MARK: 控制命令 设置音量 cmmd = 0x01 value = 模式(高4位,取值范围 1~4)| 档位（低4位，取值范围 0~10）
    public byte[] buildCMD_CtlVC(SceneMode mode) {
        byte cmmd = 0x01; // 设置音量命令
        byte md = 0;
        byte volume = 0;
        switch (mode) {
            case CONVERSATION:
                md = 1;
                volume = mode.getVolume();
                break;
            case RESTAURANT:
                md = 2;
                volume = mode.getVolume();
                break;
            case OUTDOOR:
                md = 3;
                volume = mode.getVolume();
                break;
            case MUSIC:
                md = 4;
                volume = mode.getVolume();
                break;
            default:
                break;
        }
        byte value = (byte) (md << 4 | volume);
        return baseCtlCmd(cmmd, value);
    }

    // MARK: 控制命令 设置模式 cmmd = 0x02 value = 模式(1~4)
    public byte[] buildCMD_CtlMode(SceneMode mode) {
        byte cmmd = 0x02;
        byte md  = 0;
        switch (mode) {
            case CONVERSATION:
                md = 1;
                break;
            case RESTAURANT:
                md = 2;
                break;
            case OUTDOOR:
                md = 3;
                break;
            case MUSIC:
                md = 4;
                break;
            default:
                break;
        }
        return baseCtlCmd(cmmd, md);
    }

    // MARK: 控制命令 锁定芯片
    public byte[] buildCMD_CtlLockChip() {
        byte cmmd = 0x03;
        return baseCtlCmd(cmmd, (byte) 0);
    }

    // MARK: 控制命令 开启声音
    public byte[] buildCMD_CtlUnmute() {
        byte cmmd = 0x04;
        return baseCtlCmd(cmmd, (byte) 0);
    }

    // MARK: 控制命令 关闭声音
    public byte[] buildCMD_CtlMute() {
        byte cmmd = 0x05;
        return baseCtlCmd(cmmd, (byte) 0);
    }

    // 检查返回数据是否合法
    /*
    private byte[] frame = new byte[32];
    private int desPos = 0;
    private boolean checkFeedback(String name,  byte[] data) {
        byte cs = 0;
        byte rear = 0;
        int frameLen = 0; // 数据帧的长度 不是frame数组的长度
        if (desPos == 0) { Arrays.fill(frame, (byte) 0); }
        if (desPos + data.length > 32) { desPos = 0; }
        System.arraycopy(data, 0, frame, desPos, data.length);
        desPos = desPos + data.length;
        if (desPos < 6) { return false; }
        if (frame[1] == (byte)0xFE && frame[2] == (byte)0x68) {
            frameLen = (5 + frame[4] + 2);
            rear = frame[frameLen - 1];
            if (rear == (byte) 0x16) {
                for (int index=2; index <= frameLen - 3; index++) {
                    cs = (byte) (cs + frame[index]);
                }
                if (cs == frame[frameLen - 2]) {  // 校验位
                    desPos = 0;
                    return true;
                }
            }
        }
        return false;
    } */

    private boolean checkFeedback(String name,  byte[] frame) {
        byte cs = 0;
        byte rear = 0;
        if (frame.length < 6) { return false; }
        if (frame[1] == (byte)0xFE && frame[2] == (byte)0x68) {
            int cmdLen = (5 + frame[4] + 2);
            if (cmdLen > frame.length ) { return false; }
            rear = frame[cmdLen - 1];
            if (rear == (byte) 0x16) {
                for (int index=2; index <= cmdLen - 3; index++) {
                    cs = (byte) (cs + frame[index]);
                }
                // 校验位
                return cs == frame[cmdLen - 2];
            }
        }
        return false;
    }

    private void parseFeedback(String name, byte[] frame) {
        byte cmmd = frame[3];
//        var dataLen: UInt8 = 0
        byte DI0 = 0;
        byte DI1 = 0;
        byte md = 0;
        ModeFileContent fileContent = new ModeFileContent();
        if (cmmd == 0x11 || cmmd == 0x14 || cmmd == 0x1C) { // 测试过程中发现会将发送的数据读回来，排除发送的数据
            return;
        }
        if (cmmd == Read_Success) {    // 读取蓝牙模块正常
            DI0 = frame[5];
            DI1 = frame[6];
            // DI1 ==04 : 获取 模式 和 档位
            if (DI1 == 0x03) {  // 模式文件
//                byte d0 = frame[7];
//                byte d1 = frame[8];
//                byte d2 = frame[9];
//                byte d3 = frame[10];
//                byte d4 = frame[11];
//                byte d5 = frame[12];
//                byte d6 = frame[13];
//                byte d7 = frame[14];
//                byte d8 = frame[15];
//                byte d9 = frame[16];
//                byte d10 = frame[17];
//                byte d11 = frame[18];

                int d0 = frame[7] & 0xff;  // 用于转为无符号数处理
                int d1 = frame[8] & 0xff;
                int d2 = frame[9] & 0xff;
                int d3 = frame[10] & 0xff;
                int d4 = frame[11] & 0xff;
                int d5 = frame[12] & 0xff;
                int d6 = frame[13] & 0xff;
                int d7 = frame[14] & 0xff;
                int d8 = frame[15] & 0xff;
                int d9 = frame[16] & 0xff;
                int d10 = frame[17] & 0xff;
                int d11 = frame[18] & 0xff;

                if (DI0 == 0x00) {
                    fileContent.mode = SceneMode.CONVERSATION;
                } else if (DI0 == 0x01) {
                    fileContent.mode = SceneMode.RESTAURANT;
                } else if (DI0 == 0x02) {
                    fileContent.mode = SceneMode.OUTDOOR;
                } else if (DI0 == 0x03) {
                    fileContent.mode = SceneMode.MUSIC;
                }
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
                Log.d(TAG, "[BTProtocol] 读 蓝牙模块 成功 DSP模式文件: " + fileContent);
                fileContent.mode.setDeviceName(name);
                modeFileContentObservableEmitter.onNext(fileContent);
            }
            else if (DI1 == 0x04) {
                if (DI0 == 0x00) {
                    Log.d(TAG, "[BTProtocol] 读 蓝牙模块 成功 当前模式:" + name + " md="+frame[6]);
//                    self.rxSceneMode.accept( (name, SceneMode.mode(frame[7], 0), Read_Success) )

                } else if (DI0 == 0x01) {
                    Log.d(TAG,"[BTProtocol] 读 蓝牙模块 成功 自定义模式: " + name + SceneMode.CONVERSATION);
//                    self.rxSceneMode.accept( (name, .custom(frame[7]), Read_Success) )

                } else if (DI0 == 0x02) {
                    Log.d(TAG,"[BTProtocol] 读 蓝牙模块 成功 标准模式:" + name +  SceneMode.RESTAURANT);
//                    self.rxSceneMode.accept( (name, .standard(frame[7]), Read_Success) )

                } else if (DI0 == 0x03) {
                    Log.d(TAG,"[BTProtocol] 读 蓝牙模块 成功 降噪模式:" + name + SceneMode.OUTDOOR);
//                    self.rxSceneMode.accept( (name, .denoise(frame[7]), Read_Success) )

                } else if (DI0 == 0x04) {
                    Log.d(TAG,"[BTProtocol] 读 蓝牙模块 成功 户外模式:" + name +  SceneMode.MUSIC);
//                    self.rxSceneMode.accept( (name, .outdoor(frame[7]), Read_Success) )

                } else if (DI0 == (byte)0xFF) {
                    md = frame[7];
                    byte volume = frame[7+md]; // 7+1 == 模式1音量， 7+2 = 模式2音量
                    mode2Volume = frame[7+2];
                    Log.d(TAG,"[BTProtocol] 读 蓝牙模块 成功 当前运行状态: " + name + " md="+md + " volume="+volume);
                    modeObservableEmitter.onNext(SceneMode.mode(name, md, volume, Read_Success));
//                    self.rxSceneMode.accept((name, SceneMode.mode(md, volume), Read_Success))
                }

            }
            else if (DI1 == 0x08) {
                if (DI0 == 0x10 || DI0 == 0x12) { // 读取版本号
                    byte[] v = new byte[frame.length - 2 - 7];
                    int index = 0;
                    for (int i=7; i < frame.length-2; i++) {
                        v[index++] = frame[i];
                    }
                    String version = new String(v);
                    Log.d(TAG,"[BTProtocol] 读 蓝牙模块 成功 版本号：" + version);
                    deviceVersionObservableEmitter.onNext(name + ":" + version);
                } else if (DI0 == (byte) 0xA0) {
                    byte type = frame[7];
                    Log.d(TAG,"[BTProtocol] 读 蓝牙模块 成功 LED：" + type);
                    ledTypeObservableEmitter.onNext(name + ":" + type);
                }
            }

        } else if (cmmd == Read_Error) {
            Log.d(TAG,name + " 读蓝牙模块错误");

        } else if (cmmd == Ctl_Success) {
            Log.d(TAG,name + " 控制 蓝牙模块 反馈成功");
            if (ctlFeedbackObservableEmitter != null) ctlFeedbackObservableEmitter.onNext(name + "," + "true");   //"G.SoundBuds-R,true"

        } else if (cmmd == Ctl_Error) {
            Log.d(TAG,name + " 控制 蓝牙模块 反馈错误");
            if (ctlFeedbackObservableEmitter != null) ctlFeedbackObservableEmitter.onNext(name + "," + "false");  //"G.SoundBuds-R,false"

        } else if (cmmd == Write_Success) {
            Log.d(TAG,name + " 写 蓝牙模块 反馈成功 ");
            if (writeFeedbackObservableEmitter != null) writeFeedbackObservableEmitter.onNext(name + "," + "true"); //"G.SoundBuds-R,true"

        } else if (cmmd == Write_Error) {
            Log.d(TAG,name + " 写 蓝牙模块 反馈错误");
            if (writeFeedbackObservableEmitter != null) writeFeedbackObservableEmitter.onNext(name + "," + "false"); //"G.SoundBuds-R,false"

        } else if (cmmd == Report_Success) {
            md = frame[5];
            byte volume = frame[5+md];
            Log.d(TAG,"读 蓝牙模块 成功 获取上报运行状态:" + name + "md="+md + " volume="+volume);
            if (modeObservableEmitter != null) modeObservableEmitter.onNext(SceneMode.mode(name, md, volume, Report_Success));
        }
    }

    public void checkAndParseFeedback(String name, byte[] data) {
        if (checkFeedback(name, data)) {
//            parseFeedback(name, this.frame);
            parseFeedback(name, data);
        }
    }

    private boolean initObservableEnable;
    public Observable<SceneMode> sceneModeObservable;                // 被观察者 模式
    private ObservableEmitter<SceneMode> modeObservableEmitter;
    public Observable<String> ctlFeedbackObservable;                 // 被观察者 控制命令反馈
    private ObservableEmitter<String> ctlFeedbackObservableEmitter;
    public Observable<String> writeFeedbackObservable;               // 被观察者 写命令反馈
    private ObservableEmitter<String> writeFeedbackObservableEmitter;
    public Observable<ModeFileContent> modeFileContentObservable;    // 被观察者 模式文件
    private ObservableEmitter<ModeFileContent> modeFileContentObservableEmitter;
    public Observable<String> deviceVersionObservable;               // 被观察者 版本号
    private ObservableEmitter<String> deviceVersionObservableEmitter;
    public Observable<String> ledTypeObservable;                     // 被观察者 LED设置类型
    private ObservableEmitter<String> ledTypeObservableEmitter;

    // 初始化被观察者
    private void initObservable() {
        if (initObservableEnable) return;
        initObservableEnable = true;
        sceneModeObservable = Observable.create(emitter -> modeObservableEmitter = emitter);
        ctlFeedbackObservable = Observable.create(emitter -> ctlFeedbackObservableEmitter = emitter);
        writeFeedbackObservable = Observable.create(emitter -> writeFeedbackObservableEmitter = emitter);
        modeFileContentObservable = Observable.create(emitter -> modeFileContentObservableEmitter = emitter);
        deviceVersionObservable = Observable.create(emitter -> deviceVersionObservableEmitter = emitter);
        ledTypeObservable = Observable.create(emitter -> ledTypeObservableEmitter = emitter);
    }
}
