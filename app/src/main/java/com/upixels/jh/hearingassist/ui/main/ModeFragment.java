package com.upixels.jh.hearingassist.ui.main;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.upixels.jh.hearingassist.R;
import com.upixels.jh.hearingassist.databinding.FragmentModeBinding;
import com.upixels.jh.hearingassist.util.DeviceManager;

import androidx.fragment.app.Fragment;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import me.forrest.commonlib.jh.SceneMode;
import me.forrest.commonlib.util.CommonUtil;
import me.forrest.commonlib.view.IOSLoadingDialog;

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
        binding.btnConversation.setOnClickListener( l -> {
            DeviceManager.getInstance().ctlMode(SceneMode.CONVERSATION);
            uiChange(SceneMode.CONVERSATION);
        });

        binding.btnRestaurant.setOnClickListener( l -> {
            DeviceManager.getInstance().ctlMode(SceneMode.RESTAURANT);
            uiChange(SceneMode.RESTAURANT);
        });

        binding.btnOurDoor.setOnClickListener(l -> {
            DeviceManager.getInstance().ctlMode(SceneMode.OUTDOOR);
            uiChange(SceneMode.OUTDOOR);
        });

        binding.btnMusic.setOnClickListener( l -> {
            DeviceManager.getInstance().ctlMode(SceneMode.MUSIC);
            uiChange(SceneMode.MUSIC);
        });
    }

    private void uiChange(SceneMode sceneMode) {
        binding.btnConversation.setSelected(false);
        binding.btnRestaurant.setSelected(false);
        binding.btnOurDoor.setSelected(false);
        binding.btnMusic.setSelected(false);
        switch (sceneMode) {
            case CONVERSATION:
                binding.btnConversation.setSelected(true);
                break;
            case RESTAURANT:
                binding.btnRestaurant.setSelected(true);
                break;
            case OUTDOOR:
                binding.btnOurDoor.setSelected(true);
                break;
            case MUSIC:
                binding.btnMusic.setSelected(true);
                break;
        }
    }
    
    @Override
    protected void updateView(SceneMode leftMode, SceneMode rightMode) {
        Log.d(TAG, "updateView leftMode = " + leftMode + " rightMode = " + rightMode);
        if (leftMode != null && rightMode != null && leftMode != rightMode) {
            CommonUtil.showToastLong(requireActivity(), getString(R.string.tips_mode_not_same));
        } else if (leftMode != null && leftMode == rightMode) {
            uiChange(leftMode);
        } else if (leftMode == null && rightMode != null) {
            uiChange(rightMode);
        } else {
            uiChange(SceneMode.UNKNOWN);
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
            DeviceManager.getInstance().readModeVolume();
        }
    }
}