package com.upixels.jh.hearingassist.ui.main;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.transition.TransitionManager;
import me.forrest.commonlib.jh.SceneMode;
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

    private FragmentVolumeBinding binding;
    private ConstraintLayout        layoutFragmentVolumeLeftRight;
    private ConstraintLayout        layoutFragmentVolumeLeft;
    private ConstraintLayout        layoutFragmentVolumeRight;
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

        layoutFragmentVolumeLeftRight = binding.layoutFragmentVolumeLR;
        constraintSetLeftRight.clone(layoutFragmentVolumeLeftRight);
        constraintSetLeft.clone(this.requireContext(), R.layout.fragment_volume_left);
        constraintSetRight.clone(this.requireContext(), R.layout.fragment_volume_right);

        binding.sbLeftVolume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

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

    @Override
    protected void updateView(SceneMode leftMode, SceneMode rightMode) {
        Log.d(TAG, "updateView leftMode = " + leftMode + " rightMode = " + rightMode);
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

        if (resIdL > 0 && resIdR > 0) {
            if (constraintSetFlag != 0) {
                constraintSetFlag = 0;
                constraintSetLeftRight.applyTo(layoutFragmentVolumeLeftRight);
                TransitionManager.beginDelayedTransition(layoutFragmentVolumeLeftRight);
            }

            binding.viewBgLR.setBackgroundResource(R.drawable.shape_view_bg_blue);
            binding.ivModeL.setImageResource(resIdL);
            binding.ivModeR.setImageResource(resIdR);
            if (leftMode != rightMode) {
                CommonUtil.showToastLong(requireActivity(), getString(R.string.tips_mode_not_same));
            }

        } else if (resIdL > 0) {
            if (constraintSetFlag != 1) {
                constraintSetFlag = 1;
                constraintSetLeft.applyTo(layoutFragmentVolumeLeftRight);
                TransitionManager.beginDelayedTransition(layoutFragmentVolumeLeftRight);
            }

            binding.viewBgLR.setBackgroundResource(R.drawable.shape_device_l_r);
            binding.ivModeL.setImageResource(resIdL);
            binding.sbLeftVolume.setProgress(leftMode.getVolume());
            binding.tvLVolume.setText(String.format(Locale.getDefault(),"%d%%", (int)((float)leftMode.getVolume() / 10 * 100)));

        } else if (resIdR > 0) {
            if (constraintSetFlag != 2) {
                constraintSetFlag = 2;
                constraintSetRight.applyTo(layoutFragmentVolumeLeftRight);
                TransitionManager.beginDelayedTransition(layoutFragmentVolumeLeftRight);
            }

            binding.viewBgLR.setBackgroundResource(R.drawable.shape_device_l_r);
            binding.ivModeR.setImageResource(resIdR);
            binding.sbRightVolume.setProgress(rightMode.getVolume());
            binding.tvRVolume.setText(String.format(Locale.getDefault(),"%d%%", (int)((float)rightMode.getVolume() / 10 * 100)));
        }
    }

}