package com.upixels.jh.hearingassist.ui.main;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;

import com.upixels.jh.hearingassist.R;
import com.upixels.jh.hearingassist.databinding.FragmentLoudBinding;
import com.upixels.jh.hearingassist.databinding.FragmentLoudLeftBinding;
import com.upixels.jh.hearingassist.databinding.FragmentLoudRightBinding;
import com.upixels.jh.hearingassist.util.DeviceManager;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.transition.TransitionManager;
import me.forrest.commonlib.jh.BTProtocol;
import me.forrest.commonlib.jh.SceneMode;
import me.forrest.commonlib.util.CommonUtil;

public class LoudFragment extends BaseFragment {
    private final static String TAG = LoudFragment.class.getSimpleName();
    private FragmentLoudBinding         binding;
    private FragmentLoudLeftBinding     leftBinding;
    private FragmentLoudRightBinding    rightBinding;
    private ConstraintLayout            layoutLeftRight;
    private ConstraintLayout            layoutLeft;
    private ConstraintLayout            layoutRight;
    private ConstraintSet               constraintSetLeftRight;
    private ConstraintSet               constraintSetLeft;
    private ConstraintSet               constraintSetRight;
    private int                         constraintSetFlag = 0; // 0 , 1, 2 防止重复切换

    private boolean                     changeListenerIgnoreFlag;   // !! 需要UI监听器忽略该事件，因为主动设置RadioGroup时，监听器也会响应，进入了死循环
    private boolean                     leftRBIgnoreFlag;
    private boolean                     rightRBIgnoreFlag;

    public static LoudFragment newInstance() {
        return new LoudFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        Log.d(TAG, "[onCreate]");
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "[onCreateView]");
        binding = FragmentLoudBinding.inflate(inflater, container, false);
        leftBinding = FragmentLoudLeftBinding.inflate(inflater, container, false);
        rightBinding = FragmentLoudRightBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "[onViewCreated]");
        super.onViewCreated(view, savedInstanceState);
        initView();
    }

    @Override
    public void onStart() {
        Log.d(TAG, "[onStart]");
        super.onStart();
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
    }

    @Override
    public void onDestroyView() {
        Log.d(TAG, "[onDestroyView]");
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "[onDestroy]");
        super.onDestroy();
    }

    private void initView() {
        constraintSetLeftRight = new ConstraintSet();
        constraintSetLeft = new ConstraintSet();
        constraintSetRight = new ConstraintSet();

        layoutLeftRight = binding.layoutLoudLR;
        constraintSetLeftRight.clone(layoutLeftRight);

        layoutLeft = leftBinding.layoutLoudL;
        constraintSetLeft.clone(layoutLeft);

        layoutRight = rightBinding.layoutLoudR;
        constraintSetRight.clone(layoutRight);

        binding.radioGroupL.setOnCheckedChangeListener(changeListener);
        binding.radioGroupR.setOnCheckedChangeListener(changeListener);
    }

    // 改变 L R 按钮的UI状态
    private void uiChangeLRButton(int connectCnt, String earType) {
        if (connectCnt == 0) {
            if (constraintSetFlag != 0) {
                constraintSetFlag = 0;
                constraintSetLeftRight.applyTo(layoutLeftRight);
                TransitionManager.beginDelayedTransition(layoutLeftRight);
            }
            binding.viewBgLR.setBackgroundResource(R.drawable.shape_view_bg_white); // ConstrainSet 只能改变约束不能改变背景颜色

        } else if (connectCnt == 2) {
            if (constraintSetFlag != 0) {
                constraintSetFlag = 0;
                constraintSetLeftRight.applyTo(layoutLeftRight);
                TransitionManager.beginDelayedTransition(layoutLeftRight);
            }
            binding.viewBgLR.setBackgroundResource(R.drawable.shape_view_bg_red);


        } else if (connectCnt == 1 && earType.equals(DeviceManager.EAR_TYPE_LEFT)) {
            if (constraintSetFlag != 1) {
                constraintSetFlag = 1;
                constraintSetLeft.applyTo(layoutLeftRight);
                TransitionManager.beginDelayedTransition(layoutLeftRight);
            }
            binding.viewBgLR.setBackgroundResource(R.drawable.shape_view_bg_blue);

        } else if (connectCnt == 1 && earType.equals(DeviceManager.EAR_TYPE_RIGHT)) {
            if (constraintSetFlag != 2) {
                constraintSetFlag = 2;
                constraintSetRight.applyTo(layoutLeftRight);
                TransitionManager.beginDelayedTransition(layoutLeftRight);
            }
            binding.viewBgLR.setBackgroundResource(R.drawable.shape_view_bg_red);
        }
    }

    // 改变模式指示图标
    private void uiChangeLRModeImage(SceneMode leftMode, SceneMode rightMode) {
        int resIdL = 0;
        int resIdR = 0;
        if (leftMode != null) {
            switch (leftMode) {
                case CONVERSATION:
                    resIdL = R.drawable.icon_mode_conversation_blue;
                    break;
                case RESTAURANT:
                    resIdL = R.drawable.icon_mode_restaurant_blue;
                    break;
                case OUTDOOR:
                    resIdL = R.drawable.icon_mode_outdoor_blue;
                    break;
                case MUSIC:
                    resIdL = R.drawable.icon_mode_music_blue;
                    break;
            }
        }
        if (rightMode != null) {
            switch (rightMode) {
                case CONVERSATION:
                    resIdR = R.drawable.icon_mode_conversation_nor;
                    break;
                case RESTAURANT:
                    resIdR = R.drawable.icon_mode_restaurant_nor;
                    break;
                case OUTDOOR:
                    resIdR = R.drawable.icon_mode_outdoor_nor;
                    break;
                case MUSIC:
                    resIdR = R.drawable.icon_mode_music_nor;
                    break;
            }
        }
        if (resIdL > 0) { binding.ivModeL.setImageResource(resIdL); }
        if (resIdR > 0) { binding.ivModeR.setImageResource(resIdR); }
    }

    private void uiChangeCompression(BTProtocol.ModeFileContent leftContent, BTProtocol.ModeFileContent rightContent) {
        Log.d(TAG, "uiChangeCompression +");
        changeListenerIgnoreFlag = true;
        binding.tvNoCompression.setSelected(false);
        binding.tvLowCompression.setSelected(false);
        binding.tvMediumCompression.setSelected(false);
        binding.tvHighCompression.setSelected(false);
        if (leftContent != null) {
            BTProtocol.Loud compL = leftContent.getLoud();
            switch (compL) {
                case No:
                    binding.rbLNo.setChecked(true);
                    binding.tvNoCompression.setSelected(true);
                    break;
                case Low:
                    binding.rbLLow.setChecked(true);
                    binding.tvLowCompression.setSelected(true);
                    break;
                case Medium:
//                    binding.rbLMedium.setChecked(true);
//                    binding.tvMediumCompression.setSelected(true);
                    break;
                case High:
//                    binding.rbLHigh.setChecked(true);
//                    binding.tvHighCompression.setSelected(true);
                    break;
                case Unknown:
                    binding.rbLNo.setChecked(false);
                    binding.rbLLow.setChecked(false);
//                    binding.rbLMedium.setChecked(false);
//                    binding.rbLHigh.setChecked(false);
                    break;
            }
            binding.radioGroupL.setOnCheckedChangeListener(changeListener);
        } else {
            binding.radioGroupL.setOnCheckedChangeListener(null);
        }

        if (rightContent != null) {
            BTProtocol.Loud compR = rightContent.getLoud();
            switch (compR) {
                case No:
                    binding.rbRNo.setChecked(true);
                    binding.tvNoCompression.setSelected(true);
                    break;
                case Low:
                    binding.rbRLow.setChecked(true);
                    binding.tvLowCompression.setSelected(true);
                    break;
                case Medium:
//                    binding.rbRMedium.setChecked(true);
                    binding.tvMediumCompression.setSelected(true);
                    break;
                case High:
//                    binding.rbRHigh.setChecked(true);
                    binding.tvHighCompression.setSelected(true);
                    break;
                case Unknown:
                    binding.rbRNo.setChecked(false);
                    binding.rbRLow.setChecked(false);
//                    binding.rbRMedium.setChecked(false);
//                    binding.rbRHigh.setChecked(false);
                    break;
            }
            binding.radioGroupR.setOnCheckedChangeListener(changeListener);
        } else {
            binding.radioGroupR.setOnCheckedChangeListener(null);
        }
        changeListenerIgnoreFlag = false;
        Log.d(TAG, "uiChangeCompression -");
    }

    public final RadioGroup.OnCheckedChangeListener changeListener =  new RadioGroup.OnCheckedChangeListener() {

        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            if (changeListenerIgnoreFlag) { return; }
            if (rightRBIgnoreFlag || leftRBIgnoreFlag ) { // 防止调用setChecked时又触发监听器
                rightRBIgnoreFlag = false;
                leftRBIgnoreFlag = false;
                return;
            }

//            if (checkedId == binding.rbLNo.getId() && binding.rbLNo.isChecked()) {  // 点击左耳按钮
//                leftContent.MPO = 0;
//                if (isActionCombined) {
//                    rightContent.MPO = 0;
//                    rightRBIgnoreFlag = true;
//                    binding.rbRNo.setChecked(true);
//                }
//
//            } else if (checkedId == binding.rbLLow.getId() && binding.rbLLow.isChecked() ) {
//                leftContent.MPO = 1;
//                if (isActionCombined) {
//                    rightContent.MPO = 1;
//                    rightRBIgnoreFlag = true;
//                    binding.rbRLow.setChecked(true);
//                }
//
//            } else if (checkedId == binding.rbLMedium.getId() && binding.rbLMedium.isChecked() ) {
//                leftContent.MPO = 2;
//                if (isActionCombined) {
//                    rightContent.MPO = 2;
//                    rightRBIgnoreFlag = true;
//                    binding.rbRMedium.setChecked(true);
//                }
//
//            } else if (checkedId == binding.rbLHigh.getId() && binding.rbLHigh.isChecked() ) {
//                leftContent.MPO = 3;
//                if (isActionCombined) {
//                    rightContent.MPO = 3;
//                    rightRBIgnoreFlag = true;
//                    binding.rbRHigh.setChecked(true);
//                }
//
//            } else if (checkedId== binding.rbRNo.getId() && binding.rbRNo.isChecked() ) { // 点击右耳按钮
//                rightContent.MPO = 0;
//                if (isActionCombined) {
//                    leftContent.MPO = 0;
//                    leftRBIgnoreFlag = true;
//                    binding.rbLNo.setChecked(true);
//                }
//
//            } else if (checkedId == binding.rbRLow.getId() && binding.rbRLow.isChecked() ) {
//                rightContent.MPO = 1;
//                if (isActionCombined) {
//                    leftContent.MPO = 1;
//                    leftRBIgnoreFlag = true;
//                    binding.rbLLow.setChecked(true);
//                }
//
//            } else if (checkedId == binding.rbRMedium.getId() && binding.rbRMedium.isChecked() ) {
//                rightContent.MPO = 2;
//                if (isActionCombined) {
//                    leftContent.MPO = 2;
//                    leftRBIgnoreFlag = true;
//                    binding.rbLMedium.setChecked(true);
//                }
//
//            } else if (checkedId == binding.rbRHigh.getId() && binding.rbRHigh.isChecked() ) {
//                rightContent.MPO = 3;
//                if (isActionCombined) {
//                    leftContent.MPO = 3;
//                    leftRBIgnoreFlag = true;
//                    binding.rbLHigh.setChecked(true);
//                }
//            }

            binding.tvNoCompression.setSelected(false);
            binding.tvLowCompression.setSelected(false);
            binding.tvMediumCompression.setSelected(false);
            binding.tvHighCompression.setSelected(false);
            boolean isSelectedLNo     = false;
            boolean isSelectedLLow    = false;
            boolean isSelectedLMedium = false;
            boolean isSelectedLHigh   = false;
            String test = "";
            if (leftContent != null) {
                isSelectedLNo     = leftContent.MPO == 0;
                isSelectedLLow    = leftContent.MPO == 1;
                isSelectedLMedium = leftContent.MPO == 2;
                isSelectedLHigh   = leftContent.MPO >= 3;
                test = "leftContent.MPO = " + leftContent.MPO;
            }
            boolean isSelectedRNo     = false;
            boolean isSelectedRLow    = false;
            boolean isSelectedRMedium = false;
            boolean isSelectedRHigh   = false;
            if (rightContent != null) {
                isSelectedRNo     = rightContent.MPO == 0;
                isSelectedRLow    = rightContent.MPO == 1;
                isSelectedRMedium = rightContent.MPO == 2;
                isSelectedRHigh   = rightContent.MPO >= 3;
                test = test + " rightContent.MPO = " + rightContent.MPO;
            }
            Log.d(TAG, test);
            binding.tvNoCompression.setSelected(isSelectedLNo || isSelectedRNo);
            binding.tvLowCompression.setSelected(isSelectedLLow || isSelectedRLow);
            binding.tvMediumCompression.setSelected(isSelectedLMedium || isSelectedRMedium);
            binding.tvHighCompression.setSelected(isSelectedLHigh || isSelectedRHigh);
        }
    };

    @Override
    protected void updateView(SceneMode leftMode, SceneMode rightMode) {
        Log.d(TAG, "updateView leftMode = " + leftMode + " rightMode = " + rightMode);
        int cnt = 0;
        if (leftMode != null) { cnt++; }
        if (rightMode != null) { cnt++; }
        uiChangeLRModeImage(leftMode, rightMode);
        if (cnt == 2) {
            if (leftMode == rightMode) {
                uiChangeLRButton(cnt, null);
                DeviceManager.getInstance().readModeFile(leftMode);
            } else {
                CommonUtil.showToastLong(requireActivity(), getString(R.string.tips_mode_not_same));
            }

        } else if (cnt == 1) {
            if (leftMode != null) {
                uiChangeLRButton(1, DeviceManager.EAR_TYPE_LEFT);
                DeviceManager.getInstance().readModeFile(leftMode);
            } else {
                uiChangeLRButton(1, DeviceManager.EAR_TYPE_RIGHT);
                DeviceManager.getInstance().readModeFile(rightMode);
            }

        } else {
            uiChangeLRButton(0, null);
        }
        uiChangeCompression(leftContent, rightContent);
    }

    @Override
    protected void updateModeFile(BTProtocol.ModeFileContent leftContent, BTProtocol.ModeFileContent rightContent) {

    }

}