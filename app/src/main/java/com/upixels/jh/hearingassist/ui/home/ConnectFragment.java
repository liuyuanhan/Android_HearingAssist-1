package com.upixels.jh.hearingassist.ui.home;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.text.TextUtils;
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


import com.upixels.jh.hearingassist.R;
import com.upixels.jh.hearingassist.adapter.SectionQuickAdapter;
import com.upixels.jh.hearingassist.databinding.FragmentConnectBinding;
import com.upixels.jh.hearingassist.entity.BLEDeviceEntity;
import com.upixels.jh.hearingassist.util.DeviceManager;
import com.upixels.jh.hearingassist.view.DeviceCtlDialog;
import com.upixels.jh.hearingassist.view.JHCommonDialog;
import com.upixels.jh.hearingassist.view.LocServiceDialog;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

public class ConnectFragment extends Fragment {
    private final static                String TAG = "ConnectFragment";
    private String[]                    permissions;
    private final static int            REQUEST_CODE = 1;
    private FragmentConnectBinding      binding;
    private SectionQuickAdapter         mAdapter;
    private boolean                     visible;
    private final Handler               mUIHandler = new Handler();
    private Runnable                    dismissDialogRunnable;

    private boolean                     leftConnected;
    private boolean                     rightConnected;
    private final List<BLEDeviceEntity> entities = new LinkedList<>();
    private List<BLEUtil.BLEDevice>     mBleDevices;
    private final List<BLEDeviceEntity> mScannedDeviceEntities =  new ArrayList<>(5); // 扫描到的设备
    private BLEUtil.BLEDevice           leftPairedDevice;
    private BLEUtil.BLEDevice           rightPairedDevice;
    private boolean                     autoConnectLeft;  // 是否需要主动连接左耳
    private boolean                     autoConnectRight; // 是否需要主动连接右耳
    private int                         connectedCnt = 0;
    private int                         scanCnt = 0;   // 记录第几次发起扫描，第一次扫描时为了快速连接设备，扫描到两个设备，并连接上了就不扫了。重新发起扫描时，扫描10S，

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        Log.d(TAG, "[onCreate]");
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "[onCreateView]");
        binding = FragmentConnectBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        Log.d(TAG, "[onViewCreated]");
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
        // 获取配对过的设备
        leftPairedDevice = DeviceManager.getInstance().getLeftPairedDevice();
        rightPairedDevice = DeviceManager.getInstance().getRightPairedDevice();
        Log.d(TAG, "leftPairedDevice: " + leftPairedDevice);
        Log.d(TAG, "rightPairedDevice:" + rightPairedDevice);
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
        //entities = null;//initData();
        binding.rlvBleDevice.setLayoutManager(new LinearLayoutManager(this.getContext(), LinearLayoutManager.VERTICAL, false));
        mAdapter = new SectionQuickAdapter(R.layout.item_section_content_device, R.layout.item_section_head_device, entities);
        View headView = getLayoutInflater().inflate(R.layout.item_section_head_device, binding.rlvBleDevice, false);
        mAdapter.addHeaderView(headView);
        View footView = getLayoutInflater().inflate(R.layout.item_section_foot_device, binding.rlvBleDevice, false);
        mAdapter.addFooterView(footView);
        mAdapter.setHeaderWithEmptyEnable(true);
        mAdapter.setFooterWithEmptyEnable(true);
        binding.rlvBleDevice.setAdapter(mAdapter);
        mAdapter.setEmptyView(R.layout.item_section_no_device);

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
                if (connectedCnt == 2) scanCnt++; // 如果第一次进入页面，已经连上了2个设备，就认为扫描过一次，防止第一下点击时，弹窗消失。
                BLEUtil.getInstance().scanLeDevice(true);
                v.setEnabled(false);
                IOSLoadingDialog.instance.dismissDialog();
                IOSLoadingDialog.instance.setOnTouchOutside(false).showDialog(getParentFragmentManager(), getString(R.string.bt_scanning));
                dismissDialogRunnable = () -> {
                    if (visible) { IOSLoadingDialog.instance.dismissDialog(); }
                    v.setEnabled(true);
                };
                mUIHandler.postDelayed(dismissDialogRunnable, 10500);
            } else {
                CommonUtil.showToastLong(requireActivity(), getString(R.string.tips_open_bt));
            }
        });

        mAdapter.setOnItemChildClickListener((adapter, view, position) -> {
            BLEDeviceEntity entity = entities.get(position);
            // 找到连接的设备，与要连接的设备进行比较。如果名称一致，不连；如果类型不同，不连。
            if (entity.deviceName.contains("-L")) {
                if (leftConnected) { return; }
                if (rightConnected && !rightPairedDevice.devType.equals(entity.devType)) {
                    CommonUtil.showToastShort(requireActivity(), getString(R.string.tips_connect_same_type));
                    return;
                }
            } else if (entity.deviceName.contains("-R")) {
                if (rightConnected) { return; }
                if (leftConnected && !leftPairedDevice.devType.equals(entity.devType)) {
                    CommonUtil.showToastShort(requireActivity(), getString(R.string.tips_connect_same_type));
                    return;
                }
            }
            BLEUtil.getInstance().connectBLE(entity.mac);
        });

        binding.layoutDeviceL.setOnClickListener(v -> {
            final BLEUtil.BLEDevice device = leftPairedDevice;
            if (device == null) { return; }
            DeviceCtlDialog dialog = new DeviceCtlDialog(requireContext(), R.layout.dialog_device_ctl, false);
            dialog.setTitle(String.format("Left Ear: %s(%s)", device.getShowName(), device.getLast4CharMac()));
            dialog.setOnClickListener(new DeviceCtlDialog.OnClickListener() {
                @Override
                public void onDisconnectClick(DeviceCtlDialog dialog) {
                    BLEUtil.getInstance().disconnectBLE(device.mac);
                    dialog.dismiss();
                }

                @Override
                public void onRenameClick(DeviceCtlDialog dialog) {
                    dialog.dismiss();
                    showRenameDeviceDialog(DeviceManager.EAR_TYPE_LEFT, leftPairedDevice.getShowName());
                }

                @Override
                public void onRemoveClick(DeviceCtlDialog dialog) {
                    dialog.dismiss();
                    showRemoveDeviceDialog(DeviceManager.EAR_TYPE_LEFT, String.format("%s(%s)", device.getShowName(), device.getLast4CharMac()), device.mac);
                }

                @Override
                public void onCancelClick(DeviceCtlDialog dialog) {
                    dialog.dismiss();
                }
            });
            dialog.show();
        });

        binding.layoutDeviceR.setOnClickListener(v -> {
            final BLEUtil.BLEDevice device = rightPairedDevice;
            if (device == null) { return; }
            DeviceCtlDialog dialog = new DeviceCtlDialog(requireContext(), R.layout.dialog_device_ctl, false);
            dialog.setTitle(String.format("Right Ear: %s(%s)", device.getShowName(), device.getLast4CharMac()));
            dialog.setOnClickListener(new DeviceCtlDialog.OnClickListener() {
                @Override
                public void onDisconnectClick(DeviceCtlDialog dialog) {
                    BLEUtil.getInstance().disconnectBLE(device.mac);
                    dialog.dismiss();
                }

                @Override
                public void onRenameClick(DeviceCtlDialog dialog) {
                    dialog.dismiss();
                    showRenameDeviceDialog(DeviceManager.EAR_TYPE_RIGHT, rightPairedDevice.getShowName());
                }

                @Override
                public void onRemoveClick(DeviceCtlDialog dialog) {
                    dialog.dismiss();
                    showRemoveDeviceDialog(DeviceManager.EAR_TYPE_RIGHT, String.format("%s(%s)", device.getShowName(), device.getLast4CharMac()), device.mac);
                }

                @Override
                public void onCancelClick(DeviceCtlDialog dialog) {
                    dialog.dismiss();
                }
            });
            dialog.show();
        });

        binding.btnPersonalize.setOnClickListener(v -> requireActivity().finish());
    }

    private void showRenameDeviceDialog(String earType, String message) {
        new JHCommonDialog(getContext(), R.layout.dialog_rename_device, false)
                .setTitle(getString(R.string.rename_device))
                .setMessage(message)
                .setLeftText(getString(R.string.cancel))
                .setRightText(getString(R.string.Yes))
                .setOnClickBottomListener(new JHCommonDialog.OnClickBottomListener() {

                    @Override
                    public void onRightClick(Dialog dialog) {
                        String alias = ((JHCommonDialog)dialog).getEditText().trim();
                        Log.d(TAG, "alias = " + alias);
                        if (TextUtils.isEmpty(alias)) {
                            CommonUtil.showToastShort(requireActivity(), getString(R.string.tips_input_right_name));
                            return;
                        }
                        dialog.dismiss();
                        if (earType.equals(DeviceManager.EAR_TYPE_LEFT)) {
                            leftPairedDevice.alias = alias;
                            FileUtil.writePairedDevice(requireContext(), "Left", leftPairedDevice);
                            binding.tvDeviceLName.setText(String.format(Locale.getDefault(), "%s(%s)", leftPairedDevice.getShowName(),
                                                                                                              leftPairedDevice.getLast4CharMac()));

                        } else if (earType.equals(DeviceManager.EAR_TYPE_RIGHT)) {
                            rightPairedDevice.alias = alias;
                            FileUtil.writePairedDevice(requireContext(), "Right",rightPairedDevice);
                            binding.tvDeviceRName.setText(String.format(Locale.getDefault(), "%s(%s)", rightPairedDevice.getShowName(),
                                                                                                              rightPairedDevice.getLast4CharMac()));
                        }
                        CommonUtil.showToastShort(requireActivity(), getString(R.string.tips_rename_ok));
                    }

                    @Override
                    public void onLeftClick(Dialog dialog) {
                        dialog.dismiss();
                    }
                }).show();
    }

    private void showRemoveDeviceDialog(String earType, String message, String mac) {
        new JHCommonDialog(getContext(), false)
                .setTitle(getString(R.string.remove_device))
                .setSubTitle(getString(R.string.tips_remove_device))
                .setMessage(message)
                .setLeftText(getString(R.string.cancel))
                .setRightText(getString(R.string.Yes))
                .setOnClickBottomListener(new JHCommonDialog.OnClickBottomListener() {

                    @Override
                    public void onRightClick(Dialog dialog) {
                        dialog.dismiss();
                        if (earType.equals(DeviceManager.EAR_TYPE_LEFT)) {
                            leftPairedDevice = null;
                            FileUtil.writePairedDevice(requireContext(), "Left", null);
                            DeviceManager.getInstance().setLeftPairedDevice(null);
                            binding.layoutDeviceL.setVisibility(View.GONE);
                        } else if (earType.equals(DeviceManager.EAR_TYPE_RIGHT)) {
                            rightPairedDevice = null;
                            FileUtil.writePairedDevice(requireContext(), "Right",null);
                            DeviceManager.getInstance().setRightPairedDevice(null);
                            binding.layoutDeviceR.setVisibility(View.GONE);
                        }
                        if (leftPairedDevice == null && rightPairedDevice == null) {
                            binding.layoutMyDevice.setVisibility(View.GONE);
                            binding.btnPersonalize.setVisibility(View.GONE);
                        }
                        BLEUtil.getInstance().disconnectBLE(mac);
                    }

                    @Override
                    public void onLeftClick(Dialog dialog) {
                        dialog.dismiss();
                    }
                }).show();
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

            String leftType  = ""; // 连接的设备类型
            String rightType = ""; // 连接的设备类型


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

                        if (dismissDialogRunnable != null) { mUIHandler.removeCallbacks( dismissDialogRunnable); }
                        mUIHandler.post(() -> IOSLoadingDialog.instance.setOnTouchOutside(false).showDialog(getParentFragmentManager(), ""));
                        dismissDialogRunnable = () -> {
                            binding.btnSearchDevice.setEnabled(true);
                            if (visible) { IOSLoadingDialog.instance.dismissDialog(); }
                        };
                        mUIHandler.postDelayed(dismissDialogRunnable, 10000);

                        if (device.deviceName.contains("-L")) {
                            // 重新保存配对过的设备信息
                            if (leftPairedDevice == null || !leftPairedDevice.mac.equals(device.mac)) {
                                leftPairedDevice = new BLEUtil.BLEDevice(device.deviceName, device.mac, 0, device.devType);
                                DeviceManager.getInstance().setLeftPairedDevice(leftPairedDevice);
                                FileUtil.writePairedDevice(requireContext(), "Left", leftPairedDevice);
                            }

                        } else if (device.deviceName.contains("-R")) {
                            // 重新保存配对过的设备信息
                            if (rightPairedDevice == null || !rightPairedDevice.mac.equals(device.mac)) {
                                rightPairedDevice = new BLEUtil.BLEDevice(device.deviceName, device.mac, 0, device.devType);
                                DeviceManager.getInstance().setRightPairedDevice(rightPairedDevice);
                                FileUtil.writePairedDevice(requireContext(), "Right", rightPairedDevice);
                            }
                        }
                    }

                } else { // 更新配对设备列表, 肯定在配对过的设备里面，因为连接过程中已经记录了。 STATE_CONNECTED, STATE_GET_GATT_SERVICES_OVER

                    // 记录连接成功的设备个数
                    if (device.connectStatus == BLEUtil.STATE_GET_GATT_SERVICES_OVER && device.deviceName.contains("-L")) {
                        connectedCnt++;
                        leftConnected = true;

                    } else if (device.connectStatus == BLEUtil.STATE_GET_GATT_SERVICES_OVER && device.deviceName.contains("-R")) {
                        connectedCnt++;
                        rightConnected = true;
                    }
                }
            }

            // 自动连接: 在扫描到的设备列表里面，连接与配对过的设备地址一样的设备
            if (autoConnectLeft && !leftConnected && leftPairedDevice != null) {
                if (hasPairedDevice(leftPairedDevice, mBleDevices)) {
                    Log.d(TAG, "Auto Connect Left Device");
                    autoConnectLeft = false;
                    mUIHandler.postDelayed(() -> BLEUtil.getInstance().connectBLE(leftPairedDevice.mac), 100);
                }
            }

            if (autoConnectRight && !rightConnected && rightPairedDevice != null) {
                if (hasPairedDevice(rightPairedDevice, mBleDevices)) {
                    Log.d(TAG, "Auto Connect Right Device");
                    autoConnectRight = false;
                    mUIHandler.postDelayed(() -> BLEUtil.getInstance().connectBLE(rightPairedDevice.mac), 100);
                }
            }

            entities.clear();
            entities.addAll(mScannedDeviceEntities);
            mUIHandler.post(() -> mAdapter.notifyDataSetChanged());

            // 更新 底部我的设备部分的UI
            mUIHandler.post(() -> {
                // 第一次扫描时，为了快速可以交互，连接2个设备后，立即停止扫描。
                if (scanCnt == 1 && connectedCnt == 2) {
                    mUIHandler.removeCallbacks(dismissDialogRunnable);
                    BLEUtil.getInstance().scanLeDevice(false);
                    IOSLoadingDialog.instance.dismissDialog();
                    binding.btnSearchDevice.setEnabled(true);
                }

                if (leftPairedDevice == null && rightPairedDevice == null) {
                    binding.layoutMyDevice.setVisibility(View.GONE);
                    binding.btnPersonalize.setVisibility(View.GONE);
                } else {
                    binding.layoutMyDevice.setVisibility(View.VISIBLE);
                    binding.layoutDeviceL.setVisibility(leftPairedDevice != null ? View.VISIBLE : View.GONE);
                    binding.layoutDeviceR.setVisibility(rightPairedDevice != null ? View.VISIBLE : View.GONE);
                    binding.btnPersonalize.setVisibility(View.VISIBLE);
                }

                if (leftPairedDevice != null) {
                    binding.layoutDeviceL.setAlpha(leftConnected ? 1.0f : 0.5f);
                    binding.ivDeviceLStatus.setImageResource(leftConnected ? R.drawable.icon_device_connected : R.drawable.icon_device_disconnected);
                    binding.tvDeviceLName.setText(String.format(Locale.getDefault(), "%s(%s)", leftPairedDevice.getShowName(),
                                                                                                      leftPairedDevice.getLast4CharMac()));
                    int value = DeviceManager.getInstance().getLeftBat();
                    binding.bvDeviceLBattery.setBattery((float) value / 100.f);
                    binding.tvDeviceLBattery.setText(String.format(Locale.getDefault(),"%d%%",value));
                    if (value < 10) {binding.tvDeviceLBattery.setTextColor(0xFFE22732);}
                    else if (value < 30) {binding.tvDeviceLBattery.setTextColor(0xFFF06D06);}
                    else { binding.tvDeviceLBattery.setTextColor(0xFF16DC8F); }
                }

                if (rightPairedDevice != null) {
                    binding.layoutDeviceR.setAlpha(rightConnected ? 1.0f : 0.5f);
                    binding.ivDeviceRStatus.setImageResource(rightConnected ? R.drawable.icon_device_connected : R.drawable.icon_device_disconnected);
                    binding.tvDeviceRName.setText(String.format(Locale.getDefault(), "%s(%s)", rightPairedDevice.getShowName(),
                                                                                                      rightPairedDevice.getLast4CharMac()));
                    int value = DeviceManager.getInstance().getRightBat();
                    binding.bvDeviceRBattery.setBattery((float) value / 100.f);
                    binding.tvDeviceRBattery.setText(String.format(Locale.getDefault(),"%d%%",value));
                    if (value < 10) {binding.tvDeviceRBattery.setTextColor(0xFFE22732);}
                    else if (value < 30) {binding.tvDeviceRBattery.setTextColor(0xFFF06D06);}
                    else { binding.tvDeviceRBattery.setTextColor(0xFF16DC8F); }
                }
            });
        }

        @Override
        public void onBatteryChanged(String deviceName, int value) {
            Log.d(TAG, "onBatteryChanged " + deviceName + " " + value);
            if (deviceName == null) { return; }
            requireActivity().runOnUiThread(() -> {
                if (deviceName.contains("-L")) {
                    binding.bvDeviceLBattery.setBattery((float) value / 100.f);
                    binding.tvDeviceLBattery.setText(String.format(Locale.getDefault(),"%d%%",value));
                    if (value < 10) {binding.tvDeviceLBattery.setTextColor(0xFFE22732);}
                    else if (value < 30) {binding.tvDeviceLBattery.setTextColor(0xFFF06D06);}
                    else { binding.tvDeviceLBattery.setTextColor(0xFF16DC8F); }

                } else if (deviceName.contains("-R")) {
                    binding.bvDeviceRBattery.setBattery((float) value / 100.f);
                    binding.tvDeviceRBattery.setText(String.format(Locale.getDefault(),"%d%%",value));
                    if (value < 10) {binding.tvDeviceRBattery.setTextColor(0xFFE22732);}
                    else if (value < 30) {binding.tvDeviceRBattery.setTextColor(0xFFF06D06);}
                    else { binding.tvDeviceRBattery.setTextColor(0xFF16DC8F); }
                }
            });
        }

        @Override
        public void onReadChanged(String deviceName, byte[] values) {

        }
    };

}