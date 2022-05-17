package com.upixels.jh.hearingassist.ui.main;

import android.os.Bundle;
import android.util.Log;

import com.upixels.jh.hearingassist.util.DeviceManager;

import androidx.annotation.Nullable;
import me.forrest.commonlib.jh.BTProtocol;


public class NoiseFragment extends LNBaseFragment {

    public static NoiseFragment newInstance() {
        return new NoiseFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        TAG = "NoiseFragment";
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void setHeardAid(String earType, int index) {
        BTProtocol.Noise noise;
        switch (index) {
            case 0:
                noise = BTProtocol.Noise.Off;
                break;
            case 1:
                noise = BTProtocol.Noise.Mid;
                break;
            case 2:
                noise = BTProtocol.Noise.Medium;
                break;
            case 3:
                noise = BTProtocol.Noise.High;
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + index);
        }
        DeviceManager.getInstance().writeModeFileForNoise(earType, noise);
    }

    // 在子类重写 Noise 和 Loud 需要获取的 模式文件的内容不同
    @Override
    protected void updateModeFile(BTProtocol.ModeFileContent leftContent, BTProtocol.ModeFileContent rightContent) {
        Log.d(TAG, "updateModeFile leftContent = " + leftContent + " rightContent = " + rightContent);
        int indexL = -1;
        int indexR = -1;
        this.leftContent = leftContent;
        this.rightContent = rightContent;
        int cnt = 0;
        if (leftContent != null) { cnt++; indexL = leftContent.getNoise().ordinal(); }
        if (rightContent != null) { cnt++; indexR = rightContent.getNoise().ordinal(); }
        if (cnt != 2) { isActionCombined = false; } // 只获取到了一个模式文件，肯定不需要同步动作
        this.checkedIndexL = indexL;
        this.checkedIndexR = indexR;
        uiChangeTextView(indexL, indexR);
    }


}