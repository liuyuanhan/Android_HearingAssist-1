package com.upixels.jh.hearingassist.ui.home;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import me.forrest.commonlib.util.BLEUtil;
import me.forrest.commonlib.util.CommonUtil;
import me.forrest.commonlib.util.FileUtil;
import me.forrest.commonlib.util.PermissionUtil;
import me.forrest.commonlib.view.IOSLoadingDialog;

import com.upixels.jh.hearingassist.MyApplication;
import com.upixels.jh.hearingassist.R;
import com.upixels.jh.hearingassist.adapter.SectionQuickAdapter;
import com.upixels.jh.hearingassist.databinding.FragmentConnectBinding;
import com.upixels.jh.hearingassist.entity.BLEDeviceEntity;
import com.upixels.jh.hearingassist.view.DeviceCtlDialog;
import com.upixels.jh.hearingassist.view.LocServiceDialog;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

public class ConnectFragment extends Fragment {
    private final static String TAG = "ConnectFragment";
    private String[] permissions;
    private final static int REQUEST_CODE = 1;
    private FragmentConnectBinding binding;
    private SectionQuickAdapter mAdapter;
    private boolean visible;
    private final Handler mUIHandler = new Handler();

    private List<BLEDeviceEntity> mDeviceList;
    private List<BLEUtil.BLEDevice> mBleDevices;
    private final List<BLEDeviceEntity> mScannedDeviceEntities =  new ArrayList<>(5); // 扫描到的设备
    private final List<BLEDeviceEntity> mPairedDeviceEntities = new ArrayList<>(5);   // 配对过的设备
    private BLEDeviceEntity headerSection0;
    private BLEDeviceEntity headerSection1;
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "[onCreateView]");
        binding = FragmentConnectBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        Log.d(TAG, "[onViewCreated]");
        super.onViewCreated(view, savedInstanceState);
        initView();
//        binding.buttonSecond.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                NavHostFragment.findNavController(ConnectFragment.this)
//                        .navigate(R.id.action_SecondFragment_to_FirstFragment);
//            }
//        });
        // 添加BLEUtil监听回调
        BLEUtil.getInstance().addJHBleListener(mBLEListener);
    }

    @Override
    public void onStart() {
        Log.d(TAG, "[onStart]");
        super.onStart();
        visible = true;
        BLEUtil.getInstance().updateListenerForBLEDevices();
        requestPermissions();
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

    @Override
    public void onDestroy() {
        Log.d(TAG, "[onDestroy]");
        super.onDestroy();
    }

    private void initView() {
        mDeviceList = initData();
        binding.rlvBleDevice.setLayoutManager(new LinearLayoutManager(this.getContext(), LinearLayoutManager.VERTICAL, false));
        mAdapter = new SectionQuickAdapter(R.layout.item_section_content_device, R.layout.item_section_head_device, mDeviceList);
        binding.rlvBleDevice.setAdapter(mAdapter);

        binding.btnSearchDevice.setOnClickListener(v -> {
            if (!PermissionUtil.isLocServiceEnable(requireContext())) {
                new LocServiceDialog(requireContext(), R.layout.dialog_loc_service, false)
                        .setOnClickListener(new LocServiceDialog.OnClickListener() {
                            @Override
                            public void onConfirmClick(LocServiceDialog dialog) {
                                dialog.dismiss();
                                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                startActivity(intent);
                            }

                            @Override
                            public void onCancelClick(LocServiceDialog dialog) {
                                dialog.dismiss();
                            }
                        }).show();
                return;
            }

            if (!PermissionUtil.hasPermissionsGranted(requireActivity(), permissions)) {
                CommonUtil.showToastLong(requireActivity(), getString(R.string.tips_bt_permission_system_settings));
                return;
            }

            boolean isEnable = BLEUtil.getInstance().isEnableBT();
            if (isEnable) {
                autoConnectLeft = true;
                autoConnectRight = true;
                scanCnt++;
                BLEUtil.getInstance().scanLeDevice(true);
                v.setEnabled(false);
                IOSLoadingDialog.instance.dismissDialog();
                IOSLoadingDialog.instance.setOnTouchOutside(false).showDialog(getParentFragmentManager(), getString(R.string.bt_scanning));
                mUIHandler.postDelayed(() -> {
                    if (visible) { IOSLoadingDialog.instance.dismissDialog(); }
                    v.setEnabled(true);
                }, 10500);
            } else {
                CommonUtil.showToastLong(requireActivity(), getString(R.string.tips_open_bt));
            }
        });

        mAdapter.setOnItemChildClickListener((adapter, view, position) -> {
            BLEDeviceEntity entity = mDeviceList.get(position);
            // 找到连接的设备，与要连接的设备进行比较。如果名称一致，不连；如果类型不同，不连。
            for (BLEDeviceEntity deviceEntity : mPairedDeviceEntities) {
                if (!deviceEntity.isHeader() && deviceEntity.connectStatus != BLEUtil.STATE_DISCONNECTED && deviceEntity.deviceName.equals(entity.deviceName)) {
                    return;
                } else if (!deviceEntity.isHeader() && deviceEntity.connectStatus != BLEUtil.STATE_DISCONNECTED) {
                    Log.d(TAG, "Connected Type: " + deviceEntity.devType + ", Unconnected Type: " + entity.devType);
                    if (!entity.isScanned) {
                        CommonUtil.showToastShort(requireActivity(), getString(R.string.tips_not_scanned_device));
                        return;
                    }
                    if (!deviceEntity.devType.equals(entity.devType)) {
                        CommonUtil.showToastShort(requireActivity(), getString(R.string.tips_connect_same_type));
                        return;
                    }
                }
            }
            BLEUtil.getInstance().connectBLE(entity.mac);
        });

        binding.layoutDeviceL.setOnClickListener(v -> {
            DeviceCtlDialog dialog = new DeviceCtlDialog(requireContext(), R.layout.dialog_device_ctl, true);
            dialog.show();
        });
    }

    private List<BLEDeviceEntity> initData() {
        List<BLEDeviceEntity> list = new LinkedList<>();
        headerSection0 = new BLEDeviceEntity(true);
        headerSection0.section = 0;
        headerSection0.mac = "";
        headerSection0.header = getResources().getString(R.string.search_for_a_device);
        list.add(headerSection0);

//        headerSection1 = new BLEDeviceEntity(true);
//        headerSection1.section = 1;
//        headerSection1.mac = "";
//        headerSection1.header = getResources().getString(R.string.other_device);
//        list.add(headerSection1);

        // 获取配对过的设备
        leftPairedDevices = ((MyApplication)requireActivity().getApplication()).getLeftPairedDevices();
        rightPairedDevices = ((MyApplication)requireActivity().getApplication()).getRightPairedDevices();
        FileUtil.readPairedDevices(requireContext(), "Left", leftPairedDevices);
        FileUtil.readPairedDevices(requireContext(), "Right", rightPairedDevices);
        return list;
    }

    private void requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissions = new String[] {
                    //                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.BLUETOOTH,
                    Manifest.permission.BLUETOOTH_ADMIN,
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION };
        } else {
            permissions = new String[] {
                    //                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.BLUETOOTH,
                    Manifest.permission.BLUETOOTH_ADMIN,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION };
        }

        if (!PermissionUtil.isLocServiceEnable(requireContext())) {
            new LocServiceDialog(requireContext(), R.layout.dialog_loc_service, false)
                    .setOnClickListener(new LocServiceDialog.OnClickListener() {
                        @Override
                        public void onConfirmClick(LocServiceDialog dialog) {
                            dialog.dismiss();
                            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivity(intent);
                        }

                        @Override
                        public void onCancelClick(LocServiceDialog dialog) {
                            dialog.dismiss();
                        }
                    }).show();
            return;
        }

        if (!PermissionUtil.hasPermissionsGranted(requireActivity(), permissions)) {
            PermissionUtil.requestPermission(this, permissions, REQUEST_CODE, getString(R.string.bluetooth_permission));
        } else {
//            openBT();
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(TAG, "requestCode=" + requestCode + ",permissions.length=" + permissions.length);
        if (requestCode == REQUEST_CODE) {
            for (int index = 0 ; index < permissions.length; index++) {
                if (grantResults[index] != PackageManager.PERMISSION_GRANTED) {
                    CommonUtil.showToastLong(requireActivity(), getString(R.string.tips_bluetooth_permission));
//                    finish();
                }
//                // android 9.0 获取了该权限，即可获取ssid
//                if (permissions[index].equals(Manifest.permission.ACCESS_FINE_LOCATION)) {
//                    doAfterGetSSID();
//                }
            }
//            openBT();
        }
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

            connectedCnt = 0;
            // 先加头节点
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

                        if (device.deviceName.contains("-L")) {
                            // 重新保存配对过的设备信息
                            leftPairedDevices.clear();
                            leftPairedDevices.add(new BLEUtil.BLEDevice(device.deviceName, device.mac, 0, device.devType));
                            FileUtil.writePairedDevices(requireContext(), "Left", leftPairedDevices);

                            //int oldSize = leftPairedDevices.size();
                            //leftPairedDevices.add(new BLEUtil.BLEDevice(device.deviceName, device.mac, 0, device.devType));
                            //if (leftPairedDevices.size() > oldSize) {
                            //    FileUtil.writePairedDevices(requireContext(), "Left", leftPairedDevices);
                            //}

                        } else if (device.deviceName.contains("-R")) {
                            // 重新保存配对过的设备信息
                            rightPairedDevices.clear();
                            rightPairedDevices.add(new BLEUtil.BLEDevice(device.deviceName, device.mac, 0, device.devType));
                            FileUtil.writePairedDevices(requireContext(), "Right", rightPairedDevices);

                            //int oldSize = rightPairedDevices.size();
                            //rightPairedDevices.add(new BLEUtil.BLEDevice(device.deviceName, device.mac, 0, device.devType));
                            //if (rightPairedDevices.size() > oldSize) {
                            //    FileUtil.writePairedDevices(requireContext(), "Right", rightPairedDevices);
                            //}
                        }

                    }
//                    else if (device.connectStatus == BLEUtil.STATE_GET_GATT_SERVICES_OVER && device.deviceName.equals(displayLoadingDeviceName)) {
//                        displayLoading = false;
//                        if (!BLEUtil.getInstance().isScanning()) { mUIHandler.post(IOSLoadingDialog.instance::dismissDialog); }
//                    }

                } else { // 更新配对设备列表, 肯定在配对过的设备里面，因为连接过程中已经记录了。
                    for (BLEDeviceEntity entity : mPairedDeviceEntities) {
                        if (entity.mac.equals(device.mac)) {
                            entity.connectStatus = device.connectStatus;
                            entity.devType       = device.devType;
                            entity.isScanned     = true;
                        }
                    }
                    // 记录连接成功的设备个数
                    if (device.connectStatus == BLEUtil.STATE_GET_GATT_SERVICES_OVER) {
                        connectedCnt++;
                    }

//                    BLEDeviceEntity section = new BLEDeviceEntity(false);
//                    section.section       = 1;
//                    section.deviceName    = device.deviceName;
//                    section.mac           = device.mac;
//                    section.connectStatus = device.connectStatus;
//                    section.devType       = device.devType;
//                    section.isScanned     = true;
//                    mPairedDeviceEntities.add(section);
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
                        break;
                    }
                }
            }

            mDeviceList.clear();
            mDeviceList.add(headerSection0);
            mDeviceList.addAll(mScannedDeviceEntities);
            mUIHandler.post(() -> mAdapter.notifyDataSetChanged());

            // 更新 底部我的设备部分的UI
            mUIHandler.post(() -> {
                if (scanCnt == 1 && connectedCnt == 2) {
                    BLEUtil.getInstance().scanLeDevice(false);
                    IOSLoadingDialog.instance.dismissDialog();
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
                    binding.layoutMyDevice.setVisibility(View.GONE);
                    binding.btnPersonalize.setVisibility(View.GONE);
                } else {
                    binding.layoutMyDevice.setVisibility(View.VISIBLE);
                    binding.layoutDeviceL.setVisibility(leftPairedDevice != null ? View.VISIBLE : View.GONE);
                    binding.layoutDeviceR.setVisibility(rightPairedDevice != null ? View.VISIBLE : View.GONE);
                    binding.btnPersonalize.setVisibility(View.VISIBLE);
                }

                if (leftPairedDevice != null) {
                    binding.ivDeviceLStatus.setImageResource(leftPairedDevice.connectStatus == BLEUtil.STATE_CONNECTED
                            || leftPairedDevice.connectStatus == BLEUtil.STATE_GET_GATT_SERVICES_OVER
                            ? R.drawable.icon_device_connected
                            : R.drawable.icon_device_disconnected);
                }
                if (rightPairedDevice != null) {
                    binding.ivDeviceRStatus.setImageResource(rightPairedDevice.connectStatus == BLEUtil.STATE_CONNECTED
                            || rightPairedDevice.connectStatus == BLEUtil.STATE_GET_GATT_SERVICES_OVER
                            ? R.drawable.icon_device_connected
                            : R.drawable.icon_device_disconnected);
                }
            });
        }

        @Override
        public void onBatteryChanged(String deviceName, int value) {

        }

        @Override
        public void onReadChanged(String deviceName, byte[] values) {

        }
    };
}