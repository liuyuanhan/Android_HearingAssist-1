package com.upixels.jh.hearingassist.util;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.View;

import com.upixels.jh.hearingassist.R;
import com.upixels.jh.hearingassist.entity.BLEDeviceEntity;

import java.util.List;

import me.forrest.commonlib.util.BLEUtil;
import me.forrest.commonlib.util.FileUtil;
import me.forrest.commonlib.view.IOSLoadingDialog;

public class DeviceManager {
    private static final String TAG = DeviceManager.class.getSimpleName();

    private static final DeviceManager ourInstance = new DeviceManager();

    public static DeviceManager getInstance() {
        return ourInstance;
    }

    private Context context;
    private boolean readLeftBat;  //是否读了一次电量
    private boolean readRightBat; //是否读了一次电量
    private int     leftBat;
    private int     rightBat;
    private boolean leftConnected;
    private boolean rightConnected;
    private int connectedCnt = 0;
    private BLEUtil.BLEDevice leftPairedDevice;
    private BLEUtil.BLEDevice rightPairedDevice;
    private List<BLEUtil.BLEDevice> mBleDevices;

    private HandlerThread workThread;
    private Handler workHandler;

    private DeviceManager() {}

    public void init(Context context) {
        this.context = context;
        workThread = new HandlerThread("WorkThread");
        workThread.start();
        workHandler = new Handler(workThread.getLooper());
        BLEUtil.getInstance().addJHBleListener(mBLEListener);
    }

    public int getLeftBat() {
        return leftBat;
    }

    public int getRightBat() {
        return rightBat;
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

    private final BLEUtil.JHBLEListener mBLEListener =  new BLEUtil.JHBLEListener() {

        @Override
        public void updateBLEDevice(List<BLEUtil.BLEDevice> bleDevices) {
            Log.d(TAG, "BLEDevice size = " + bleDevices.size());
            mBleDevices = bleDevices;

            leftConnected   = false;
            rightConnected  = false;
            connectedCnt = 0;

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

                }  else if (device.connectStatus == BLEUtil.STATE_CONNECTED
                        || device.connectStatus == BLEUtil.STATE_GET_GATT_SERVICES_OVER) {

                    // 记录连接成功的设备个数,并读取一次电量
                    if (device.connectStatus == BLEUtil.STATE_GET_GATT_SERVICES_OVER && device.deviceName.contains("-L")) {
                        connectedCnt++;
                        leftConnected = true;
                        leftPairedDevice = device;
                        if ((!readLeftBat)) {
                            readLeftBat = true;
                            workHandler.postDelayed(() -> BLEUtil.getInstance().readBatValue(device.mac), 500);
                        }

                    } else if (device.connectStatus == BLEUtil.STATE_GET_GATT_SERVICES_OVER && device.deviceName.contains("-R")) {
                        connectedCnt++;
                        rightConnected = true;
                        rightPairedDevice = device;
                        if ((!readRightBat)) {
                            readRightBat = true;
                            workHandler.postDelayed(() -> BLEUtil.getInstance().readBatValue(device.mac), 500);
                        }
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
}
