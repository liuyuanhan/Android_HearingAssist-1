package com.upixels.jh.hearingassist.util;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import java.util.LinkedList;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.Disposable;
import me.forrest.commonlib.jh.BTProtocol;
import me.forrest.commonlib.jh.SceneMode;
import me.forrest.commonlib.util.BLEUtil;
import me.forrest.commonlib.util.FileUtil;
import me.forrest.commonlib.view.IOSLoadingDialog;

public class DeviceManager {
    private static final String TAG = DeviceManager.class.getSimpleName();

    private static final DeviceManager ourInstance = new DeviceManager();

    public static DeviceManager getInstance() {
        return ourInstance;
    }

    public static final String EAR_TYPE_LEFT  = "left";
    public static final String EAR_TYPE_RIGHT = "right";

    private Context     context;
    private boolean     leftConnected;
    private boolean     rightConnected;
    private boolean     readLeftBat;  //是否读了一次电量
    private boolean     readRightBat; //是否读了一次电量
    private int         leftBat;
    private int         rightBat;
    private SceneMode   leftMode;
    private SceneMode   rightMode;
    private String      leftMac;
    private String      rightMac;
    private String      leftResult;
    private String      rightResult;
    private BTProtocol.ModeFileContent leftModeFileContent;
    private BTProtocol.ModeFileContent rightModeFileContent;
    private BTProtocol.ModeFileContent mutableLeftModeFileContent;
    private BTProtocol.ModeFileContent mutableRightModeFileContent;
    private int         connectedCnt = 0;
    private int         modeCnt;            //获取到的模式数量
    private int         modeFileCnt;
    private int         writeFeedbackCnt;
    private int         ctlFeedbackCnt;

    private BLEUtil.BLEDevice leftPairedDevice;
    private BLEUtil.BLEDevice rightPairedDevice;
    private List<BLEUtil.BLEDevice> mBleDevices;

    private Disposable  disposable0;
    private Disposable  disposable1;
    private Disposable  disposable2;
    private Disposable  disposable3;

    private final HandlerThread workThread = new HandlerThread("WorkThread");
    private Handler             workHandler;
    private long                delay;

    private final List<DeviceChangeListener> listeners = new LinkedList<>();

    public interface DeviceChangeListener {
        // 是否正在读数过程中，用于UI显示圆圈
        void onReadingStatus(boolean isReading);
        // 电量变化回调
        void onChangeBat(int leftBat, int rightBat);
        // 模式变化回调
        void onChangeSceneMode(SceneMode leftMode, SceneMode rightMode);

        // 模式文件变化回调
        void onChangeModeFile(BTProtocol.ModeFileContent leftContent, BTProtocol.ModeFileContent rightContent);

        // 控制命令回调
        void onCtlFeedback(String leftResult, String rightResult);

        // 写命令Feedback回调
        void onWriteFeedback(String leftResult, String rightResult);
    }

    public void addListener(DeviceChangeListener listener) {
        listeners.add(listener);
    }

    public void removeListener(DeviceChangeListener listener) {
        listeners.remove(listener);
    }

    private void updateListenerForOnReadingStatus(boolean isReading) {
        workHandler.post(() -> {
            for (DeviceChangeListener l : listeners) {
                l.onReadingStatus(isReading);
            }
        });
    }

    private void updateListenerForOnChangeSceneMode(SceneMode leftMode, SceneMode rightMode) {
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

    private DeviceManager() {}

    public void init(Context context) {
        this.context = context;
        workThread.start();
        workHandler = new Handler(workThread.getLooper());
        BLEUtil.getInstance().addJHBleListener(mBLEListener);
        initRxListener();
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

    public void setLeftMode(SceneMode leftMode) {
        this.leftMode = leftMode;
    }

    public void setRightMode(SceneMode rightMode) {
        this.rightMode = rightMode;
    }

    public SceneMode getLeftMode() {
        return this.leftMode;
    }

    public SceneMode getRightMode() {
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

    // ************************ 协议进一步的封装 ************************
    // 读取模式和音量
    public boolean readModeVolume(String mac) {
        byte[] data = BTProtocol.share.buildCMD_ReadModeVolume((byte) 0xFF);
        boolean result = BLEUtil.getInstance().writeCharacteristic(mac, data);
        return result;
    }

    public boolean readModeVolume() {
        modeCnt = 0;
        boolean resultL = true;
        boolean resultR = true;
        byte[] data = BTProtocol.share.buildCMD_ReadModeVolume((byte) 0xFF);
        if (leftConnected) {
            resultL = BLEUtil.getInstance().writeCharacteristic(leftMac, data);
        }
        if (rightConnected) {
            resultR = BLEUtil.getInstance().writeCharacteristic(rightMac, data);
        }
        return resultL && resultR;
    }

    public boolean readModeFile(String mac, SceneMode mode) {
        byte[] data = BTProtocol.share.buildCMD_ReadModeFile(mode);
        return BLEUtil.getInstance().writeCharacteristic(mac, data);
    }

    // 控制模式
    public boolean ctlMode(String mac, SceneMode mode) {
        byte[] data = BTProtocol.share.buildCMD_CtlMode(mode);
        return BLEUtil.getInstance().writeCharacteristic(mac, data);
    }

    // 控制模式
    public boolean ctlMode(SceneMode mode) {
        leftResult = null;
        rightResult = null;
        ctlFeedbackCnt =0;
        boolean resultL = true;
        boolean resultR = true;
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
    public boolean ctlVolume(String mac, SceneMode mode) {
        byte[] data = BTProtocol.share.buildCMD_CtlVC(mode);
        return BLEUtil.getInstance().writeCharacteristic(mac, data);
    }

    // 读取模式文件
    public boolean readModeFile(SceneMode mode) {
        leftModeFileContent = null;
        rightModeFileContent = null;
        mutableLeftModeFileContent = null;
        mutableRightModeFileContent = null;
        modeFileCnt = 0;
        boolean resultL = true;
        boolean resultR = true;
        byte[] data = BTProtocol.share.buildCMD_ReadModeFile(mode);
        if (leftConnected) {
            resultL = BLEUtil.getInstance().writeCharacteristic(leftMac, data);
        }
        if (rightConnected) {
            resultR = BLEUtil.getInstance().writeCharacteristic(rightMac, data);
        }
        if (resultL || resultR) { updateListenerForOnReadingStatus(true); }
        return resultL && resultR;
    }

    // 设置模式文件
    public boolean writeModeFileForEQ(BTProtocol.ModeFileContent leftContent, BTProtocol.ModeFileContent rightContent) {
        leftResult = null;
        rightResult = null;
        writeFeedbackCnt = 0;
        boolean resultL = true;
        boolean resultR = true;
        if (leftConnected && leftContent != null) {
            BTProtocol.ModeFileContent.copyEQ(leftModeFileContent, leftContent, "V2");
            byte[] data = BTProtocol.share.buildCMD_WriteModeFile(leftModeFileContent, leftModeFileContent.mode);
            resultL = BLEUtil.getInstance().writeCharacteristic(leftMac, data);
        }
        if (rightConnected && rightContent != null) {
            BTProtocol.ModeFileContent.copyEQ(rightModeFileContent, rightContent, "V2");
            byte[] data = BTProtocol.share.buildCMD_WriteModeFile(rightModeFileContent, rightModeFileContent.mode);
            resultR = BLEUtil.getInstance().writeCharacteristic(rightMac, data);
        }
        if (resultL || resultR) { updateListenerForOnReadingStatus(true); }
        return resultL && resultR;
    }
    // *******************************************************************

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
                Log.d(TAG, device.deviceName + " " + device.connectStatus);
                // 如果正在连接
                if (device.connectStatus == BLEUtil.STATE_CONNECTING) {
                    if (device.deviceName.contains("-L")) {
                        readLeftBat = false;
                        leftBat = 0;
                    } else if (device.deviceName.contains("-R")) {
                        readRightBat = false;
                        rightBat = 0;
                    }

                }  else if (device.connectStatus == BLEUtil.STATE_CONNECTED || device.connectStatus == BLEUtil.STATE_GET_GATT_SERVICES_OVER) {

                    // 记录连接成功的设备个数,并读取一次电量
                    if (device.connectStatus == BLEUtil.STATE_GET_GATT_SERVICES_OVER && device.deviceName.contains("-L")) {
                        connectedCnt++;
                        leftConnected = true;
                        leftPairedDevice = device;
                        leftMac = device.mac;
                        delay = !readLeftBat ? 1500 : 500; // 如果没读电量，先读电量，再读模式音量
                        if (!readLeftBat) {
                            readLeftBat = true;
                            Log.d(TAG, "读取 左耳 电量");
                            workHandler.postDelayed(() -> BLEUtil.getInstance().readBatValue(device.mac), 500);
                        }

                        updateListenerForOnReadingStatus(true);
                        workHandler.postDelayed(() -> {
                            Log.d(TAG, "读取 左耳 当前模式 及 档位 ");
                            modeCnt = modeCnt > 0 ? modeCnt - 1 : 0;
                            leftMode = null;
                            DeviceManager.getInstance().readModeVolume(leftMac);
                        }, delay);

                    } else if (device.connectStatus == BLEUtil.STATE_GET_GATT_SERVICES_OVER && device.deviceName.contains("-R")) {
                        connectedCnt++;
                        rightConnected = true;
                        rightPairedDevice = device;
                        rightMac = device.mac;
                        delay = !readRightBat ? 1500 : 500; // 如果没读电量，先读电量，再读模式音量
                        if ((!readRightBat)) {
                            readRightBat = true;
                            workHandler.postDelayed(() -> BLEUtil.getInstance().readBatValue(device.mac), 500);
                        }
                        updateListenerForOnReadingStatus(true);
                        workHandler.postDelayed(() -> {
                            Log.d(TAG, "读取 右耳 当前模式 及 档位 ");
                            modeCnt = modeCnt > 0 ? modeCnt - 1 : 0;
                            rightMode = null;
                            DeviceManager.getInstance().readModeVolume(rightMac);
                        }, delay);
                    }
                }
            }
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
        disposable0 = BTProtocol.share.sceneModeObservable.subscribe(sceneMode -> {
                    Log.d(TAG, "获取模式成功 " + sceneMode);
                    if (sceneMode.getDeviceName().contains("-L")) {
                        if(sceneMode.getType() == BTProtocol.Read_Success ) {
                            modeCnt++;
                        }
                        leftMode = sceneMode;
//                        Log.d(TAG, "L 获取模式文件");
//                        DeviceManager.getInstance().setLeftMode(leftMode);
//                        DeviceManager.getInstance().readModeFile(leftMac, leftMode);

                    } else if (sceneMode.getDeviceName().contains("-R")) {
                        if(sceneMode.getType() == BTProtocol.Read_Success ) {
                            modeCnt++;
                        }
                        rightMode = sceneMode;
//                        Log.d(TAG, "R 获取模式文件");
//                        DeviceManager.getInstance().setRightMode(rightMode);
//                        DeviceManager.getInstance().readModeFile(rightMac, rightMode);
                    }

                    if (sceneMode.getType() == BTProtocol.Read_Success) {
                        if (modeCnt == connectedCnt) {
                            updateListenerForOnChangeSceneMode(leftMode, rightMode);
                            updateListenerForOnReadingStatus(false);
                        }
//                        if (modeCnt == connectedCnt && modeCnt == 2 && leftMode != rightMode) {
//                            IOSLoadingDialog.instance.dismiss();
//                            CommonUtil.showToast(this, getString(R.string.tips_mode_not_same));
//                        } else if (modeCnt == connectedCnt) {
//                            IOSLoadingDialog.instance.dismiss();
//                            runOnUiThread(() -> {
//                                Fragment fragment = getSupportFragmentManager().findFragmentByTag("f"+binding.viewPager.getCurrentItem());
//                                Log.d(TAG, "fragment = " + fragment);
//                            });
//                        }

                    } else if (sceneMode.getType() == BTProtocol.Report_Success) {
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
                    if (modeFileContent.mode.getDeviceName().contains("-L")) {
                        Log.d(TAG, modeFileContent.mode.getDeviceName() + " 获取模式文件成功");
                        leftModeFileContent = modeFileContent;
                        mutableLeftModeFileContent = modeFileContent;
                        modeFileCnt++;

                    } else if (modeFileContent.mode.getDeviceName().contains("-R")) {
                        Log.d(TAG, modeFileContent.mode.getDeviceName() + " 获取模式文件成功");
                        rightModeFileContent = modeFileContent;
                        mutableRightModeFileContent = modeFileContent;
                        modeFileCnt++;
                    }
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
                ctlFeedbackCnt++;
            } else if (strings[0].contains("-R")) {
                rightResult = result;
                ctlFeedbackCnt++;
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
                writeFeedbackCnt++;
            } else if (name.contains("-R") && isSuccess) {
                Log.d(TAG, name + " 写模式文件成功");
                rightResult = result;
                writeFeedbackCnt++;
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
