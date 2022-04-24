package com.upixels.jh.hearingassist.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.upixels.jh.hearingassist.ConnectActivity;
import com.upixels.jh.hearingassist.R;
import com.upixels.jh.hearingassist.databinding.FragmentHomeBinding;
import com.upixels.jh.hearingassist.entity.BLEDeviceEntity;

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
    private final List<BLEDeviceEntity> mPairedDeviceEntities = new ArrayList<>(5);   // 配对过的设备
    private HashSet<BLEUtil.BLEDevice> leftPairedDevices;
    private HashSet<BLEUtil.BLEDevice> rightPairedDevices;
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
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "[onDestroy]");
        super.onDestroy();
    }

    // 判断是否在配对过的记录里面
    private boolean isPairedDevice(BLEUtil.BLEDevice device) {
        if (device.deviceName.contains("-L")) {
            for (BLEUtil.BLEDevice dev : leftPairedDevices) {
                if (dev.mac.equals(device.mac)) {
                    return true;
                }
            }
        }

        if (device.deviceName.contains("-R")) {
            for (BLEUtil.BLEDevice dev : rightPairedDevices) {
                if (dev.mac.equals(device.mac)) {
                    return true;
                }
            }
        }
        return false;
    }

    private BLEUtil.BLEDevice getPairedDevice(HashSet<BLEUtil.BLEDevice> pairedDevices) {
        for (BLEUtil.BLEDevice pairedDevice : pairedDevices) {
            return pairedDevice;
        }
        return null;
    }

    private final BLEUtil.JHBLEListener mBLEListener =  new BLEUtil.JHBLEListener() {
        boolean displayLoading = false;    // 是否需要等待BLE获取服务器完成
        String displayLoadingDeviceName;   // 正在连接设备的名字
        private final ArrayList<BLEUtil.BLEDevice> candidateLeftBLEDeviceArray  = new ArrayList<>(10);   // 候选自动连接的设备
        private final ArrayList<BLEUtil.BLEDevice> candidateRightBLEDeviceArray = new ArrayList<>(10);   // 候选自动连接的设备

        @Override
        public void updateBLEDevice(List<BLEUtil.BLEDevice> bleDevices) {
            if (!visible) { return; }
            mBleDevices = bleDevices;
            Log.d(TAG, "mBluetoothDevices size = " + mBleDevices.size());

            leftConnected   = false;
            rightConnected  = false;
            connectedCnt = 0;

            mScannedDeviceEntities.clear();
            mPairedDeviceEntities.clear();

            candidateLeftBLEDeviceArray.clear();
            candidateRightBLEDeviceArray.clear();
            String leftType  = ""; // 连接的设备类型
            String rightType = ""; // 连接的设备类型

            // 保存配对过的设备
            for(BLEUtil.BLEDevice device : leftPairedDevices) {
                BLEDeviceEntity entity = new BLEDeviceEntity(false);
                entity.section       = 0;
                entity.deviceName    = device.deviceName;
                entity.mac           = device.mac;
                entity.connectStatus = device.connectStatus;
                mPairedDeviceEntities.add(entity);
            }

            for(BLEUtil.BLEDevice device : rightPairedDevices) {
                BLEDeviceEntity entity = new BLEDeviceEntity(false);
                entity.section       = 0;
                entity.deviceName    = device.deviceName;
                entity.mac           = device.mac;
                entity.connectStatus = device.connectStatus;
                mPairedDeviceEntities.add(entity);
            }

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

                    // 如果正在连接，保存相关信息
                    if (device.connectStatus == BLEUtil.STATE_CONNECTING) {
                        displayLoadingDeviceName = device.deviceName;
                        displayLoading = true;

                        mUIHandler.post(() -> IOSLoadingDialog.instance.setOnTouchOutside(false).showDialog(getParentFragmentManager(), ""));
                        mUIHandler.postDelayed(IOSLoadingDialog.instance::dismissDialog, 10000);
                    }

                // 更新配对设备列表, 肯定在配对过的设备里面，因为连接过程中已经记录了。 STATE_CONNECTED, STATE_GET_GATT_SERVICES_OVER
                } else {
                    for (BLEDeviceEntity entity : mPairedDeviceEntities) {
                        if (entity.mac.equals(device.mac)) {
                            entity.connectStatus = device.connectStatus;
                            entity.devType       = device.devType;
                            entity.isScanned     = true;
                        }
                    }

                    // 记录连接成功的设备个数,并读取一次电量
                    if (device.connectStatus == BLEUtil.STATE_GET_GATT_SERVICES_OVER && device.deviceName.contains("-L")) {
                        connectedCnt++;
                        leftConnected = true;
                        if ((!readLeftBat)) {
                            readLeftBat = true;
                            mUIHandler.postDelayed(() -> BLEUtil.getInstance().readBatValue(device.mac), 500);
                        }

                    } else if (device.connectStatus == BLEUtil.STATE_GET_GATT_SERVICES_OVER && device.deviceName.contains("-R")) {
                        connectedCnt++;
                        rightConnected = true;
                        if ((!readRightBat)) {
                            readRightBat = true;
                            mUIHandler.postDelayed(() -> BLEUtil.getInstance().readBatValue(device.mac), 500);
                        }
                    }
                }

                // 自动连接1: 保存扫描到的当前连接的设备 类型
                if (device.connectStatus != BLEUtil.STATE_DISCONNECTED) {
                    if (device.deviceName.contains("-L")) {
                        autoConnectLeft = false;
                        leftType = device.devType;
                    } else if (device.deviceName.contains("-R")) {
                        autoConnectRight = false;
                        rightType = device.devType;
                    }
                }

                // 自动连接2: 添加候选需要连接的设备 -> 在配对过的设备列表里,且扫描到的设备中没连接成功
                boolean isPairedFlag = isPairedDevice(device);
                if (isPairedFlag && device.connectStatus == BLEUtil.STATE_DISCONNECTED && device.deviceName.contains("-L")) {
                    candidateLeftBLEDeviceArray.add(device);
                } else if (isPairedFlag && device.connectStatus == BLEUtil.STATE_DISCONNECTED && device.deviceName.contains("-R")) {
                    candidateRightBLEDeviceArray.add(device);
                }
            }

            // 自动连接3: 在配对的列表里，需要自动连接，连接状态是断开, 左右设备类型相同
            if (autoConnectLeft) {
                for(BLEUtil.BLEDevice bleDevice : candidateLeftBLEDeviceArray) {
                    if (rightType.equals("") || bleDevice.devType.equals(rightType)) {
                        leftType = bleDevice.devType;
                        Log.d(TAG, "AutoConnect L - R type: " + leftType + " - " + rightType);
                        BLEUtil.getInstance().connectBLE(bleDevice.mac);
                        autoConnectLeft = false;
                        readLeftBat = false;
                        break;
                    }
                }
            }

            if (autoConnectRight) {
                for(BLEUtil.BLEDevice bleDevice : candidateRightBLEDeviceArray) {
                    if (leftType.equals("") || bleDevice.devType.equals(leftType)) {
                        rightType = bleDevice.devType;
                        Log.d(TAG, "AutoConnect L - R type: " + leftType + " - " + rightType);
                        BLEUtil.getInstance().connectBLE(bleDevice.mac);
                        autoConnectRight = false;
                        readRightBat = false;
                        break;
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

                int pSize = mPairedDeviceEntities.size();
                BLEDeviceEntity leftPairedDevice = null;
                BLEDeviceEntity rightPairedDevice = null;
                for(BLEDeviceEntity deviceEntity: mPairedDeviceEntities) {
                    if(deviceEntity.deviceName.contains("-L")) {
                        leftPairedDevice = deviceEntity;
                    } else if(deviceEntity.deviceName.contains("-R")) {
                        rightPairedDevice = deviceEntity;
                    }
                }

                if (pSize == 0) {
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
                    binding.tvLeftName.setText(getPairedDevice(leftPairedDevices).deviceName);
                    binding.bvLeftBattery.setVisibility(!leftConnected ? View.GONE : View.VISIBLE);
                    binding.tvLeftBattery.setVisibility(!leftConnected ? View.GONE : View.VISIBLE);
                }
                if (rightPairedDevice != null) {

                }
            });
        }

        @Override
        public void onBatteryChanged(String deviceName, int value) {
            Log.d(TAG, "onBatteryChanged " + deviceName + " " + value);
            if (deviceName == null) { return; }
            requireActivity().runOnUiThread(() -> {
                if (deviceName.contains("-L")) {
//                    binding.bvDeviceLBattery.setBattery((float) value / 100.f);
//                    binding.tvDeviceLBattery.setText("" + value + "%");
//                    if (value < 10) {binding.tvDeviceLBattery.setTextColor(0xFFE22732);}
//                    else if (value < 30) {binding.tvDeviceLBattery.setTextColor(0xFFF06D06);}
//                    else { binding.tvDeviceLBattery.setTextColor(0xFF16DC8F); }

                } else if (deviceName.contains("-R")) {
//                    binding.bvDeviceRBattery.setBattery((float) value / 100.f);
//                    binding.tvDeviceRBattery.setText("" + value + "%");
//                    if (value < 10) {binding.tvDeviceRBattery.setTextColor(0xFFE22732);}
//                    else if (value < 30) {binding.tvDeviceRBattery.setTextColor(0xFFF06D06);}
//                    else { binding.tvDeviceRBattery.setTextColor(0xFF16DC8F); }
                }
            });
        }

        @Override
        public void onReadChanged(String deviceName, byte[] values) {

        }
    };

}