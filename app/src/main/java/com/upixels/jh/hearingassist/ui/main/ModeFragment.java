package com.upixels.jh.hearingassist.ui.main;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.upixels.jh.hearingassist.R;
import com.upixels.jh.hearingassist.databinding.FragmentModeBinding;
import com.upixels.jh.hearingassist.util.DeviceManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import me.forrest.commonlib.jh.AIDMode;
import me.forrest.commonlib.util.CommonUtil;

public class ModeFragment extends BaseFragment {
    private static final String TAG = ModeFragment.class.getSimpleName();
    private FragmentModeBinding binding;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        Log.d(TAG, "[onCreate]");
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "[onCreateView]");
        binding = FragmentModeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "[onViewCreated]");
        super.onViewCreated(view, savedInstanceState);
        initUIListener();
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

    private void initUIListener() {
        binding.btnConversation.setOnClickListener(clickListener);
        binding.btnRestaurant.setOnClickListener(clickListener);
        binding.btnOurDoor.setOnClickListener(clickListener);
        binding.btnMusic.setOnClickListener(clickListener);
    }

    private final View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            AIDMode aidMode = null;
            if (v == binding.btnConversation) {
                aidMode = new AIDMode(AIDMode.CONVERSATION);
            } else if (v == binding.btnRestaurant) {
                aidMode = new AIDMode(AIDMode.RESTAURANT);
            } else if (v == binding.btnOurDoor) {
                aidMode = new AIDMode(AIDMode.OUTDOOR);
            } else if (v == binding.btnMusic) {
                aidMode = new AIDMode(AIDMode.MUSIC);
            }
            if (aidMode != null) {
                DeviceManager.getInstance().ctlMode(aidMode);
                uiChange(aidMode);
            }
        }
    };

    private void uiListenerEnable(boolean enable) {
        binding.btnConversation.setEnabled(enable);
        binding.btnRestaurant.setEnabled(enable);
        binding.btnOurDoor.setEnabled(enable);
        binding.btnMusic.setEnabled(enable);
    }

    private void uiChange(AIDMode sceneMode) {
        binding.btnConversation.setSelected(false);
        binding.btnRestaurant.setSelected(false);
        binding.btnOurDoor.setSelected(false);
        binding.btnMusic.setSelected(false);
        switch (sceneMode.getMode()) {
            case AIDMode.CONVERSATION:
                binding.btnConversation.setSelected(true);
                break;
            case AIDMode.RESTAURANT:
                binding.btnRestaurant.setSelected(true);
                break;
            case AIDMode.OUTDOOR:
                binding.btnOurDoor.setSelected(true);
                break;
            case AIDMode.MUSIC:
                binding.btnMusic.setSelected(true);
                break;
        }
    }
    
    @Override
    protected void updateView(AIDMode leftMode, AIDMode rightMode) {
        Log.d(TAG, "updateView leftMode = " + leftMode + " rightMode = " + rightMode);
        if (leftMode != null && rightMode != null && leftMode.getMode() != rightMode.getMode()) {
            CommonUtil.showToastLong(requireActivity(), getString(R.string.tips_mode_not_same));
            uiChange(new AIDMode(AIDMode.UNKNOWN));
            uiListenerEnable(true);
        } else if (leftMode != null && rightMode != null && leftMode.getMode() == rightMode.getMode()) {
            uiChange(leftMode);
            uiListenerEnable(true);
        } else if (leftMode != null && rightMode == null) {
            uiChange(leftMode);
            uiListenerEnable(true);
        } else if (leftMode == null && rightMode != null) {
            uiChange(rightMode);
            uiListenerEnable(true);
        } else {
            uiChange(new AIDMode(AIDMode.UNKNOWN));
            uiListenerEnable(false);
        }
    }

    @Override
    protected void updateCtlFeedback(String leftResult, String rightResult) {
        Log.d(TAG, "updateCtlFeedback leftResult = " + leftResult + " rightResult = " + rightResult);
        boolean isSuccessL = true;
        boolean isSuccessR = true;
        if (leftResult != null) {
            isSuccessL = Boolean.parseBoolean(leftResult.split(",")[1]);
        }
        if (rightResult != null) {
            isSuccessR = Boolean.parseBoolean(rightResult.split(",")[1]);
        }
        if (isSuccessL && isSuccessR) {
            DeviceManager.getInstance().readModeVolume(true);
        }
    }
}