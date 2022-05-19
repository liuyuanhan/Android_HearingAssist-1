package com.upixels.jh.hearingassist.util;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import io.reactivex.rxjava3.disposables.Disposable;
import me.forrest.commonlib.jh.AIDMode;
import me.forrest.commonlib.jh.BTProtocol;
import me.forrest.commonlib.util.BLEUtil;
import me.forrest.commonlib.util.FileUtil;

public class DeviceManager {
    private static final String TAG = DeviceManager.class.getSimpleName();

    private static final DeviceManager ourInstance = new DeviceManager();

    public static DeviceManager getInstance() {
        return ourInstance;
    }

    public static final String EAR_TYPE_LEFT  = "left";
    public static final String EAR_TYPE_RIGHT = "right";
    public static final String EAR_TYPE_BOTH  = "both";

    private Context                     context;
    private boolean                     leftConnected;
    private boolean                     rightConnected;
    private boolean                     readLeftBat;  //是否读了一次电量
    private boolean                     readRightBat; //是否读了一次电量
    private int                         leftBat;
    private int                         rightBat;
    private AIDMode                     leftMode;
    private AIDMode                     rightMode;
    private String                      leftMac;
    private String                      rightMac;
    private String                      leftResult;
    private String                      rightResult;
    private BTProtocol.ModeFileContent  leftModeFileContent;
    private BTProtocol.ModeFileContent  rightModeFileContent;
    private BTProtocol.ModeFileContent  mutableLeftModeFileContent;
    private BTProtocol.ModeFileContent  mutableRightModeFileContent;
    private int                         connectedCnt;       // 用两个bit表示 0000 0011
    private int                         modeCnt;            //获取到的模式数量 用两个bit表示 0000 0011
    private int                         modeFileCnt;
    private int                         writeFeedbackCnt;
    private int                         ctlFeedbackCnt;

    private BLEUtil.BLEDevice           leftPairedDevice;
    private BLEUtil.BLEDevice           rightPairedDevice;
    private List<BLEUtil.BLEDevice>     mBleDevices;
    private HashSet<String>             connectedDeviceMacSet = new HashSet<>(2);   // 记录连接成功的设备的MAC地址 用于在有设备断开时，更新状态

    private Disposable  disposable0;
    private Disposable  disposable1;
    private Disposable  disposable2;
    private Disposable  disposable3;

    private final HandlerThread workThread = new HandlerThread("WorkThread");
    private Handler             workHandler;
    private long                delay;

    private final List<DeviceChangeListener> listeners = new LinkedList<>();

    public interface DeviceChangeListener {
        // 连接状态回调
        void onConnectStatus(boolean leftConnected, boolean rightConnected);

        // 是否正在读数过程中，用于UI显示圆圈
        void onReadingStatus(boolean isReading);
        // 电量变化回调
        void onChangeBat(int leftBat, int rightBat);
        // 模式变化回调
        void onChangeSceneMode(AIDMode leftMode, AIDMode rightMode);

        // 模式文件变化回调
        void onChangeModeFile(BTProtocol.ModeFileContent leftContent, BTProtocol.ModeFileContent rightContent);

        // 控制命令回调
        void onCtlFeedback(String leftResult, String rightResult);

        // 写命令Feedback回调
        void onWriteFeedback(String leftResult, String rightResult);
    }

    public void addListener(DeviceChangeListener listener) {
        workHandler.post(() -> {
            listeners.add(listener);
        });
    }

    public void removeListener(DeviceChangeListener listener) {
        workHandler.post(() -> {
            Iterator<DeviceChangeListener> it = listeners.iterator();
            while (it.hasNext()) {
                if (it.next() == listener ) {
                    it.remove();
                    break;
                }
            }
        });
    }

    private void updateListenerForOnConnectStatus(boolean leftConnected, boolean rightConnected) {
        workHandler.post(() -> {
            for (DeviceChangeListener l : listeners) {
                l.onConnectStatus(leftConnected, rightConnected);
            }
        });
    }

    private void updateListenerForOnReadingStatus(boolean isReading) {
        workHandler.post(() -> {
            for (DeviceChangeListener l : listeners) {
                l.onReadingStatus(isReading);
            }
        });
    }

    private void updateListenerForOnChangeSceneMode(AIDMode leftMode, AIDMode rightMode) {
        workHandler.post(() -> {
            for (DeviceChangeListener l : listeners) {
                l.onChangeSceneMode(leftMode, rightMode);
            }
        });
    }

    private void updateListenerForOnChangeModeFile(BTProtocol.ModeFileContent leftContent, BTProtocol.ModeFileContent rightContent) {
        workHandler.post(() -> {
            for (DeviceChangeListener l : listeners) {
                l.onChangeModeFile(leftContent, rightContent);
            }
        });
    }

    private void updateListenerForOnCtlFeedback(String leftResult, String rightResult) {
        workHandler.post(() -> {
            for (DeviceChangeListener l : listeners) {
                l.onCtlFeedback(leftResult, rightResult);
            }
        });
    }

    private void updateListenerForOnWriteFeedback(String leftResult, String rightResult) {
        workHandler.post(() -> {
            for (DeviceChangeListener l : listeners) {
                l.onWriteFeedback(leftResult, rightResult);
            }
        });
    }

    private static int BIT(int n) {
        return 1 << n;
    }

    private DeviceManager() {}

    public void init(Context context) {
        this.context = context;
        workThread.start();
        workHandler = new Handler(workThread.getLooper());
        BLEUtil.getInstance().addJHBleListener(mBLEListener);
        initRxListener();
    }

    public void unInit() {
        unInitRxListener();
        BLEUtil.getInstance().removeJHBLEListener(mBLEListener);
        workThread.quit();
        context = null;
    }

    public boolean getLeftConnected() {
        return this.leftConnected;
    }

    public boolean getRightConnected() {
        return this.rightConnected;
    }

    public String getLeftMac() {
        return this.leftMac;
    }

    public String getRightMac() {
        return this.rightMac;
    }

    public int getLeftBat() {
        return leftBat;
    }

    public int getRightBat() {
        return rightBat;
    }

    public void setLeftMode(AIDMode leftMode) {
        this.leftMode = leftMode;
    }

    public void setRightMode(AIDMode rightMode) {
        this.rightMode = rightMode;
    }

    public AIDMode getLeftMode() {
        return this.leftMode;
    }

    public AIDMode getRightMode() {
        return this.rightMode;
    }

    public BLEUtil.BLEDevice getLeftPairedDevice() {
        return this.leftPairedDevice;
    }

    public BLEUtil.BLEDevice getRightPairedDevice() {
        return this.rightPairedDevice;
    }

    public void setLeftPairedDevice(BLEUtil.BLEDevice bleDevice) {
        this.leftPairedDevice = bleDevice;
    }

    public void setRightPairedDevice(BLEUtil.BLEDevice bleDevice) {
        this.rightPairedDevice = bleDevice;
    }
    // 获取保存的配对的设备

    public void readPairedDevice() {
        leftPairedDevice = new BLEUtil.BLEDevice();
        if (!FileUtil.readPairedDevice(context, "Left", leftPairedDevice)) {
            leftPairedDevice = null;
        }
        rightPairedDevice = new BLEUtil.BLEDevice();
        if (!FileUtil.readPairedDevice(context, "Right", rightPairedDevice)) {
            rightPairedDevice = null;
        }
    }

    // ++++++++++++++++++++++++++ 协议进一步的封装 ++++++++++++++++++++++++++

    // 读取模式和音量
    public boolean readModeVolume(String mac) {
        byte[] data = BTProtocol.share.buildCMD_ReadModeVolume((byte) 0xFF);
        boolean result = BLEUtil.getInstance().writeCharacteristic(mac, data);
        return result;
    }

    // forceRead: 是否强制读取模式和音量
    public void readModeVolume(boolean forceRead) {
        modeCnt = 0;
        boolean resultL = false;
        boolean resultR = false;
        byte[] data = BTProtocol.share.buildCMD_ReadModeVolume((byte) 0xFF);
        if (leftConnected && (forceRead || leftMode == null)) {
            Log.d(TAG, "读取 左耳 当前模式 及 档位 ");
            leftMode = null;
            modeCnt = modeCnt & ~BIT(0);
            resultL = BLEUtil.getInstance().writeCharacteristic(leftMac, data);
        }
        if (rightConnected && (forceRead || rightMode == null)) {
            Log.d(TAG, "读取 右耳 当前模式 及 档位 ");
            rightMode = null;
            modeCnt = modeCnt & ~BIT(1);
            resultR = BLEUtil.getInstance().writeCharacteristic(rightMac, data);
        }
        if (resultL || resultR) { updateListenerForOnReadingStatus(true); }
    }

    public boolean readModeFile(String mac, AIDMode mode) {
        byte[] data = BTProtocol.share.buildCMD_ReadModeFile(mode);
        return BLEUtil.getInstance().writeCharacteristic(mac, data);
    }

    // 控制模式
    public boolean ctlMode(String mac, AIDMode mode) {
        byte[] data = BTProtocol.share.buildCMD_CtlMode(mode);
        return BLEUtil.getInstance().writeCharacteristic(mac, data);
    }

    // 控制模式
    public boolean ctlMode(AIDMode mode) {
        leftResult = null;
        rightResult = null;
        ctlFeedbackCnt =0;
        boolean resultL = false;
        boolean resultR = false;
        byte[] data = BTProtocol.share.buildCMD_CtlMode(mode);
        if (leftConnected) {
            resultL = BLEUtil.getInstance().writeCharacteristic(leftMac, data);
        }
        if (rightConnected) {
            resultR = BLEUtil.getInstance().writeCharacteristic(rightMac, data);
        }
        if (leftConnected || rightConnected) { updateListenerForOnReadingStatus(true); }
        return resultL && resultR;
    }

    // 控制音量
    public boolean ctlVolume(String mac, AIDMode mode) {
        byte[] data = BTProtocol.share.buildCMD_CtlVC(mode);
        return BLEUtil.getInstance().writeCharacteristic(mac, data);
    }

    // 读取模式文件
    public void readModeFile(AIDMode mode) {
        // 判断是否需要再次读模式，如果存在模式文件，且模式文件中的模式与要读取的模式相同就不需要再读了。
        boolean needReadLeftFlag = leftConnected;
        boolean needReadRightFlag = rightConnected;
        if (leftConnected && leftModeFileContent != null && leftModeFileContent.aidMode.getMode() == mode.getMode()) {
            needReadLeftFlag = false;
        }
        if (rightConnected && rightModeFileContent != null && rightModeFileContent.aidMode.getMode() == mode.getMode()) {
            needReadRightFlag = false;
        }
        if (!needReadLeftFlag && !needReadRightFlag) {
            updateListenerForOnChangeModeFile(leftModeFileContent, rightModeFileContent);
            return;
        }

        boolean resultL = false;
        boolean resultR = false;
        byte[] data = BTProtocol.share.buildCMD_ReadModeFile(mode);
        if (leftConnected) {
            leftModeFileContent = null;
            mutableLeftModeFileContent = null;
            modeFileCnt = modeFileCnt & ~BIT(0);
            resultL = BLEUtil.getInstance().writeCharacteristic(leftMac, data);
        }
        if (rightConnected) {
            rightModeFileContent = null;
            mutableRightModeFileContent = null;
            modeFileCnt = modeFileCnt & ~BIT(1);
            resultR = BLEUtil.getInstance().writeCharacteristic(rightMac, data);
        }
        if (resultL || resultR) { updateListenerForOnReadingStatus(true); }
    }

    // 设置模式文件
    public void writeModeFileForEQ(BTProtocol.ModeFileContent leftContent, BTProtocol.ModeFileContent rightContent) {
        leftResult = null;
        rightResult = null;
        boolean resultL = false;
        boolean resultR = false;
        if (leftConnected && leftContent != null) {
            writeFeedbackCnt = writeFeedbackCnt & ~BIT(0);
            BTProtocol.ModeFileContent.copyEQ(leftModeFileContent, leftContent, "V2");
            byte[] data = BTProtocol.share.buildCMD_WriteModeFile(leftModeFileContent, leftModeFileContent.aidMode);
            resultL = BLEUtil.getInstance().writeCharacteristic(leftMac, data);
        }
        if (rightConnected && rightContent != null) {
            writeFeedbackCnt = writeFeedbackCnt & ~BIT(1);
            BTProtocol.ModeFileContent.copyEQ(rightModeFileContent, rightContent, "V2");
            byte[] data = BTProtocol.share.buildCMD_WriteModeFile(rightModeFileContent, rightModeFileContent.aidMode);
            resultR = BLEUtil.getInstance().writeCharacteristic(rightMac, data);
        }
        if (resultL || resultR) { updateListenerForOnReadingStatus(true); }
    }

    // 设置Directional
    public void writeModeFileForDirectional(BTProtocol.Focus directional) {
        leftResult = null;
        rightResult = null;
        boolean resultL = false;
        boolean resultR = false;
        if (leftConnected) {
            writeFeedbackCnt = writeFeedbackCnt & ~BIT(0);
            leftModeFileContent.setFocus(directional);
            byte[] data = BTProtocol.share.buildCMD_WriteModeFile(leftModeFileContent, leftModeFileContent.aidMode);
            resultL = BLEUtil.getInstance().writeCharacteristic(leftMac, data);
        }
        if (rightConnected) {
            writeFeedbackCnt = writeFeedbackCnt & ~BIT(1);
            rightModeFileContent.setFocus(directional);
            byte[] data = BTProtocol.share.buildCMD_WriteModeFile(rightModeFileContent, rightModeFileContent.aidMode);
            resultR = BLEUtil.getInstance().writeCharacteristic(rightMac, data);
        }
        if (resultL || resultR) { updateListenerForOnReadingStatus(true); }
    }

    // 设置Noise
    public void writeModeFileForLoud(String earType, BTProtocol.Loud loud) {
        leftResult = null;
        rightResult = null;
        boolean resultL = false;
        boolean resultR = false;
        if (leftConnected && (earType.equals(EAR_TYPE_LEFT) || earType.equals(EAR_TYPE_BOTH))) {
            writeFeedbackCnt = writeFeedbackCnt & ~BIT(0);
            leftModeFileContent.setLoud(loud);
            byte[] data = BTProtocol.share.buildCMD_WriteModeFile(leftModeFileContent, leftModeFileContent.aidMode);
            resultL = BLEUtil.getInstance().writeCharacteristic(leftMac, data);
        }
        if (rightConnected && (earType.equals(EAR_TYPE_RIGHT) || earType.equals(EAR_TYPE_BOTH))) {
            writeFeedbackCnt = writeFeedbackCnt & ~BIT(1);
            rightModeFileContent.setLoud(loud);
            byte[] data = BTProtocol.share.buildCMD_WriteModeFile(rightModeFileContent, rightModeFileContent.aidMode);
            resultR = BLEUtil.getInstance().writeCharacteristic(rightMac, data);
        }
        if (resultL || resultR) { updateListenerForOnReadingStatus(true); }
    }

    // 设置Noise
    public void writeModeFileForNoise(String earType, BTProtocol.Noise noise) {
        leftResult = null;
        rightResult = null;
        boolean resultL = false;
        boolean resultR = false;
        if (leftConnected && (earType.equals(EAR_TYPE_LEFT) || earType.equals(EAR_TYPE_BOTH))) {
            writeFeedbackCnt = writeFeedbackCnt & ~BIT(0);
            leftModeFileContent.setNoise(noise);
            byte[] data = BTProtocol.share.buildCMD_WriteModeFile(leftModeFileContent, leftModeFileContent.aidMode);
            resultL = BLEUtil.getInstance().writeCharacteristic(leftMac, data);
        }
        if (rightConnected && (earType.equals(EAR_TYPE_RIGHT) || earType.equals(EAR_TYPE_BOTH))) {
            writeFeedbackCnt = writeFeedbackCnt & ~BIT(1);
            rightModeFileContent.setNoise(noise);
            byte[] data = BTProtocol.share.buildCMD_WriteModeFile(rightModeFileContent, rightModeFileContent.aidMode);
            resultR = BLEUtil.getInstance().writeCharacteristic(rightMac, data);
        }
        if (resultL || resultR) { updateListenerForOnReadingStatus(true); }
    }

    // ----------------------------------------------------------------------------------

    // 判断连接成功的设备是否有状态变化，用于决定是否需要重新获取助听器信息
    private final HashMap<String, Integer> mapMacStatus = new HashMap<>(5);
    private boolean isStatusChanged(List<BLEUtil.BLEDevice> bleDevices) {
        if (bleDevices.size() == 0) { return false; }
        int flag = 0;
        for (BLEUtil.BLEDevice device : bleDevices) {
            Integer status = mapMacStatus.get(device.mac);
            if (status == null) { // 如果还没有保存过这个设备，认为状态有变化
                mapMacStatus.put(device.mac, device.connectStatus);
                flag = 1;
                break;

            } else if (device.connectStatus == BLEUtil.STATE_RECONNECTING ) { // 有重连操作，延时3000ms读设备
               // delay = 3000;

            } else if (Math.abs(device.connectStatus - status) == 4) { // 状态 从连接到断开相关转换时，认为有状态变化。需要再次读取。
                // Log.d(TAG,  "isStatusChanged: " + device.deviceName + " oldStatus=" + status + " newStatus=" + device.connectStatus );
                mapMacStatus.put(device.mac, device.connectStatus);
                flag++;
            }
        }
        Log.d(TAG,  "isStatusChanged: " + (flag > 0));
        return flag > 0;
    }

    private final BLEUtil.JHBLEListener mBLEListener =  new BLEUtil.JHBLEListener() {

        @Override
        public void updateBLEDevice(List<BLEUtil.BLEDevice> bleDevices) {
            Log.d(TAG, "BLEDevice size = " + bleDevices.size());
            mBleDevices = bleDevices;

            leftMac         = null;
            rightMac        = null;
            leftConnected   = false;
            rightConnected  = false;
            connectedCnt    = 0;
            delay           = 0;

            for (BLEUtil.BLEDevice device: mBleDevices) {
                Log.d(TAG, device.deviceName + " ConnectStatus = " + device.connectStatus);
                // 如果正在连接
                if (device.connectStatus == BLEUtil.STATE_CONNECTING) {

                    if (device.deviceName.contains("-L")) {
                        readLeftBat = false;
                        leftBat = 0;
                        leftMode = null;
                    } else if (device.deviceName.contains("-R")) {
                        readRightBat = false;
                        rightBat = 0;
                        rightMode = null;
                    }

                }  else if (device.connectStatus == BLEUtil.STATE_CONNECTED || device.connectStatus == BLEUtil.STATE_GET_GATT_SERVICES_OVER) {

                    // 记录连接成功的设备个数,并读取一次电量
                    if (device.connectStatus == BLEUtil.STATE_GET_GATT_SERVICES_OVER && device.deviceName.contains("-L")) {
                        connectedCnt = connectedCnt | BIT(0);
                        leftConnected = true;
                        leftPairedDevice = device;
                        leftMac = device.mac;
                        connectedDeviceMacSet.add(device.mac);
                        delay = !readLeftBat ? 1500 : 500; // 如果没读电量，先读电量
                        if (!readLeftBat) {
                            Log.d(TAG, "读取 左耳 电量");
                            readLeftBat = true;
                            workHandler.postDelayed(() -> BLEUtil.getInstance().readBatValue(device.mac), 500);
                        }

                    } else if (device.connectStatus == BLEUtil.STATE_GET_GATT_SERVICES_OVER && device.deviceName.contains("-R")) {
                        connectedCnt = connectedCnt | BIT(1);
                        rightConnected = true;
                        rightPairedDevice = device;
                        rightMac = device.mac;
                        connectedDeviceMacSet.add(device.mac);
                        delay = !readRightBat ? 1500 : 500; // 如果没读电量，先读电量
                        if ((!readRightBat)) {
                            Log.d(TAG, "读取 右耳 电量");
                            readRightBat = true;
                            workHandler.postDelayed(() -> BLEUtil.getInstance().readBatValue(device.mac), 500);
                        }
                    }

                }  else if (device.connectStatus == BLEUtil.STATE_RECONNECTING) {
//                    if (device.deviceName.contains("-L")) {
//                        readLeftBat = false;
//                        leftBat = 0;
//                        leftMode = null;
//                    } else if (device.deviceName.contains("-R")) {
//                        readRightBat = false;
//                        rightBat = 0;
//                        rightMode = null;
//                    }

                }  else if (device.connectStatus == BLEUtil.STATE_DISCONNECTED) {
                    // 更新连接成功设备Set，从连接成功到连接失败时，需要将leftMode和rightMode置成空
                    for (String mac : connectedDeviceMacSet) {
                        if (device.mac.equals(mac)) {
                            connectedDeviceMacSet.remove(mac);
                            if (device.deviceName.contains("-L")) {
                                leftMode = null;
                                leftModeFileContent = null;
                            } else if (device.deviceName.contains("-R")) {
                                rightMode = null;
                                rightModeFileContent = null;
                            }
                            break;
                        }
                    }
                }
            }
            if (isStatusChanged(bleDevices)) { updateListenerForOnConnectStatus(leftConnected, rightConnected); }
        }

        @Override
        public void onBatteryChanged(String deviceName, int value) {
            Log.d(TAG, "onBatteryChanged " + deviceName + " " + value);
            if (deviceName == null) { return; }
            if (deviceName.contains("-L")) {
                leftBat = value;
            } else if (deviceName.contains("-R")) {
                rightBat = value;
            }
        }

        @Override
        public void onReadChanged(String deviceName, byte[] values) {

        }
    };

    private void initRxListener() {
        // 带有一个Consumer参数的方法表示下游只关心onNext事件
        disposable0 = BTProtocol.share.sceneModeObservable.subscribe(aidMode -> {
                    Log.d(TAG, "获取模式成功 " + aidMode);
                    if (aidMode.getDeviceName().contains("-L")) {
                        if(aidMode.getType() == BTProtocol.Read_Success ) {
                            modeCnt = modeCnt | BIT(0);
                        }
                        leftMode = aidMode;

                    } else if (aidMode.getDeviceName().contains("-R")) {
                        if(aidMode.getType() == BTProtocol.Read_Success ) {
                            modeCnt = modeCnt | BIT(1);
                        }
                        rightMode = aidMode;
                    }

                    Log.d(TAG, String.format(Locale.getDefault(), "modeCnt(%d) connectedCnt(%d)", modeCnt, connectedCnt));
                    if (aidMode.getType() == BTProtocol.Read_Success) {
                        if (modeCnt == connectedCnt) {
                            updateListenerForOnChangeSceneMode(leftMode, rightMode);
                            updateListenerForOnReadingStatus(false);
                        }

                    } else if (aidMode.getType() == BTProtocol.Report_Success) {
//                        if (connectedCnt == 2 && leftMode != rightMode) {
//                            if (sceneMode.getDeviceName().contains("-L")) {
//                                syncMode(sceneMode, "-R");
//                            } else if (sceneMode.getDeviceName().contains("-R")) {
//                                syncMode(sceneMode, "-L");
//                            }
//                        } else if (connectedCnt == 1) {
//
//                        }
                    }
                });


        // 获取模式文件
        disposable1 = BTProtocol.share.modeFileContentObservable.subscribe(modeFileContent -> {
                    if (modeFileContent.aidMode.getDeviceName().contains("-L")) {
                        Log.d(TAG, modeFileContent.aidMode.getDeviceName() + " 获取模式文件成功");
                        leftModeFileContent = modeFileContent;
                        mutableLeftModeFileContent = modeFileContent;
                        modeFileCnt = modeFileCnt | BIT(0);

                    } else if (modeFileContent.aidMode.getDeviceName().contains("-R")) {
                        Log.d(TAG, modeFileContent.aidMode.getDeviceName() + " 获取模式文件成功");
                        rightModeFileContent = modeFileContent;
                        mutableRightModeFileContent = modeFileContent;
                        modeFileCnt = modeFileCnt | BIT(1);
                    }
                    Log.d(TAG, String.format(Locale.getDefault(), "modeFileCnt(%d) connectedCnt(%d)", modeFileCnt, connectedCnt));
                    if (modeFileCnt == connectedCnt) {
                        updateListenerForOnChangeModeFile(mutableLeftModeFileContent, mutableRightModeFileContent);
                        updateListenerForOnReadingStatus(false);
                    }
                });

        disposable2 = BTProtocol.share.ctlFeedbackObservable.subscribe(result -> {
            Log.d(TAG, "控制命令回调 result = " + result);
            String[] strings = result.split(",");
            if (strings[0].contains("-L")) {
                leftResult = result;
                ctlFeedbackCnt = ctlFeedbackCnt | BIT(0);
            } else if (strings[0].contains("-R")) {
                rightResult = result;
                ctlFeedbackCnt = ctlFeedbackCnt | BIT(1);
            }
            if (ctlFeedbackCnt == connectedCnt) {
                updateListenerForOnCtlFeedback(leftResult, rightResult);
            }
        });

        // 设置EQ成功
        disposable3 = BTProtocol.share.writeFeedbackObservable.subscribe(result -> {
            String[] name_isSuccess = result.split(",");
            String name = name_isSuccess[0];
            boolean isSuccess = Boolean.parseBoolean(name_isSuccess[1]);
            if (name.contains("-L") && isSuccess) {
                Log.d(TAG, name + " 写模式文件成功");
                leftResult = result;
                writeFeedbackCnt = writeFeedbackCnt | BIT(0);
            } else if (name.contains("-R") && isSuccess) {
                Log.d(TAG, name + " 写模式文件成功");
                rightResult = result;
                writeFeedbackCnt = writeFeedbackCnt | BIT(1);
            }
            if (writeFeedbackCnt == connectedCnt) {
                updateListenerForOnWriteFeedback(leftResult, rightResult);
                updateListenerForOnReadingStatus(false);
            }
        });
    }

    private void unInitRxListener() {
        if (disposable0 != null) {
            disposable0.dispose();
            disposable0 = null;
        }

        if (disposable1 != null) {
            disposable1.dispose();
            disposable1 = null;
        }

        if (disposable2 != null) {
            disposable2.dispose();
            disposable2 = null;
        }

        if (disposable3 != null) {
            disposable3.dispose();
            disposable3 = null;
        }
    }
}
