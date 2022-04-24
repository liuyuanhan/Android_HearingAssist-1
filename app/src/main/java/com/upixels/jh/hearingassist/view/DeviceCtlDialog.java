package com.upixels.jh.hearingassist.view;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.upixels.jh.hearingassist.R;

import me.forrest.commonlib.util.DensityUtil;

public class DeviceCtlDialog extends Dialog {

    private int resId;
    private String message;
    private String title;
    private boolean canceledOnTouchOutside = true;

    public DeviceCtlDialog(Context context) {
//        super(context, R.style.BaseDialogFragmentStyle);
        super(context);  // 不设置style 宽不能全屏, 但是可以通过 设置 WindowManager.LayoutParams 参数来处理
    }

    public DeviceCtlDialog(Context context, int resId, boolean canceledOnTouchOutside) {
//        super(context, R.style.BaseDialogFragmentStyle);
        super(context);
        this.resId = resId;
        this.canceledOnTouchOutside = canceledOnTouchOutside;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int id = this.resId == 0 ? R.layout.dialog_device_ctl : resId;
        View view = LayoutInflater.from(getContext()).inflate(id, null);
        setContentView(view);
        // 如何想要 setCanceledOnTouchOutside(true) 有效 必须重新设置 Window的大小，
        // 否则整个window通过Activity获取的还是全屏，没有outside可以点击
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.width = DensityUtil.dip2px(getContext(), 330);
        params.height = DensityUtil.dip2px(getContext(), 250);
        getWindow().setAttributes(params);
        getWindow().setGravity(Gravity.BOTTOM);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        getWindow().setWindowAnimations(me.forrest.commonlib.R.style.dialog_anim_bottom_in_out);
        //按空白处不能取消动画
        setCanceledOnTouchOutside(this.canceledOnTouchOutside);
        initView();
        //初始化界面数据
        refreshView();
        //初始化界面控件的事件
        initEvent();
    }

    /**
     * 初始化界面的确定和取消监听器
     */
    private void initEvent() {
        findViewById(R.id.tv_dialog_disconnect).setOnClickListener(v -> {
            if (onClickListener != null) { onClickListener.onDisconnectClick(this); }
        });
        findViewById(R.id.tv_dialog_rename).setOnClickListener(v -> {
            if (onClickListener != null) { onClickListener.onRenameClick(this); }
        });
        findViewById(R.id.tv_dialog_remove).setOnClickListener(v -> {
            if (onClickListener != null) { onClickListener.onRemoveClick(this); }
        });
        findViewById(R.id.tv_dialog_cancel).setOnClickListener(v -> {
            if (onClickListener != null) { onClickListener.onCancelClick(this); }
        });
    }

    /**
     * 初始化界面控件的显示数据
     */
    public void refreshView() {
        ((TextView)findViewById(R.id.tv_dialog_title)).setText(title);
    }

    @Override
    public void show() {
        super.show();
        refreshView();
    }

    /**
     * 初始化界面控件
     */
    private void initView() {

    }

    /**
     * 设置确定取消按钮的回调
     */
    private OnClickListener onClickListener;

    public DeviceCtlDialog setOnClickListener(OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
        return this;
    }

    public interface OnClickListener {
        void onDisconnectClick(DeviceCtlDialog dialog);
        void onRenameClick(DeviceCtlDialog dialog);
        void onRemoveClick(DeviceCtlDialog dialog);
        void onCancelClick(DeviceCtlDialog dialog);
    }

    public String getTitle() {
        return title;
    }

    public DeviceCtlDialog setTitle(String title) {
        this.title = title;
        return this;
    }

}

