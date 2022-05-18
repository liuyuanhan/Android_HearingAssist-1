package com.upixels.jh.hearingassist.ui.main;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.transition.TransitionManager;
import me.forrest.commonlib.jh.AIDMode;
import me.forrest.commonlib.util.CommonUtil;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;

import com.upixels.jh.hearingassist.R;
import com.upixels.jh.hearingassist.databinding.FragmentVolumeBinding;
import com.upixels.jh.hearingassist.util.DeviceManager;

import java.util.Locale;


public class VolumeFragment extends BaseFragment {
    private final static String TAG = VolumeFragment.class.getSimpleName();

    private FragmentVolumeBinding   binding;
    private ConstraintLayout        layoutLeftRight;
    private ConstraintLayout        layoutLeft;
    private ConstraintLayout        layoutRight;
    private ConstraintSet           constraintSetLeftRight;
    private ConstraintSet           constraintSetLeft;
    private ConstraintSet           constraintSetRight;
    private int                     constraintSetFlag = 0; // 0 , 1, 2 防止重复切换

    public VolumeFragment() {
    }

    public static VolumeFragment newInstance() {
        VolumeFragment fragment = new VolumeFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "[onCreate]");
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "[onCreateView]");
        binding = FragmentVolumeBinding.inflate(inflater, container, false);
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

        layoutLeftRight = binding.layoutFragmentVolumeLR;
        constraintSetLeftRight.clone(layoutLeftRight);
        constraintSetLeft.clone(this.requireContext(), R.layout.fragment_volume_left);
        constraintSetRight.clone(this.requireContext(), R.layout.fragment_volume_right);

        binding.sbLeftVolume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                binding.tvLVolume.setText(String.format(Locale.getDefault(),"%d%%", progress * 10));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                String leftMac = DeviceManager.getInstance().getLeftMac();
                leftMode.setVolume((byte) seekBar.getProgress());
                DeviceManager.getInstance().ctlVolume(leftMac, leftMode);
            }
        });

        binding.sbRightVolume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                binding.tvRVolume.setText(String.format(Locale.getDefault(),"%d%%", progress * 10));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                String rightMac = DeviceManager.getInstance().getRightMac();
                rightMode.setVolume((byte) seekBar.getProgress());
                DeviceManager.getInstance().ctlVolume(rightMac, rightMode);
            }
        });
    }

    private void uiListenerEnable(boolean enable) {
        binding.sbLeftVolume.setEnabled(enable);
        binding.sbRightVolume.setEnabled(enable);
    }

    // 改变 L / R 按钮的UI状态
    protected void uiChangeLRButton(int connectCnt, String earType) {
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
    protected void uiChangeLRModeImage(AIDMode leftMode, AIDMode rightMode) {
        int resIdL = 0;
        int resIdR = 0;
        if (leftMode != null) {
            switch (leftMode.getMode()) {
                case AIDMode.CONVERSATION:
                    resIdL = R.drawable.icon_mode_conversation_blue;
                    break;
                case AIDMode.RESTAURANT:
                    resIdL = R.drawable.icon_mode_restaurant_blue;
                    break;
                case AIDMode.OUTDOOR:
                    resIdL = R.drawable.icon_mode_outdoor_blue;
                    break;
                case AIDMode.MUSIC:
                    resIdL = R.drawable.icon_mode_music_blue;
                    break;
            }
        }
        if (rightMode != null) {
            switch (rightMode.getMode()) {
                case AIDMode.CONVERSATION:
                    resIdR = R.drawable.icon_mode_conversation_nor;
                    break;
                case AIDMode.RESTAURANT:
                    resIdR = R.drawable.icon_mode_restaurant_nor;
                    break;
                case AIDMode.OUTDOOR:
                    resIdR = R.drawable.icon_mode_outdoor_nor;
                    break;
                case AIDMode.MUSIC:
                    resIdR = R.drawable.icon_mode_music_nor;
                    break;
            }
        }
        if (resIdL > 0) { binding.ivModeL.setImageResource(resIdL); }
        if (resIdR > 0) { binding.ivModeR.setImageResource(resIdR); }
    }

    protected void uiChangeTextView(int volumeL, int volumeR) {
        Log.d(TAG, "uiChangeTextView + volumeL = " + volumeL + " volumeR = "+volumeR);
        if (volumeL >= 0) {
            binding.sbLeftVolume.setProgress(leftMode.getVolume());
            binding.tvLVolume.setText(String.format(Locale.getDefault(),"%d%%", (int)((float)leftMode.getVolume() / 10 * 100)));
        }
        if (volumeR >= 0) {
            binding.sbRightVolume.setProgress(rightMode.getVolume());
            binding.tvRVolume.setText(String.format(Locale.getDefault(),"%d%%", (int)((float)rightMode.getVolume() / 10 * 100)));
        }
        Log.d(TAG, "uiChangeTextView -");
    }

    @Override
    protected void updateView(AIDMode leftMode, AIDMode rightMode) {
        Log.d(TAG, "updateView leftMode = " + leftMode + " rightMode = " + rightMode);
        int cnt = 0;
        int volumeL = -1;
        int volumeR = -1;
        if (leftMode != null) { cnt++; volumeL = leftMode.getVolume(); }
        if (rightMode != null) { cnt++; volumeR = rightMode.getVolume(); }
        uiChangeLRModeImage(leftMode, rightMode);
        uiChangeTextView(volumeL, volumeR);
        if (cnt > 0) { uiListenerEnable(true); }
        if (cnt == 2) {
            if (leftMode.getMode() == rightMode.getMode()) {
                uiChangeLRButton(cnt, null);
            } else {
                CommonUtil.showToastLong(requireActivity(), getString(R.string.tips_mode_not_same));
                uiListenerEnable(false);
            }

        } else if (cnt == 1) {
            if (leftMode != null) {
                uiChangeLRButton(1, DeviceManager.EAR_TYPE_LEFT);
            } else {
                uiChangeLRButton(1, DeviceManager.EAR_TYPE_RIGHT);
            }

        } else {
            uiChangeLRButton(0, null);
        }
    }

}