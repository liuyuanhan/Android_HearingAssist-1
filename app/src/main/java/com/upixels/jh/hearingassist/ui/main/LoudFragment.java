package com.upixels.jh.hearingassist.ui.main;

import android.os.Bundle;
import android.util.Log;

import com.upixels.jh.hearingassist.R;
import com.upixels.jh.hearingassist.util.DeviceManager;

import androidx.annotation.Nullable;
import me.forrest.commonlib.jh.BTProtocol;


public class LoudFragment extends LNBaseFragment {

    public static LoudFragment newInstance() {
        return new LoudFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        TAG = "LoudFragment";
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void initView() {
        super.initView();
        binding.tvHearingModes.setText(R.string.Loud_Sound_Protection);
        binding.tvSet0.setText(R.string.No_Compression);
        binding.tvSet1.setText(R.string.Low_Compression);
        binding.tvSet2.setText(R.string.Medium_Compression);
        binding.tvSet3.setText(R.string.High_Compression);
        binding.tv0.setText(R.string.tips_loud_settings);
    }

    @Override
    protected void setHeardAid(String earType, int index) {
        Log.d(TAG, "setHeardAid earType = " + earType + " index = " + index);
        BTProtocol.Loud loud;
        switch (index) {
            case 0:
                loud = BTProtocol.Loud.No;
                break;
            case 1:
                loud = BTProtocol.Loud.Low;
                break;
            case 2:
                loud = BTProtocol.Loud.Medium;
                break;
            case 3:
                loud = BTProtocol.Loud.High;
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + index);
        }
        DeviceManager.getInstance().writeModeFileForLoud(earType, loud);
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
        if (leftContent != null) { cnt++; indexL = leftContent.getLoud().ordinal(); }
        if (rightContent != null) { cnt++; indexR = rightContent.getLoud().ordinal(); }
        if (cnt != 2) { isActionCombined = false; } // 只获取到了一个模式文件，肯定不需要同步动作
        this.checkedIndexL = indexL;
        this.checkedIndexR = indexR;
        uiChangeTextView(indexL, indexR);
    }


}