package com.upixels.jh.hearingassist.ui.main;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.upixels.jh.hearingassist.R;
import com.upixels.jh.hearingassist.databinding.FragmentFocusBinding;
import com.upixels.jh.hearingassist.databinding.FragmentNoiseBinding;
import com.upixels.jh.hearingassist.util.DeviceManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import me.forrest.commonlib.jh.BTProtocol;
import me.forrest.commonlib.jh.SceneMode;
import me.forrest.commonlib.util.CommonUtil;

public class FocusFragment extends BaseFragment {
    private final static String TAG = FocusFragment.class.getSimpleName();
    private FragmentFocusBinding binding;

    public static FocusFragment newInstance() {
        return new FocusFragment();
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
        binding = FragmentFocusBinding.inflate(inflater, container, false);
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
        binding.btnNormal.setOnClickListener(clickListener);
        binding.btnTv.setOnClickListener(clickListener);
        binding.btnMeeting.setOnClickListener(clickListener);
        binding.btnFaceToFace.setOnClickListener(clickListener);
        binding.btnConfigure.setOnClickListener(clickListener);
    }

    private View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (leftMode != null && rightMode != null && leftMode != rightMode) {
                CommonUtil.showToastShort(requireActivity(), R.string.tips_mode_not_same);
                return;
            }
            if (v.getId() != R.id.btn_configure) {
                binding.btnNormal.setSelected(false);
                binding.btnTv.setSelected(false);
                binding.btnMeeting.setSelected(false);
                binding.btnFaceToFace.setSelected(false);
                v.setSelected(true);
            } else {
                BTProtocol.Directional directional;
                if (binding.btnNormal.isSelected()) { directional = BTProtocol.Directional.normal; }
                else if (binding.btnTv.isSelected()) { directional = BTProtocol.Directional.TV; }
                else if (binding.btnMeeting.isSelected()) { directional = BTProtocol.Directional.meeting; }
                else if (binding.btnFaceToFace.isSelected()) { directional = BTProtocol.Directional.face_to_face; }
                else {directional = BTProtocol.Directional.unknown; }
                DeviceManager.getInstance().writeModeFileForDirectional(directional);
            }
        }
    };

    // 改变Directional图标
    private void uiChangeDirectional(BTProtocol.ModeFileContent leftContent, BTProtocol.ModeFileContent rightContent) {
        binding.btnNormal.setSelected(false);
        binding.btnTv.setSelected(false);
        binding.btnMeeting.setSelected(false);
        binding.btnFaceToFace.setSelected(false);
        if(leftContent != null) {
            switch (leftContent.getDirectional()) {
                case normal:
                    binding.btnNormal.setSelected(true);
                    break;
                case TV:
                    binding.btnTv.setSelected(true);
                    break;
                case meeting:
                    binding.btnMeeting.setSelected(true);
                    break;
                case face_to_face:
                    binding.btnFaceToFace.setSelected(true);
                    break;
                case unknown:
                    break;
            }
        }

        if (rightContent != null) {
            switch (rightContent.getDirectional()) {
                case normal:
                    binding.btnNormal.setSelected(true);
                    break;
                case TV:
                    binding.btnTv.setSelected(true);
                    break;
                case meeting:
                    binding.btnMeeting.setSelected(true);
                    break;
                case face_to_face:
                    binding.btnFaceToFace.setSelected(true);
                    break;
                case unknown:
                    break;
            }
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
        if (resIdL > 0) {
            binding.ivModeL.setImageResource(resIdL);
            binding.ivModeL.setVisibility(View.VISIBLE);
        } else {
            binding.ivModeL.setVisibility(View.INVISIBLE);
        }
        if (resIdR > 0) {
            binding.ivModeR.setImageResource(resIdR);
            binding.ivModeR.setVisibility(View.VISIBLE);
        } else {
            binding.ivModeL.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    protected void updateView(SceneMode leftMode, SceneMode rightMode) {
        Log.d(TAG, "updateView leftMode = " + leftMode + " rightMode = " + rightMode);
        int cnt = 0;
        if (leftMode != null) { cnt++; }
        if (rightMode != null) { cnt++; }
        uiChangeLRModeImage(leftMode, rightMode);
        if (cnt == 2) {
            if (leftMode == rightMode) {
                DeviceManager.getInstance().readModeFile(leftMode);
            } else {
                CommonUtil.showToastLong(requireActivity(), getString(R.string.tips_mode_not_same));
                return;
            }

        } else if (cnt == 1) {
            if (leftMode != null) {
                DeviceManager.getInstance().readModeFile(leftMode);
            } else {
                DeviceManager.getInstance().readModeFile(rightMode);
            }
        }
        uiChangeDirectional(leftContent, rightContent);
    }

    @Override
    protected void updateModeFile(BTProtocol.ModeFileContent leftContent, BTProtocol.ModeFileContent rightContent) {
        Log.d(TAG, "updateModeFile leftContent = " + leftContent + " rightContent = " + rightContent);
        this.leftContent = leftContent;
        this.rightContent = rightContent;
        int cnt = 0;
        if (leftContent != null) { cnt++; }
        if (rightContent != null) { cnt++; }
        uiChangeDirectional(leftContent, rightContent);
    }

    @Override
    protected void updateWriteFeedback(String leftResult, String rightResult) {
        Log.d(TAG, "updateWriteFeedback leftResult = " + leftResult + " rightResult = " + rightResult);
        boolean isSuccessL = true;
        boolean isSuccessR = true;
        if (leftResult != null) {
            isSuccessL = Boolean.parseBoolean(leftResult.split(",")[1]);
        }
        if (rightResult != null) {
            isSuccessR = Boolean.parseBoolean(rightResult.split(",")[1]);
        }
        if (isSuccessL && isSuccessR) {
            CommonUtil.showToastLong(requireActivity(), getString(R.string.tips_configure_ok));
        }
    }
}