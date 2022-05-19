package com.upixels.jh.hearingassist.ui.main;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.upixels.jh.hearingassist.R;
import com.upixels.jh.hearingassist.databinding.FragmentFocusBinding;
import com.upixels.jh.hearingassist.util.DeviceManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import me.forrest.commonlib.jh.AIDMode;
import me.forrest.commonlib.jh.BTProtocol;
import me.forrest.commonlib.util.CommonUtil;

public class FocusFragment extends BaseFragment {
    private final static String         TAG = FocusFragment.class.getSimpleName();
    private FragmentFocusBinding        binding;

    protected int                       checkedIndexL;
    protected int                       checkedIndexR;

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

    private final View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (leftMode != null && rightMode != null && leftMode.getMode() != rightMode.getMode()) {
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
                BTProtocol.Focus directional;
                if (binding.btnNormal.isSelected()) { directional = BTProtocol.Focus.normal; }
                else if (binding.btnTv.isSelected()) { directional = BTProtocol.Focus.TV; }
                else if (binding.btnMeeting.isSelected()) { directional = BTProtocol.Focus.meeting; }
                else if (binding.btnFaceToFace.isSelected()) { directional = BTProtocol.Focus.face_to_face; }
                else { return; }
                DeviceManager.getInstance().writeModeFileForDirectional(directional);
            }
        }
    };

    // 改变模式指示图标
    private void uiChangeLRModeImage(AIDMode leftMode, AIDMode rightMode) {
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
        binding.ivModeL.setImageResource(resIdL);
        binding.ivModeR.setImageResource(resIdR);
    }

    // 改变Directional图标
    private void uiChangeDirectional(int indexL, int indexR) {
        Log.d(TAG, "uiChangeDirectional indexL = " + indexL + " indexR = " + indexR);
        binding.btnNormal.setSelected(false);
        binding.btnTv.setSelected(false);
        binding.btnMeeting.setSelected(false);
        binding.btnFaceToFace.setSelected(false);
        if(indexL >= 0) {
            switch (indexL) {
                case 0:
                    binding.btnNormal.setSelected(true);
                    break;
                case 1:
                    binding.btnTv.setSelected(true);
                    break;
                case 2:
                    binding.btnMeeting.setSelected(true);
                    break;
                case 3:
                    binding.btnFaceToFace.setSelected(true);
                    break;
                case 4:
                    break;
            }
        }

        if (indexR >= 0) {
            switch (indexR) {
                case 0:
                    binding.btnNormal.setSelected(true);
                    break;
                case 1:
                    binding.btnTv.setSelected(true);
                    break;
                case 2:
                    binding.btnMeeting.setSelected(true);
                    break;
                case 3:
                    binding.btnFaceToFace.setSelected(true);
                    break;
                case 4:
                    break;
            }
        }
    }

    @Override
    protected void updateView(AIDMode leftMode, AIDMode rightMode) {
        Log.d(TAG, "updateView leftMode = " + leftMode + " rightMode = " + rightMode);
        int cnt = 0;
        if (leftMode != null) { cnt++; }
        if (rightMode != null) { cnt++; }
        uiChangeLRModeImage(leftMode, rightMode);
        if (cnt == 2) {
            if (leftMode.getMode() == rightMode.getMode()) {
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
        uiChangeDirectional(-1, -1);
    }

    @Override
    protected void updateModeFile(BTProtocol.ModeFileContent leftContent, BTProtocol.ModeFileContent rightContent) {
        Log.d(TAG, "updateModeFile leftContent = " + leftContent + " rightContent = " + rightContent);
        this.leftContent = leftContent;
        this.rightContent = rightContent;
        int indexL = -1;
        int indexR = -1;
        int cnt = 0;
        if (leftContent != null) { cnt++; indexL = leftContent.getFocus().ordinal(); }
        if (rightContent != null) { cnt++; indexR = rightContent.getFocus().ordinal(); }
        this.checkedIndexL = indexL;
        this.checkedIndexR = indexR;
        uiChangeDirectional(indexL, indexR);
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