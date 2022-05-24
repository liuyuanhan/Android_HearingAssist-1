package com.upixels.jh.hearingassist;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.tabs.TabLayout;


import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.Disposable;
import me.forrest.commonlib.jh.AIDMode;
import me.forrest.commonlib.jh.BTProtocol;
import me.forrest.commonlib.util.BLEUtil;
import me.forrest.commonlib.util.CommonUtil;
import me.forrest.commonlib.view.IOSLoadingDialog;

import android.os.Handler;
import android.util.Log;

import com.google.android.material.tabs.TabLayoutMediator;
import com.upixels.jh.hearingassist.ui.main.SectionsPagerAdapter;
import com.upixels.jh.hearingassist.databinding.ActivityMainBinding;
import com.upixels.jh.hearingassist.util.DeviceManager;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String         TAG =           MainActivity.class.getSimpleName();
    private ActivityMainBinding         binding;
    private Handler                     uiHandler;
    private boolean                     isSimulateModeFlag;
    private String                      leftDevType;
    private String                      rightDevType;
    private boolean                     leftConnected;
    private boolean                     rightConnected;
    private boolean                     leftConnecting;        //
    private boolean                     rightConnecting;
    private int                         connectedCnt;
    private AIDMode                     leftMode;
    private AIDMode                     rightMode;
    private int                         modeCnt;            //获取到的模式数量
    private int                         readModeFileCnt;
    private int                         modeFileCnt;        //获取到的模式文件数量
    private int                         settingCnt;
    private String                      leftMac;
    private String                      rightMac;
    private BTProtocol.ModeFileContent  leftModeFileContent;
    private BTProtocol.ModeFileContent  rightModeFileContent;
    private BTProtocol.ModeFileContent  mutableLeftModeFileContent;
    private BTProtocol.ModeFileContent  mutableRightModeFileContent;


    private int     statusChange = 1;  // 用于断开连接变化标记，防止重复设置UI 1: 表示有变化 0: 表示无变化
    private int     delay        = 50; // 用于重连时延时获取数据
    private Disposable                  disposable0;
    private Disposable                  ctlFeedbackDisposable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "[onCreate]");
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Intent intent = getIntent();
        isSimulateModeFlag = intent.getBooleanExtra("SimulateMode", false);

        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(this);
        ViewPager2 viewPager = binding.viewPager;
        viewPager.setAdapter(sectionsPagerAdapter);
        viewPager.setCurrentItem(1);
        TabLayout tabLayout = binding.tabLayout;

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            if (position == 0) {
                tab.setText(R.string.tab_text_Mode);
                tab.setIcon(R.drawable.tab_icon_mode);
            } else if (position == 1) {
                tab.setText(R.string.tab_text_Volume);
                tab.setIcon(R.drawable.tab_icon_volume);
            } else if (position == 2) {
                tab.setText(R.string.tab_text_Band);
                tab.setIcon(R.drawable.tab_icon_band);
            } else if (position == 3) {
                tab.setText(R.string.tab_text_Loud);
                tab.setIcon(R.drawable.tab_icon_loud);
            } else if (position == 4) {
                tab.setText(R.string.tab_text_Focus);
                tab.setIcon(R.drawable.tab_icon_focus);
            } else if (position == 5) {
                tab.setText(R.string.tab_text_Noise);
                tab.setIcon(R.drawable.tab_icon_noise);
            }
        }).attach();

        binding.ivBack.setOnClickListener(v -> finish());

        uiHandler = new Handler(getMainLooper());
    }

    @Override
    protected void onStart() {
        Log.d(TAG, "[onStart]");
        super.onStart();
        if (!isSimulateModeFlag) {
            DeviceManager.getInstance().addListener(deviceChangeListener);
            DeviceManager.getInstance().readModeVolume(true);    // 只有在进入Sound Control时才会去读一次模式
        }
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "[onResume]");
        super.onResume();
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "[onPause]");
        super.onPause();
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "[onStop]");
        super.onStop();
        if (!isSimulateModeFlag) {
            DeviceManager.getInstance().removeListener(deviceChangeListener);
        }
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "[onDestroy]");
        super.onDestroy();
    }

    public boolean isSimulateMode() {
        return this.isSimulateModeFlag;
    }

    // 创建模拟AIDMode
    private AIDMode sLeftMode;
    private AIDMode sRightMode;
    public synchronized AIDMode getSimulateLeftMode() {
        if (sLeftMode == null) {
            sLeftMode = new AIDMode("HA-STREAM-L", (byte)1, (byte)7, BTProtocol.Read_Success);
        }
        return sLeftMode;
    }

    public synchronized AIDMode getSimulateRightMode() {
        if (sRightMode == null) {
            sRightMode = new AIDMode("HA-STREAM-R", (byte)1, (byte)7, BTProtocol.Read_Success);
        }
        return sRightMode;
    }

    // 创建模拟ModeFileContent
    private BTProtocol.ModeFileContent leftContent;
    private BTProtocol.ModeFileContent rightContent;
    public synchronized BTProtocol.ModeFileContent getSimulateLeftModeFileContent() {
        if (leftContent == null) {
            leftContent = new BTProtocol.ModeFileContent();
            leftContent.aidMode = getSimulateLeftMode();
            leftContent.INPUT = 1;
            leftContent.NC2   = 0;
            leftContent.NC3   = 0;
            leftContent.NC4   = 0;
            leftContent.PG1   = 4;  // 前置增益MIC1
            leftContent.PG2   = 4;  // 前置增益MIC2
            leftContent.EQ1   = 7;  // 250
            leftContent.EQ2   = 7;  // 500
            leftContent.EQ3   = 7;  // 1000
            leftContent.EQ4   = 7;  // 1500
            leftContent.EQ5   = 7;  // 2000
            leftContent.EQ6   = 7;  // 2500
            leftContent.EQ7   = 7;  // 3000
            leftContent.EQ8   = 7;  // 3500
            leftContent.EQ9   = 7;  // 4000
            leftContent.EQ10  = 7; // 5000
            leftContent.EQ11  = 7; // 6000
            leftContent.EQ12  = 7; // 7000
            leftContent.CR1   = 0;
            leftContent.CR2   = 0;
            leftContent.CR3   = 0;
            leftContent.CR4   = 0;
            leftContent.EXPAN = 0; // MIC 扩展
            leftContent.CT    = 0;    // 压缩阈值
            leftContent.MPO   = 0;
            leftContent.NR    = 0;
        }
        return leftContent;
    }

    public synchronized BTProtocol.ModeFileContent getSimulateRightModeFileContent() {
        if (rightContent == null) {
            rightContent = new BTProtocol.ModeFileContent();
            rightContent.aidMode = getSimulateRightMode();
            rightContent.INPUT = 1;
            rightContent.NC2   = 0;
            rightContent.NC3   = 0;
            rightContent.NC4   = 0 ;
            rightContent.PG1   = 4;  // 前置增益MIC1
            rightContent.PG2   = 4;  // 前置增益MIC2
            rightContent.EQ1   = 7;  // 250
            rightContent.EQ2   = 7;  // 500
            rightContent.EQ3   = 7;  // 1000
            rightContent.EQ4   = 7;  // 1500
            rightContent.EQ5   = 7;  // 2000
            rightContent.EQ6   = 7;  // 2500
            rightContent.EQ7   = 7;  // 3000
            rightContent.EQ8   = 7;  // 3500
            rightContent.EQ9   = 7;  // 4000
            rightContent.EQ10  = 7; // 5000
            rightContent.EQ11  = 7; // 6000
            rightContent.EQ12  = 7; // 7000
            rightContent.CR1   = 0;
            rightContent.CR2   = 0;
            rightContent.CR3   = 0;
            rightContent.CR4   = 0;
            rightContent.EXPAN = 0; // MIC 扩展
            rightContent.CT    = 0;    // 压缩阈值
            rightContent.MPO   = 0;
            rightContent.NR    = 0;
        }
        return rightContent;
    }

    private final Runnable dismissLoadingDialogRunnable = IOSLoadingDialog.instance::dismissDialog;

    private final DeviceManager.DeviceChangeListener deviceChangeListener = new DeviceManager.DeviceChangeListener() {
        boolean isShowing = false;

        @Override
        public void onConnectStatus(boolean leftConnected, boolean rightConnected) {
            if (!leftConnected && !rightConnected) {
                finish();
            }
        }

        @Override
        public void onReadingStatus(boolean isReading) {
            Log.d(TAG, "onReadingStatus " + isReading);
            if (!isShowing && isReading) {
                isShowing = true;
                IOSLoadingDialog.instance.setOnTouchOutside(false).showDialog(getSupportFragmentManager(), "");
                uiHandler.postDelayed(dismissLoadingDialogRunnable, 10000);
            } else if (isShowing && !isReading) {
                isShowing = false;
                uiHandler.removeCallbacks(dismissLoadingDialogRunnable);
                IOSLoadingDialog.instance.dismissDialog();
            }
        }

        @Override
        public void onChangeBat(int leftBat, int rightBat) {

        }

        @Override
        public void onChangeSceneMode(AIDMode leftMode, AIDMode rightMode) {
        }

        @Override
        public void onChangeModeFile(BTProtocol.ModeFileContent leftContent, BTProtocol.ModeFileContent rightContent) {

        }

        @Override
        public void onCtlFeedback(String leftResult, String rightResult) {

        }

        @Override
        public void onWriteFeedback(String leftResult, String rightResult) {

        }
    };

    private final BLEUtil.JHBLEListener mBLEListener =  new BLEUtil.JHBLEListener() {

        @Override
        public void updateBLEDevice(List<BLEUtil.BLEDevice> bleDevices) {
            initStatus();
        }

        @Override
        public void onBatteryChanged(String deviceName, int value) {

        }

        @Override
        public void onReadChanged(String deviceName, byte[] values) {

        }
    };

    // 同步模式 mode:上报的模式 deviceName:需要去设置的模式
    private void syncMode(AIDMode mode, final String deviceName) {
        if(deviceName.contains("-L") && !leftConnected) { return; }
        if(deviceName.contains("-R") && !rightConnected) { return; }
        IOSLoadingDialog.instance.dismissDialog();

        IOSLoadingDialog.instance.showDialog(getSupportFragmentManager(), getString(R.string.tips_mode_sync));
        if (deviceName.contains("-L")) {
            Log.d(TAG, "同步模式: 左耳 ");
            DeviceManager.getInstance().ctlMode(leftMac, mode);
        } else if (deviceName.contains("-R")) {
            Log.d(TAG, "同步模式: 右耳 ");
            DeviceManager.getInstance().ctlMode(rightMac, mode);
        }

        modeCnt = modeCnt-1;
        if (ctlFeedbackDisposable != null) { ctlFeedbackDisposable.dispose(); }
        ctlFeedbackDisposable = BTProtocol.share.ctlFeedbackObservable.subscribe(result -> {
            Log.d(TAG, "模式同步成功, 读取当前运行状态 (当前模式 和 各模式下的档位) " + result);
            String[] name_isSuccess = result.split(",");
            String name = name_isSuccess[0];
            boolean isSuccess = Boolean.parseBoolean(name_isSuccess[1]);
            if (name.contains("-L") && leftConnected) {
                DeviceManager.getInstance().readModeVolume(leftMac);
            } else if (name.contains("-R") && rightConnected) {
                DeviceManager.getInstance().readModeVolume(rightMac);
            }
            ctlFeedbackDisposable.dispose();
            ctlFeedbackDisposable = null;
            IOSLoadingDialog.instance.dismissDialog();
        });
    }

    // 初始化连接状态 获取自定义模式文件
    private void initStatus() {
        leftConnected                = false;
        rightConnected               = false;
        leftDevType                  = "";
        rightDevType                 = "";
        leftModeFileContent          = null;
        rightModeFileContent         = null;
        mutableLeftModeFileContent   = null;
        mutableRightModeFileContent  = null;
        connectedCnt                 = 0;
        readModeFileCnt              = 0;
        settingCnt                   = 0;
        modeCnt                      = 0;
        modeFileCnt                  = 0;

        for (BLEUtil.BLEDevice bleDevice : BLEUtil.getInstance().getBLEDevices()) {
            Log.d(TAG, bleDevice.deviceName + " " + bleDevice.connectStatus);
            if (bleDevice.deviceName.contains("-L") && bleDevice.connectStatus == BLEUtil.STATE_GET_GATT_SERVICES_OVER) {
                leftConnected = true;
                leftConnecting = false;
                connectedCnt++;
                leftMac = bleDevice.mac;
                leftDevType = bleDevice.devType;

            } else if (bleDevice.deviceName.contains("-L") && bleDevice.connectStatus == BLEUtil.STATE_DISCONNECTED) {
                leftModeFileContent = null;
                mutableLeftModeFileContent = null;

            } else if (bleDevice.deviceName.contains("-L") && (bleDevice.connectStatus == BLEUtil.STATE_RECONNECTING || bleDevice.connectStatus == BLEUtil.STATE_CONNECTING)) {
                delay = 2000;
                leftConnecting = true;

            } else if (bleDevice.deviceName.contains("-R") && bleDevice.connectStatus == BLEUtil.STATE_GET_GATT_SERVICES_OVER) {
                rightConnected = true;
                rightConnecting = false;
                connectedCnt++;
                rightMac = bleDevice.mac;
                rightDevType = bleDevice.devType;

            } else if (bleDevice.deviceName.contains("-R") && bleDevice.connectStatus == BLEUtil.STATE_DISCONNECTED) {
                rightModeFileContent = null;
                mutableRightModeFileContent = null;

            } else if (bleDevice.deviceName.contains("-R") && (bleDevice.connectStatus == BLEUtil.STATE_RECONNECTING || bleDevice.connectStatus == BLEUtil.STATE_CONNECTING)) {
                delay = 2000;
                rightConnecting = true;
            }
        }

        if (leftConnecting || rightConnecting) {
            CommonUtil.showToastLong(this, getString(R.string.Connecting));
            return;
        } else if (!leftConnected && !rightConnected) {
            return;
        } else {
            CommonUtil.showToastShort(this, getString(R.string.Connected));
        }

        IOSLoadingDialog.instance.setOnTouchOutside(true).showDialog(getSupportFragmentManager(), "");
//        // W3: 读取当前运行状态 (获取当前模式 和 各模式下的档位)
//        if (leftConnected) {
//            boolean result = DeviceManager.getInstance().readModeVolume(leftMac);
//            Log.d(TAG, "读取 左耳 当前模式 及 档位 " + result);
//            if (!result) { IOSLoadingDialog.instance.dismissDialog(); }
//        }
//        if (rightConnected) {
//            boolean result = DeviceManager.getInstance().readModeVolume(rightMac);
//            Log.d(TAG, "读取 右耳 当前模式 及 档位 " + result);
//            if (!result) { IOSLoadingDialog.instance.dismissDialog(); }
//        }
    }

    private void initRxListener() {
        // 带有一个Consumer参数的方法表示下游只关心onNext事件
        disposable0 = BTProtocol.share.sceneModeObservable.observeOn(AndroidSchedulers.mainThread())
                .subscribe(sceneMode -> {
                    Log.d(TAG, "获取模式成功 " + sceneMode);
                    if (sceneMode.getDeviceName().contains("-L")) {
                        if(sceneMode.getType() == BTProtocol.Read_Success ) {
                            modeCnt++;
                        }
                        Log.d(TAG, "L 获取模式文件");
                        leftMode = sceneMode;
                        DeviceManager.getInstance().setLeftMode(leftMode);
                        DeviceManager.getInstance().readModeFile(leftMac, leftMode);

                    } else if (sceneMode.getDeviceName().contains("-R")) {
                        if(sceneMode.getType() == BTProtocol.Read_Success ) {
                            modeCnt++;
                        }
                        Log.d(TAG, "R 获取模式文件");
                        rightMode = sceneMode;
                        DeviceManager.getInstance().setRightMode(rightMode);
                        DeviceManager.getInstance().readModeFile(rightMac, rightMode);
                    }

                    if (sceneMode.getType() == BTProtocol.Read_Success) {
                        if (modeCnt == connectedCnt && modeCnt == 2 && leftMode != rightMode) {
                            IOSLoadingDialog.instance.dismiss();
                            CommonUtil.showToast(this, getString(R.string.tips_mode_not_same));
                        } else if (modeCnt == connectedCnt) {
                            IOSLoadingDialog.instance.dismiss();
                            runOnUiThread(() -> {
                                Fragment fragment = getSupportFragmentManager().findFragmentByTag("f"+binding.viewPager.getCurrentItem());
                                Log.d(TAG, "fragment = " + fragment);
                            });
                        }

                    } else if (sceneMode.getType() == BTProtocol.Report_Success) {
                        if (connectedCnt == 2 && leftMode != rightMode) {
                            if (sceneMode.getDeviceName().contains("-L")) {
                                syncMode(sceneMode, "-R");
                            } else if (sceneMode.getDeviceName().contains("-R")) {
                                syncMode(sceneMode, "-L");
                            }
                        } else if (connectedCnt == 1) {

                        }
                    }
                });

        BTProtocol.share.ctlFeedbackObservable.subscribe(result -> {
            Log.d(TAG, "获取音量设置状态 " + result);
        });
    }

    private void unInitRxListener() {
        if (disposable0 != null) {
            disposable0.dispose();
            disposable0 = null;
        }
    }


}