package com.upixels.jh.hearingassist.ui.home;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.upixels.jh.hearingassist.ConnectActivity;
import com.upixels.jh.hearingassist.HearingEntryActivity;
import com.upixels.jh.hearingassist.MainActivity;
import com.upixels.jh.hearingassist.R;
import com.upixels.jh.hearingassist.databinding.FragmentHomeBinding;
import com.upixels.jh.hearingassist.entity.BLEDeviceEntity;
import com.upixels.jh.hearingassist.util.DeviceManager;
import com.upixels.jh.hearingassist.view.JHCommonDialog;

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
    }

    @Override
    public void onStart() {
        Log.d(TAG, "[onStart]");
        super.onStart();
        visible = true;
        DeviceManager.getInstance().readPairedDevice();
    }

    @Override
    public void onResume() {
        Log.d(TAG, "[onResume]");
        super.onResume();
        uiChange();
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
    }

    private void initView() {
        binding.btnSearchDevice.setOnClickListener(v -> {
            startActivity(new Intent(requireContext(), ConnectActivity.class));
        });

        binding.layoutSoundControl.setOnClickListener(v -> {
            boolean leftConnected = DeviceManager.getInstance().getLeftConnected();
            boolean rightConnected = DeviceManager.getInstance().getRightConnected();
            Intent intent = new Intent(requireContext(), MainActivity.class);
            if (!leftConnected && !rightConnected) {
                new JHCommonDialog(getContext(), false)
                        .setTitle(getString(R.string.Note))
                        .setSubTitle(getString(R.string.tips_enter_simulate_mode))
                        .setLeftText(getString(R.string.cancel))
                        .setRightText(getString(R.string.ok))
                        .setOnClickBottomListener(new JHCommonDialog.OnClickBottomListener() {

                            @Override
                            public void onRightClick(Dialog dialog) {
                                dialog.dismiss();
                                intent.putExtra("SimulateMode", true);
                                startActivity(intent);
                            }

                            @Override
                            public void onLeftClick(Dialog dialog) {
                                dialog.dismiss();
                            }
                        }).show();
            } else {
                intent.putExtra("SimulateMode", false);
                startActivity(intent);
            }
        });

        binding.layoutHearingCheck.setOnClickListener( v -> {
            startActivity(new Intent(requireContext(), HearingEntryActivity.class));
        });
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "[onDestroy]");
        super.onDestroy();
    }

    private void uiChange() {
        leftPairedDevice = DeviceManager.getInstance().getLeftPairedDevice();
        rightPairedDevice = DeviceManager.getInstance().getRightPairedDevice();
        leftConnected = DeviceManager.getInstance().getLeftConnected();
        rightConnected = DeviceManager.getInstance().getRightConnected();

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

        int value = DeviceManager.getInstance().getLeftBat();
        binding.bvLeftBattery.setBattery((float) value / 100.f);
        binding.tvLeftBattery.setText("" + value + "%");
        if (value < 10) {binding.tvLeftBattery.setTextColor(0xFFE22732);}
        else if (value < 30) {binding.tvLeftBattery.setTextColor(0xFFF06D06);}
        else { binding.tvLeftBattery.setTextColor(0xFF16DC8F); }

        value = DeviceManager.getInstance().getRightBat();
        binding.bvRightBattery.setBattery((float) value / 100.f);
        binding.tvRightBattery.setText("" + value + "%");
        if (value < 10) {binding.tvRightBattery.setTextColor(0xFFE22732);}
        else if (value < 30) {binding.tvRightBattery.setTextColor(0xFFF06D06);}
        else { binding.tvRightBattery.setTextColor(0xFF16DC8F); }
    }

}