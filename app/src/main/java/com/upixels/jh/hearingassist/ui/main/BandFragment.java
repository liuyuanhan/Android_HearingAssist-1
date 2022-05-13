package com.upixels.jh.hearingassist.ui.main;

import android.annotation.SuppressLint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;

import com.upixels.jh.hearingassist.R;
import com.upixels.jh.hearingassist.databinding.FragmentBandBinding;
import com.upixels.jh.hearingassist.databinding.FragmentBandLeftBinding;
import com.upixels.jh.hearingassist.databinding.FragmentBandRightBinding;
import com.upixels.jh.hearingassist.util.DeviceManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.AppCompatSeekBar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.transition.TransitionManager;
import me.forrest.commonlib.jh.BTProtocol;
import me.forrest.commonlib.jh.SceneMode;
import me.forrest.commonlib.util.CommonUtil;
import me.forrest.commonlib.util.DensityHelper;

public class BandFragment extends BaseFragment implements View.OnTouchListener {
    private final static String TAG = BandFragment.class.getSimpleName();
    private FragmentBandBinding         binding;
    private FragmentBandLeftBinding     leftBinding;
    private FragmentBandRightBinding    rightBinding;
    private ConstraintLayout            layoutLeftRight;
    private ConstraintLayout            layoutLeft;
    private ConstraintLayout            layoutRight;
    private ConstraintSet               constraintSetLeftRight;
    private ConstraintSet               constraintSetLeft;
    private ConstraintSet               constraintSetRight;
    private int                         constraintSetFlag = 0; // 0 , 1, 2 防止重复切换


    private BTProtocol.ModeFileContent leftContent;
    private BTProtocol.ModeFileContent rightContent;
    private BTProtocol.ModeFileContent curContent;

    public static BandFragment newInstance() {
        return new BandFragment();
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
        binding = FragmentBandBinding.inflate(inflater, container, false);
        leftBinding = FragmentBandLeftBinding.inflate(inflater, container, false);
        rightBinding = FragmentBandRightBinding.inflate(inflater, container, false);
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

    @SuppressLint("ClickableViewAccessibility")
    private void initView() {
        binding.sbEq250.setOnTouchListener(this);
        binding.sbEq500.setOnTouchListener(this);
        binding.sbEq1000.setOnTouchListener(this);
        binding.sbEq1500.setOnTouchListener(this);
        binding.sbEq2000.setOnTouchListener(this);
        binding.sbEq3000.setOnTouchListener(this);
        binding.sbEq4000.setOnTouchListener(this);
        binding.sbEq6000.setOnTouchListener(this);

        binding.sbEq250.setOnSeekBarChangeListener(changeListener);
        binding.sbEq500.setOnSeekBarChangeListener(changeListener);
        binding.sbEq1000.setOnSeekBarChangeListener(changeListener);
        binding.sbEq1500.setOnSeekBarChangeListener(changeListener);
        binding.sbEq2000.setOnSeekBarChangeListener(changeListener);
        binding.sbEq3000.setOnSeekBarChangeListener(changeListener);
        binding.sbEq4000.setOnSeekBarChangeListener(changeListener);
        binding.sbEq6000.setOnSeekBarChangeListener(changeListener);

        constraintSetLeftRight = new ConstraintSet();
        constraintSetLeft = new ConstraintSet();
        constraintSetRight = new ConstraintSet();

        layoutLeftRight = binding.layoutBandLR;
        constraintSetLeftRight.clone(layoutLeftRight);

        layoutLeft = leftBinding.layoutBandL;
        constraintSetLeft.clone(layoutLeft);

        layoutRight = rightBinding.layoutBandR;
        constraintSetRight.clone(layoutRight);

        binding.btnAudition.setOnClickListener(l -> {
//            if (cnt == 0) {
//                uiChangeLR(2, DeviceManager.EAR_TYPE_LEFT);
//                cnt++;
//            } else if (cnt == 1) {
//                uiChangeLR(2, DeviceManager.EAR_TYPE_RIGHT);
//                cnt++;
//            } else if (cnt == 2) {
//                uiChangeLR(0, DeviceManager.EAR_TYPE_RIGHT);
//                cnt++;
//            } else if (cnt == 3) {
//                uiChangeLR(1, DeviceManager.EAR_TYPE_LEFT);
//                cnt++;
//            } else if (cnt == 4) {
//                uiChangeLR(1, DeviceManager.EAR_TYPE_RIGHT);
//                cnt = 0;
//            }
            Log.d(TAG, "curContent " + curContent.toString());
        });
    }
    int cnt = 0;

    // UI更新 Seekbar
    private void uiChangeSetSeekbarEnable(boolean enable, BTProtocol.ModeFileContent content) {
        binding.sbEq250.setEnabled(enable);
        binding.sbEq500.setEnabled(enable);
        binding.sbEq1000.setEnabled(enable);
        binding.sbEq1500.setEnabled(enable);
        binding.sbEq2000.setEnabled(enable);
        binding.sbEq3000.setEnabled(enable);
        binding.sbEq4000.setEnabled(enable);
        binding.sbEq6000.setEnabled(enable);
        curContent = null;
        if (enable) {
            curContent = content;
            Drawable drawable;
            Rect bounds250 = binding.sbEq250.getProgressDrawable().getBounds();
            int resId = 0;
            if (content.mode.getDeviceName().contains("-L")) {
                resId = R.drawable.seekbar_band_l;
            } else {
                resId = R.drawable.seekbar_band_r;
            }

            binding.sbEq250.setProgressDrawable(AppCompatResources.getDrawable(requireContext(), resId));
            binding.sbEq250.getProgressDrawable().setBounds(bounds250);
            binding.sbEq250.setProgress(content.EQ1);
            binding.tvGain250.setText(String.valueOf(BTProtocol.ModeFileContent.EQ2Value(content.EQ1)));

            binding.sbEq500.setProgressDrawable(AppCompatResources.getDrawable(requireContext(), resId));
            binding.sbEq500.getProgressDrawable().setBounds(bounds250);
            binding.sbEq500.setProgress(content.EQ2);
            binding.tvGain500.setText(String.valueOf(BTProtocol.ModeFileContent.EQ2Value(content.EQ2)));

            binding.sbEq1000.setProgressDrawable(AppCompatResources.getDrawable(requireContext(), resId));
            binding.sbEq1000.getProgressDrawable().setBounds(bounds250);
            binding.sbEq1000.setProgress(content.EQ3);
            binding.tvGain1000.setText(String.valueOf(BTProtocol.ModeFileContent.EQ2Value(content.EQ3)));

            binding.sbEq1500.setProgressDrawable(AppCompatResources.getDrawable(requireContext(), resId));
            binding.sbEq1500.getProgressDrawable().setBounds(bounds250);
            binding.sbEq1500.setProgress(content.EQ4);
            binding.tvGain1500.setText(String.valueOf(BTProtocol.ModeFileContent.EQ2Value(content.EQ4)));

            binding.sbEq2000.setProgressDrawable(AppCompatResources.getDrawable(requireContext(), resId));
            binding.sbEq2000.getProgressDrawable().setBounds(bounds250);
            binding.sbEq2000.setProgress(content.EQ5);
            binding.tvGain2000.setText(String.valueOf(BTProtocol.ModeFileContent.EQ2Value(content.EQ5)));

            binding.sbEq3000.setProgressDrawable(AppCompatResources.getDrawable(requireContext(), resId));
            binding.sbEq3000.getProgressDrawable().setBounds(bounds250);
            binding.sbEq3000.setProgress(content.EQ7);
            binding.tvGain3000.setText(String.valueOf(BTProtocol.ModeFileContent.EQ2Value(content.EQ7)));

            binding.sbEq4000.setProgressDrawable(AppCompatResources.getDrawable(requireContext(), resId));
            binding.sbEq4000.getProgressDrawable().setBounds(bounds250);
            binding.sbEq4000.setProgress(content.EQ9);
            binding.tvGain4000.setText(String.valueOf(BTProtocol.ModeFileContent.EQ2Value(content.EQ9)));

            binding.sbEq6000.setProgressDrawable(AppCompatResources.getDrawable(requireContext(), resId));
            binding.sbEq6000.getProgressDrawable().setBounds(bounds250);
            binding.sbEq6000.setProgress(content.EQ11);
            binding.tvGain6000.setText(String.valueOf(BTProtocol.ModeFileContent.EQ2Value(content.EQ11)));
        }
    }

    // 改变 L R 按钮的UI状态
    private void uiChangeLR(int connectCnt, String earType) {
        if (connectCnt == 0) {
            if (constraintSetFlag != 0) {
                constraintSetFlag = 0;
                constraintSetLeftRight.applyTo(layoutLeftRight);
                TransitionManager.beginDelayedTransition(layoutLeftRight);
                binding.viewBgLR.setBackgroundResource(R.drawable.shape_view_bg_l_r); // ConstrainSet 只能改变约束不能改变背景颜色
            }
            binding.ivL.setBackground(null);
            binding.ivR.setBackground(null);
            binding.ivL.setOnClickListener(null); // 未连接不可点击
            binding.ivR.setOnClickListener(null);

        } else if (connectCnt == 2 && earType.equals(DeviceManager.EAR_TYPE_LEFT)) {
            if (constraintSetFlag != 0) {
                constraintSetFlag = 0;
                constraintSetLeftRight.applyTo(layoutLeftRight);
                TransitionManager.beginDelayedTransition(layoutLeftRight);
                binding.viewBgLR.setBackgroundResource(R.drawable.shape_view_bg_l_r);
                binding.ivL.setBackgroundResource(R.drawable.selector_band_drawable_l);
                binding.ivR.setBackgroundResource(R.drawable.selector_band_drawable_r);
            }
            binding.ivL.setOnClickListener(lrListener);
            binding.ivR.setOnClickListener(lrListener);
            binding.ivL.setSelected(true);
            binding.ivR.setSelected(false);


        } else if (connectCnt == 2 && earType.equals(DeviceManager.EAR_TYPE_RIGHT)) {
            if (constraintSetFlag != 0) {
                constraintSetFlag = 0;
                constraintSetLeftRight.applyTo(layoutLeftRight);
                TransitionManager.beginDelayedTransition(layoutLeftRight);
                binding.viewBgLR.setBackgroundResource(R.drawable.shape_view_bg_l_r);
                binding.ivL.setBackgroundResource(R.drawable.selector_band_drawable_l);
                binding.ivR.setBackgroundResource(R.drawable.selector_band_drawable_r);
            }
            binding.ivL.setOnClickListener(lrListener);
            binding.ivR.setOnClickListener(lrListener);
            binding.ivL.setSelected(false);
            binding.ivR.setSelected(true);
        }

        else if (connectCnt == 1 && earType.equals(DeviceManager.EAR_TYPE_LEFT)) {
            if (constraintSetFlag != 1) {
                constraintSetFlag = 1;
                constraintSetLeft.applyTo(layoutLeftRight);
                TransitionManager.beginDelayedTransition(layoutLeftRight);
                binding.viewBgLR.setBackgroundResource(R.drawable.shape_device_l);
                binding.ivL.setBackground(null);
            }
            binding.ivL.setOnClickListener(null); // 单个设备 也不需要点击
            binding.ivR.setOnClickListener(null);

        } else if (connectCnt == 1 && earType.equals(DeviceManager.EAR_TYPE_RIGHT)) {
            if (constraintSetFlag != 2) {
                constraintSetFlag = 2;
                constraintSetRight.applyTo(layoutLeftRight);
                TransitionManager.beginDelayedTransition(layoutLeftRight);
                binding.viewBgLR.setBackgroundResource(R.drawable.shape_device_l_r);
                binding.ivR.setBackground(null);
            }
            binding.ivL.setOnClickListener(null); // 单个设备 也不需要点击
            binding.ivR.setOnClickListener(null);
        }
    }

    private View.OnClickListener lrListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.iv_L) {
                binding.ivL.setSelected(true);
                binding.ivR.setSelected(false);
            } else if (v.getId() == R.id.iv_R) {
                binding.ivL.setSelected(false);
                binding.ivR.setSelected(true);
            }
        }
    };

    private SeekBar.OnSeekBarChangeListener changeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            Log.d(TAG, "onProgressChanged progress = " + progress);
            if (seekBar.getId() == R.id.sb_eq_250) {
                curContent.EQ1 = (byte) progress;
                binding.tvGain250.setText(String.valueOf(BTProtocol.ModeFileContent.EQ2Value((byte) progress)));
            } else if (seekBar.getId() == R.id.sb_eq_500) {
                curContent.EQ2 = (byte) progress;
                binding.tvGain500.setText(String.valueOf(BTProtocol.ModeFileContent.EQ2Value((byte)progress)));
            } else if (seekBar.getId() == R.id.sb_eq_1000) {
                curContent.EQ3 = (byte) progress;
                binding.tvGain1000.setText(String.valueOf(BTProtocol.ModeFileContent.EQ2Value((byte)progress)));
            } else if (seekBar.getId() == R.id.sb_eq_1500) {
                curContent.EQ4 = (byte) progress;
                binding.tvGain1500.setText(String.valueOf(BTProtocol.ModeFileContent.EQ2Value((byte)progress)));
            } else if (seekBar.getId() == R.id.sb_eq_2000) {
                curContent.EQ5 = (byte) progress;
                binding.tvGain2000.setText(String.valueOf(BTProtocol.ModeFileContent.EQ2Value((byte)progress)));
            } else if (seekBar.getId() == R.id.sb_eq_3000) {
                curContent.EQ7 = (byte) progress;
                binding.tvGain3000.setText(String.valueOf(BTProtocol.ModeFileContent.EQ2Value((byte)progress)));
            } else if (seekBar.getId() == R.id.sb_eq_4000) {
                curContent.EQ9 = (byte) progress;
                binding.tvGain4000.setText(String.valueOf(BTProtocol.ModeFileContent.EQ2Value((byte)progress)));
            } else if (seekBar.getId() == R.id.sb_eq_6000) {
                curContent.EQ11 = (byte) progress;
                binding.tvGain6000.setText(String.valueOf(BTProtocol.ModeFileContent.EQ2Value((byte)progress)));
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    };

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                // Disallow ScrollView to intercept touch events.
                v.getParent().requestDisallowInterceptTouchEvent(true);
                break;

            case MotionEvent.ACTION_UP:
                // Allow ScrollView to intercept touch events.
                v.getParent().requestDisallowInterceptTouchEvent(false);
                break;
        }
        v.onTouchEvent(event);
        return true;
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
            binding.ivModeL.setVisibility(View.VISIBLE);
            binding.ivModeR.setVisibility(View.VISIBLE);
            binding.ivModeL.setImageResource(resIdL);
            binding.ivModeR.setImageResource(resIdR);
            if (leftMode != rightMode) {
                CommonUtil.showToastLong(requireActivity(), getString(R.string.tips_mode_not_same));
                uiChangeLR(0,null);
                uiChangeSetSeekbarEnable(false, null);
                return;
            }
            DeviceManager.getInstance().readModeFile(leftMode);

        } else if (resIdL > 0) {
            binding.ivModeL.setImageResource(resIdL);
            binding.ivModeR.setVisibility(View.INVISIBLE);
            DeviceManager.getInstance().readModeFile(leftMode);

        } else if (resIdR > 0) {
            binding.ivModeL.setVisibility(View.INVISIBLE);
            binding.ivModeR.setImageResource(resIdR);
            DeviceManager.getInstance().readModeFile(rightMode);
        }
        uiChangeLR(0,null);
        uiChangeSetSeekbarEnable(false, null);
    }

    @Override
    protected void updateModeFile(BTProtocol.ModeFileContent leftContent, BTProtocol.ModeFileContent rightContent) {
        Log.d(TAG, "updateModeFile leftContent = " + leftContent + " rightContent = " + rightContent);
        this.leftContent = leftContent;
        this.rightContent = rightContent;
        int cnt = 0;
        if (leftContent != null) { cnt++; }
        if (rightContent != null) { cnt++; }
        if (leftContent != null) {
            uiChangeLR(cnt, DeviceManager.EAR_TYPE_LEFT);
            uiChangeSetSeekbarEnable(true, leftContent);
        } else if (rightContent != null) {
            uiChangeLR(cnt, DeviceManager.EAR_TYPE_RIGHT);
            uiChangeSetSeekbarEnable(true, rightContent);
        } else {
            uiChangeLR(0, null);
        }
    }
}