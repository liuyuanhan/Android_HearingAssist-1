package com.upixels.jh.hearingassist;

import android.content.res.Configuration;
import android.util.Log;

//import com.umeng.analytics.MobclickAgent;
//import com.umeng.commonsdk.UMConfigure;

import com.upixels.jh.hearingassist.util.DeviceManager;

import java.util.HashSet;

import androidx.annotation.NonNull;
import me.forrest.commonlib.BaseApplication;
import me.forrest.commonlib.util.BLEUtil;
import me.forrest.commonlib.util.MultiLanguageUtil;
import me.forrest.commonlib.util.SPUtil;

public class MyApplication extends BaseApplication {
    private final static String TAG = "MyApplication";

//    @Override
//    protected void attachBaseContext(Context base) {
//        Log.d(TAG, "attachBaseContext");
//        super.attachBaseContext(LocaleUtil.updateResources(base, "x"));
//    }

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate");
        super.onCreate();
//        if (!BuildConfig.DEBUG) {
//            UMConfigure.preInit(this, "6094ff5853b6726499efe6e1", BuildConfig.CHANNEL);
//            if (BuildConfig.CHANNEL.equals("tencent") && SPUtil.getInstance(this).getBoolean("user_agreement", false)) {
//                UMConfigure.init(this, "6094ff5853b6726499efe6e1", BuildConfig.CHANNEL, UMConfigure.DEVICE_TYPE_PHONE, "");
//            } else {
//                UMConfigure.init(this, "6094ff5853b6726499efe6e1", BuildConfig.CHANNEL, UMConfigure.DEVICE_TYPE_PHONE, "");
//            }
//            UMConfigure.setLogEnabled(false);
//            MobclickAgent.setPageCollectionMode(MobclickAgent.PageMode.AUTO);
//        }
//        MultiLanguageUtil.init(this);
        BLEUtil.getInstance().init(getApplicationContext());
        DeviceManager.getInstance().init(getApplicationContext());
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        Log.d(TAG, "onConfigurationChanged");
        super.onConfigurationChanged(newConfig);
    }

    private HashSet<BLEUtil.BLEDevice> leftPairedDevices = new HashSet<>(5);
    private HashSet<BLEUtil.BLEDevice> rightPairedDevices = new HashSet<>(5);

    public HashSet<BLEUtil.BLEDevice> getLeftPairedDevices() {
        return this.leftPairedDevices;
    }

    public HashSet<BLEUtil.BLEDevice> getRightPairedDevices() {
        return this.rightPairedDevices;
    }

    private boolean firstStart = true;

    public boolean getFirstStart() {
        return this.firstStart;
    }

    public void setFirstStart(boolean firstStart) {
        this.firstStart = firstStart;
    }

    private int remoteVersionCode;

    public int getRemoteVersionCode() {
        return this.remoteVersionCode;
    }

    public void setRemoteVersionCode(int versionCode) {
        this.remoteVersionCode = versionCode;
    }

    // 保存输入的听力图结果
    private final int[] leftInputResult  = new int[] {20, 20, 20, 20, 20, 20, 20, 20};
    private final int[] rightInputResult = new int[] {20, 20, 20, 20, 20, 20, 20, 20};

    public int[] getLeftInputResult() {
        return leftInputResult;
    }

    public int[] getRightInputResult() {
        return rightInputResult;
    }

}
