package com.upixels.jh.hearingassist.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.upixels.jh.hearingassist.ConnectActivity;
import com.upixels.jh.hearingassist.MainActivity;
import com.upixels.jh.hearingassist.R;
import com.upixels.jh.hearingassist.databinding.FragmentHomeBinding;
import com.upixels.jh.hearingassist.entity.BLEDeviceEntity;
import com.upixels.jh.hearingassist.util.DeviceManager;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import me.forrest.commonlib.util.BLEUtil;
import me.forrest.commonlib.util.FileUtil;
import me.forrest.commonlib.view.IOSLoadingDialog;


public class HomeFragment extends Fragment {
    private final static String TAG = "HomeFragment";
    private FragmentHomeBinding binding;

    private boolean visible;
    private final Handler mUIHandler = new Handler();
    private Runnable dismissDialogRunnable;

    private boolean leftConnected;
    private boolean rightConnected;
    private boolean readLeftBat;  //是否读了一次电量
    private boolean readRightBat; //是否读了一次电量
    private List<BLEUtil.BLEDevice> mBleDevices;
    private final List<BLEDeviceEntity> mScannedDeviceEntities =  new ArrayList<>(5); // 扫描到的设备
    private BLEUtil.BLEDevice leftPairedDevice;
    private BLEUtil.BLEDevice rightPairedDevice;
    private boolean autoConnectLeft;  // 是否需要主动连接左耳
    private boolean autoConnectRight; // 是否需要主动连接右耳
    private int connectedCnt = 0;
    private int scanCnt = 0;   // 记录第几次发起扫描，第一次扫描时为了快速连接设备，扫描到两个设备，并连接上了就不扫了。重新发起扫描时，扫描10S，

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        Log.d(TAG, "[onCreate]");
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView();
        // 添加BLEUtil监听回调
        BLEUtil.getInstance().addJHBleListener(mBLEListener);
    }

    @Override
    public void onStart() {
        Log.d(TAG, "[onStart]");
        super.onStart();
        visible = true;
        DeviceManager.getInstance().readPairedDevice();
        leftPairedDevice = DeviceManager.getInstance().getLeftPairedDevice();
        rightPairedDevice = DeviceManager.getInstance().getRightPairedDevice();
        BLEUtil.getInstance().updateListenerForBLEDevices();
//        requestPermissions();
    }

    @Override
    public void onResume() {
        Log.d(TAG, "[onResume]");
        super.onResume();
    }

    @Override
    public void onPause() {
        Log.d(TAG, "[onPause]");
        super.onPause();
    }

    @Override
    public void onStop() {
        Log.d(TAG, "[onStop]");
        super.onStop();
        visible = false;
    }

    @Override
    public void onDestroyView() {
        Log.d(TAG, "[onDestroyView]");
        super.onDestroyView();
        binding = null;
        // 删除BLEUtil监听
        BLEUtil.getInstance().removeJHBLEListener(mBLEListener);
    }

    private void initView() {
        binding.btnSearchDevice.setOnClickListener(v -> {
            startActivity(new Intent(requireContext(), ConnectActivity.class));
        });

        binding.layoutSoundControl.setOnClickListener(v -> {
            startActivity(new Intent(requireContext(), MainActivity.class));
        });
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "[onDestroy]");
        super.onDestroy();
    }

    // 判断是否扫描到配对过的设备
    private boolean hasPairedDevice(BLEUtil.BLEDevice pDevice, List<BLEUtil.BLEDevice> deviceList) {
        for (BLEUtil.BLEDevice device : deviceList) {
            if (device.mac.equals(pDevice.mac)) {
                return true;
            }
        }
        return false;
    }

    private final BLEUtil.JHBLEListener mBLEListener =  new BLEUtil.JHBLEListener() {
        boolean displayLoading = false;    // 是否需要等待BLE获取服务器完成
        String displayLoadingDeviceName;   // 正在连接设备的名字

        @Override
        public void updateBLEDevice(List<BLEUtil.BLEDevice> bleDevices) {
            if (!visible) { return; }
            mBleDevices = bleDevices;
            Log.d(TAG, "mBluetoothDevices size = " + mBleDevices.size());

            leftConnected   = false;
            rightConnected  = false;
            connectedCnt = 0;

            mScannedDeviceEntities.clear();


            for (BLEUtil.BLEDevice device: mBleDevices) {
                Log.d(TAG, device.deviceName + " " + device.connectStatus);
                // 更新配对设备列表
                if (device.connectStatus != BLEUtil.STATE_CONNECTED && device.connectStatus != BLEUtil.STATE_GET_GATT_SERVICES_OVER) {

                    // 未在配对过的链表里面，添加进List
                    BLEDeviceEntity entity = new BLEDeviceEntity(false);
                    entity.section       = 0;
                    entity.deviceName    = device.deviceName;
                    entity.mac           = device.mac;
                    entity.connectStatus = device.connectStatus;
                    entity.devType       = device.devType;
                    entity.isScanned     = true;
                    mScannedDeviceEntities.add(entity);

                    // 如果正在连接
                    if (device.connectStatus == BLEUtil.STATE_CONNECTING) {
                        displayLoadingDeviceName = device.deviceName;
                        displayLoading = true;

                        mUIHandler.post(() -> IOSLoadingDialog.instance.setOnTouchOutside(false).showDialog(getParentFragmentManager(), ""));
                        mUIHandler.postDelayed(IOSLoadingDialog.instance::dismissDialog, 10000);
                    }

                // 更新配对设备列表 (STATE_CONNECTED, STATE_GET_GATT_SERVICES_OVER)
                } else {
                    // 记录连接成功的设备个数,并读取一次电量
                    if (device.connectStatus == BLEUtil.STATE_GET_GATT_SERVICES_OVER && device.deviceName.contains("-L")) {
                        connectedCnt++;
                        leftConnected = true;
                        leftPairedDevice = device;
                        if ((!readLeftBat)) {
                            readLeftBat = true;
                            mUIHandler.postDelayed(() -> BLEUtil.getInstance().readBatValue(device.mac), 500);
                        }

                    } else if (device.connectStatus == BLEUtil.STATE_GET_GATT_SERVICES_OVER && device.deviceName.contains("-R")) {
                        connectedCnt++;
                        rightConnected = true;
                        rightPairedDevice = device;
                        if ((!readRightBat)) {
                            readRightBat = true;
                            mUIHandler.postDelayed(() -> BLEUtil.getInstance().readBatValue(device.mac), 500);
                        }
                    }
                }

            }

            // 更新 底部我的设备部分的UI
            mUIHandler.post(() -> {
                // 第一次扫描时，为了快速可以交互，连接2个设备后，立即停止扫描。
                if (connectedCnt == 2) {
//                    mUIHandler.removeCallbacks(dismissDialogRunnable);
                    BLEUtil.getInstance().scanLeDevice(false);
                    IOSLoadingDialog.instance.dismissDialog();
                    binding.btnSearchDevice.setEnabled(true);
                }

                if (leftPairedDevice == null && rightPairedDevice == null) {
                    binding.btnSearchDevice.setVisibility(View.VISIBLE);
                    binding.layoutLeftDevice.setVisibility(View.GONE);
                    binding.layoutRightDevice.setVisibility(View.GONE);
                } else {
                    binding.btnSearchDevice.setVisibility(View.GONE);
                    binding.layoutLeftDevice.setVisibility(View.VISIBLE);
                    binding.layoutRightDevice.setVisibility(View.VISIBLE);
                }

                if (leftPairedDevice != null) {
                    binding.ivLeftDisconnected.setVisibility(leftConnected ? View.GONE : View.VISIBLE);
                    binding.tvLeftDisconnected.setVisibility(leftConnected ? View.GONE : View.VISIBLE);
                    binding.tvLeftName.setVisibility(!leftConnected ? View.GONE : View.VISIBLE);
                    binding.tvLeftName.setText(leftPairedDevice.deviceName);
                    binding.bvLeftBattery.setVisibility(!leftConnected ? View.GONE : View.VISIBLE);
                    binding.tvLeftBattery.setVisibility(!leftConnected ? View.GONE : View.VISIBLE);
                }
                if (rightPairedDevice != null) {
                    binding.ivRightDisconnected.setVisibility(rightConnected ? View.GONE : View.VISIBLE);
                    binding.tvRightDisconnected.setVisibility(rightConnected ? View.GONE : View.VISIBLE);
                    binding.tvRightName.setVisibility(!rightConnected ? View.GONE : View.VISIBLE);
                    binding.tvRightName.setText(rightPairedDevice.deviceName);
                    binding.bvRightBattery.setVisibility(!rightConnected ? View.GONE : View.VISIBLE);
                    binding.tvRightBattery.setVisibility(!rightConnected ? View.GONE : View.VISIBLE);
                }
            });
        }

        @Override
        public void onBatteryChanged(String deviceName, int value) {
            Log.d(TAG, "onBatteryChanged " + deviceName + " " + value);
            if (deviceName == null) { return; }
            requireActivity().runOnUiThread(() -> {
                if (deviceName.contains("-L")) {
                    binding.bvLeftBattery.setBattery((float) value / 100.f);
                    binding.tvLeftBattery.setText("" + value + "%");
                    if (value < 10) {binding.tvLeftBattery.setTextColor(0xFFE22732);}
                    else if (value < 30) {binding.tvLeftBattery.setTextColor(0xFFF06D06);}
                    else { binding.tvLeftBattery.setTextColor(0xFF16DC8F); }

                } else if (deviceName.contains("-R")) {
                    binding.bvRightBattery.setBattery((float) value / 100.f);
                    binding.tvRightBattery.setText("" + value + "%");
                    if (value < 10) {binding.tvRightBattery.setTextColor(0xFFE22732);}
                    else if (value < 30) {binding.tvRightBattery.setTextColor(0xFFF06D06);}
                    else { binding.tvRightBattery.setTextColor(0xFF16DC8F); }
                }
            });
        }

        @Override
        public void onReadChanged(String deviceName, byte[] values) {

        }
    };

}