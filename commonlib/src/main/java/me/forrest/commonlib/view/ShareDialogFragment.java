package me.forrest.commonlib.view;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import me.forrest.commonlib.R;

public class ShareDialogFragment extends BottomSheetDialogFragment implements View.OnClickListener {

    private SubmitListener submitListener;
    private boolean isShowing = false; //是否已经显示

    public void setSubmitListener(SubmitListener listener) {
        this.submitListener = listener;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_FRAME, R.style.BaseDialogFragmentStyle); // 无框
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);//取消标题
        getDialog().getWindow().setWindowAnimations(R.style.dialog_anim_bottom_in_out);
        setTranslucentStatus();
        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        View contentView = inflater.inflate(R.layout.layout_dialog_share, container);
        contentView.findViewById(R.id.layout_friends).setOnClickListener(this);
        contentView.findViewById(R.id.layout_qq).setOnClickListener(this);
        contentView.findViewById(R.id.layout_wechat).setOnClickListener(this);
        contentView.findViewById(R.id.layout_weibo).setOnClickListener(this);
        contentView.findViewById(R.id.tv_cancel).setOnClickListener(this);
        return contentView;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    private void setTranslucentStatus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {//5.0 全透明实现
            Window window = getDialog().getWindow();
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
        } else {//4.4 全透明状态栏
            getDialog().getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
    }

    /**
     * 避免Fragment already added
     *
     * @param manager
     * @param tag
     */
    @Override
    public void show(FragmentManager manager, String tag) {
        Fragment fragment = manager.findFragmentByTag(tag);
        if (!isShowing && (fragment == null || !fragment.isAdded())) {
            super.show(manager, tag);
            isShowing = true;
        }
    }

    public void show(FragmentManager manager) {
        String tag = getClass().getSimpleName();
        show(manager,tag);
    }

    @Override
    public void dismiss() {
        if (isShowing) {
            super.dismiss();
            isShowing = false;
        }
    }

    @Override
    public void onClick(View v) {
        if (this.submitListener != null) {
            this.submitListener.onSubmit(v.getId());
        }
        dismiss();
    }
}
