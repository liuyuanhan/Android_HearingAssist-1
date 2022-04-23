package com.upixels.jh.hearingassist.view;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;


import com.upixels.jh.hearingassist.R;

import me.forrest.commonlib.util.DensityUtil;

public class LocServiceDialog extends Dialog {

    private int resId;
    private ImageView imageIv;
    private TextView tv_title;
    private TextView tv_sub_title;
    private TextView tv_message;
    private Button btn_confirm;
    private Button btn_cancel;

    private String message;
    private String title;
    private String btnText;
    private boolean canceledOnTouchOutside = true;

    public LocServiceDialog(Context context) {
//        super(context, R.style.BaseDialogFragmentStyle);
        super(context);  // 不设置style 宽不能全屏, 但是可以通过 设置 WindowManager.LayoutParams 参数来处理
    }

    public LocServiceDialog(Context context, int resId, boolean canceledOnTouchOutside) {
//        super(context, R.style.BaseDialogFragmentStyle);
        super(context);
        this.resId = resId;
        this.canceledOnTouchOutside = canceledOnTouchOutside;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int id = this.resId == 0 ? R.layout.dialog_loc_service : resId;
        View view = LayoutInflater.from(getContext()).inflate(id, null);
        setContentView(view);
        // 如何想要 setCanceledOnTouchOutside(true) 有效 必须重新设置 Window的大小，
        // 否则整个window通过Activity获取的还是全屏，没有outside可以点击
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.width = DensityUtil.dip2px(getContext(), 260);
        params.height = DensityUtil.dip2px(getContext(), 220);
        getWindow().setAttributes(params);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
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
        btn_confirm.setOnClickListener(v -> {
            if (onClickListener != null) {
                onClickListener.onConfirmClick(this);
            }
        });

        btn_cancel.setOnClickListener(v -> {
            if (onClickListener != null) {
                onClickListener.onCancelClick(this);
            }
        });
    }

    /**
     * 初始化界面控件的显示数据
     */
    public void refreshView() {
        //如果用户自定了title和message
        if (!TextUtils.isEmpty(title)) {
            tv_title.setText(title);
            tv_title.setVisibility(View.VISIBLE);
        }
        if (!TextUtils.isEmpty(message)) {
            tv_message.setText(message);
        }
//        if (!TextUtils.isEmpty(btnText)) {
//            btn_confirm.setText(btnText);
//        }
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
        btn_confirm = findViewById(R.id.btn_confirm);
        btn_cancel  = findViewById(R.id.btn_cancel);
        tv_title    = findViewById(R.id.tv_title);
        tv_message  = findViewById(R.id.tv_message);
    }

    /**
     * 设置确定取消按钮的回调
     */
    private OnClickListener onClickListener;

    public LocServiceDialog setOnClickListener(OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
        return this;
    }

    public interface OnClickListener {
        /**
         * 点击确定按钮事件
         */
        void onConfirmClick(LocServiceDialog dialog);

        void onCancelClick(LocServiceDialog dialog);
    }

    public void hideBtnConfirm() {
        this.btn_confirm.setVisibility(View.INVISIBLE);
    }

    public void showBtnConfirm() {
        this.btn_confirm.setVisibility(View.VISIBLE);
    }

    public String getMessage() {
        return message;
    }

    public LocServiceDialog setMessage(String message) {
        this.message = message;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public LocServiceDialog setTitle(String title) {
        this.title = title;
        return this;
    }

//    public AppUpdateInfoDialog setBtnText(String text) {
//        this.btnText = text;
//        return this;
//    }

}

