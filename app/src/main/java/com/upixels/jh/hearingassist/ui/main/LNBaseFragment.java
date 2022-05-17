package com.upixels.jh.hearingassist.ui.main;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;

import com.upixels.jh.hearingassist.R;
import com.upixels.jh.hearingassist.databinding.FragmentLnBaseBinding;
import com.upixels.jh.hearingassist.databinding.FragmentLnBaseLeftBinding;
import com.upixels.jh.hearingassist.databinding.FragmentLnBaseRightBinding;
import com.upixels.jh.hearingassist.util.DeviceManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.transition.TransitionManager;
import me.forrest.commonlib.jh.BTProtocol;
import me.forrest.commonlib.jh.SceneMode;
import me.forrest.commonlib.util.CommonUtil;

/**
 * Loud 和 Noise 具有相同的UI结构，只有文字不同，这里统一做一个基类用于处理相同操作
 *
 *
 */
public class LNBaseFragment extends BaseFragment {
    protected static String TAG = "";
    private FragmentLnBaseBinding       binding;
    private FragmentLnBaseLeftBinding   leftBinding;
    private FragmentLnBaseRightBinding  rightBinding;
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

    protected int                       checkedIndexL;
    protected int                       checkedIndexR;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        Log.d(TAG, "[onCreate]");
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "[onCreateView]");
        binding = FragmentLnBaseBinding.inflate(inflater, container, false);
        leftBinding = FragmentLnBaseLeftBinding.inflate(inflater, container, false);
        rightBinding = FragmentLnBaseRightBinding.inflate(inflater, container, false);
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

    protected void initView() {
        constraintSetLeftRight = new ConstraintSet();
        constraintSetLeft = new ConstraintSet();
        constraintSetRight = new ConstraintSet();

        layoutLeftRight = binding.layoutDeviceLR;
        constraintSetLeftRight.clone(layoutLeftRight);

        layoutLeft = leftBinding.layoutDeviceL;
        constraintSetLeft.clone(layoutLeft);

        layoutRight = rightBinding.layoutDeviceR;
        constraintSetRight.clone(layoutRight);
    }

    // 设置助听器的参数 需要子类重写
    protected void setHeardAid(String earType, int index) {}

    // 改变 L R 按钮的UI状态
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
    protected void uiChangeLRModeImage(SceneMode leftMode, SceneMode rightMode) {
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

    // 改变中间的文字显示状态
    protected void uiChangeTextView(int indexL, int indexR) {
        Log.d(TAG, "uiChangeTextView + indexL=" + indexL + " indexR="+indexR);
        changeListenerIgnoreFlag = true;
        binding.tvSet0.setSelected(false);
        binding.tvSet1.setSelected(false);
        binding.tvSet2.setSelected(false);
        binding.tvSet3.setSelected(false);
        if (indexL >= 0) {
            switch (indexL) {
                case 0:
                    binding.rbLSet0.setChecked(true);
                    binding.tvSet0.setSelected(true);
                    break;
                case 1:
                    binding.rbLSet1.setChecked(true);
                    binding.tvSet1.setSelected(true);
                    break;
                case 2:
                    binding.rbLSet2.setChecked(true);
                    binding.tvSet2.setSelected(true);
                    break;
                case 3:
                    binding.rbLSet3.setChecked(true);
                    binding.tvSet3.setSelected(true);
                    break;
                case 4:
                    binding.rbLSet0.setChecked(false);
                    binding.rbLSet1.setChecked(false);
                    binding.rbLSet2.setChecked(false);
                    binding.rbLSet3.setChecked(false);
                    break;
            }
            binding.radioGroupL.setOnCheckedChangeListener(changeListener);
        } else {
            binding.radioGroupL.setOnCheckedChangeListener(null);
        }

        if (indexR >= 0) {
            switch (indexR) {
                case 0:
                    binding.rbRSet0.setChecked(true);
                    binding.tvSet0.setSelected(true);
                    break;
                case 1:
                    binding.rbRSet1.setChecked(true);
                    binding.tvSet1.setSelected(true);
                    break;
                case 2:
                    binding.rbRSet2.setChecked(true);
                    binding.tvSet2.setSelected(true);
                    break;
                case 3:
                    binding.rbRSet3.setChecked(true);
                    binding.tvSet3.setSelected(true);
                    break;
                case 4:
                    binding.rbRSet0.setChecked(false);
                    binding.rbRSet1.setChecked(false);
                    binding.rbRSet2.setChecked(false);
                    binding.rbRSet3.setChecked(false);
                    break;
            }
            binding.radioGroupR.setOnCheckedChangeListener(changeListener);
        } else {
            binding.radioGroupR.setOnCheckedChangeListener(null);
        }
        changeListenerIgnoreFlag = false;
        Log.d(TAG, "uiChangeTextView -");
    }

    protected final RadioGroup.OnCheckedChangeListener changeListener =  new RadioGroup.OnCheckedChangeListener() {

        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            Log.d(TAG, "onCheckedChanged ");
            String earType = "";
            if (changeListenerIgnoreFlag) { return; }
            if (rightRBIgnoreFlag || leftRBIgnoreFlag ) { // 防止调用setChecked时又触发监听器
                rightRBIgnoreFlag = false;
                leftRBIgnoreFlag = false;
                return;
            }
            // 点击左耳按钮
            if (checkedId == binding.rbLSet0.getId() && binding.rbLSet0.isChecked()) {
                earType = DeviceManager.EAR_TYPE_LEFT;
                checkedIndexL = 0;
                if (isActionCombined) {
                    checkedIndexR = 0;
                    rightRBIgnoreFlag = true;
                    binding.rbRSet0.setChecked(true);
                }

            } else if (checkedId == binding.rbLSet1.getId() && binding.rbLSet1.isChecked() ) {
                earType = DeviceManager.EAR_TYPE_LEFT;
                checkedIndexL = 1;
                if (isActionCombined) {
                    checkedIndexR = 1;
                    rightRBIgnoreFlag = true;
                    binding.rbRSet1.setChecked(true);
                }

            } else if (checkedId == binding.rbLSet2.getId() && binding.rbLSet2.isChecked() ) {
                earType = DeviceManager.EAR_TYPE_LEFT;
                checkedIndexL = 2;
                if (isActionCombined) {
                    checkedIndexR = 2;
                    rightRBIgnoreFlag = true;
                    binding.rbRSet2.setChecked(true);
                }

            } else if (checkedId == binding.rbLSet3.getId() && binding.rbLSet3.isChecked() ) {
                earType = DeviceManager.EAR_TYPE_LEFT;
                checkedIndexL = 3;
                if (isActionCombined) {
                    checkedIndexR = 3;
                    rightRBIgnoreFlag = true;
                    binding.rbRSet3.setChecked(true);
                }

            // 点击右耳按钮
            } else if (checkedId== binding.rbRSet0.getId() && binding.rbRSet0.isChecked() ) {
                earType = DeviceManager.EAR_TYPE_RIGHT;
                checkedIndexR = 0;
                if (isActionCombined) {
                    checkedIndexL = 0;
                    leftRBIgnoreFlag = true;
                    binding.rbLSet0.setChecked(true);
                }

            } else if (checkedId == binding.rbRSet1.getId() && binding.rbRSet1.isChecked() ) {
                earType = DeviceManager.EAR_TYPE_RIGHT;
                checkedIndexR = 1;
                if (isActionCombined) {
                    checkedIndexL = 1;
                    leftRBIgnoreFlag = true;
                    binding.rbLSet1.setChecked(true);
                }

            } else if (checkedId == binding.rbRSet2.getId() && binding.rbRSet2.isChecked() ) {
                earType = DeviceManager.EAR_TYPE_RIGHT;
                checkedIndexR = 2;
                if (isActionCombined) {
                    checkedIndexL = 2;
                    leftRBIgnoreFlag = true;
                    binding.rbLSet2.setChecked(true);
                }

            } else if (checkedId == binding.rbRSet3.getId() && binding.rbRSet3.isChecked() ) {
                earType = DeviceManager.EAR_TYPE_RIGHT;
                checkedIndexR = 3;
                if (isActionCombined) {
                    checkedIndexL = 3;
                    leftRBIgnoreFlag = true;
                    binding.rbLSet3.setChecked(true);
                }
            }

            binding.tvSet0.setSelected(checkedIndexL == 0 || checkedIndexR == 0);
            binding.tvSet1.setSelected(checkedIndexL == 1 || checkedIndexR == 1);
            binding.tvSet2.setSelected(checkedIndexL == 2 || checkedIndexR == 2);
            binding.tvSet3.setSelected(checkedIndexL == 3 || checkedIndexR == 3);

            if (isActionCombined) {
                setHeardAid(DeviceManager.EAR_TYPE_BOTH, checkedIndexL);
            } else if (earType.equals(DeviceManager.EAR_TYPE_LEFT)) {
                setHeardAid(DeviceManager.EAR_TYPE_LEFT, checkedIndexL);
            } else if (earType.equals(DeviceManager.EAR_TYPE_RIGHT)) {
                setHeardAid(DeviceManager.EAR_TYPE_RIGHT, checkedIndexR);
            }
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
        uiChangeTextView(-1, -1);
    }

    // 在子类重写 Noise 和 Loud 需要获取的 模式文件的内容不同
//    @Override
//    protected void updateModeFile(BTProtocol.ModeFileContent leftContent, BTProtocol.ModeFileContent rightContent) {
//        Log.d(TAG, "updateModeFile leftContent = " + leftContent + " rightContent = " + rightContent);
//        int indexL = -1;
//        int indexR = -1;
//        this.leftContent = leftContent;
//        this.rightContent = rightContent;
//        int cnt = 0;
//        if (leftContent != null) { cnt++; indexL = leftContent.getNoise().ordinal(); }
//        if (rightContent != null) { cnt++; indexR = rightContent.getNoise().ordinal(); }
//        if (cnt != 2) { isActionCombined = false; } // 只获取到了一个模式文件，肯定不需要同步动作
//        this.checkedIndexL = indexL;
//        this.checkedIndexR = indexR;
//        uiChangeTextView(indexL, indexR);
//    }

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