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
import android.widget.ImageView;
import android.widget.TextView;

import com.upixels.jh.hearingassist.R;

import me.forrest.commonlib.util.DensityUtil;

public class JHCommonDialog extends Dialog {

    private int resId;
    private ImageView imageIv;
    private TextView tv_title;
    private TextView tv_sub_title;
    private TextView tv_message;
    private TextView btn_left;
    private TextView btn_right;

    private String message;
    private String title;
    private String subTitle;
    private String rightText;
    private String leftText;
    private boolean canceledOnTouchOutside = true;

    public JHCommonDialog(Context context) {
//        super(context, R.style.BaseDialogFragmentStyle);
        super(context);  // 不设置style 宽不能全屏, 但是可以通过 设置 WindowManager.LayoutParams 参数来处理
    }

    public JHCommonDialog(Context context, boolean canceledOnTouchOutside) {
//        super(context, R.style.BaseDialogFragmentStyle);
        super(context);
        this.canceledOnTouchOutside = canceledOnTouchOutside;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = LayoutInflater.from(getContext()).inflate(R.layout.layout_dialog_jh_common, null);
        setContentView(view);

        // 如何想要 setCanceledOnTouchOutside(true) 有效 必须重新设置 Window的大小，
        // 否则整个window通过Activity获取的还是全屏，没有outside可以点击
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.width = DensityUtil.dip2px(getContext(), 270);
        params.height = DensityUtil.dip2px(getContext(), 178);
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
        //设置确定按钮被点击后，向外界提供监听
        btn_right.setOnClickListener(v -> {
            if (onClickBottomListener != null) {
                onClickBottomListener.onRightClick(JHCommonDialog.this);
            }
        });
        //设置取消按钮被点击后，向外界提供监听
        btn_left.setOnClickListener(v -> {
            if (onClickBottomListener != null) {
                onClickBottomListener.onLeftClick(JHCommonDialog.this);
            }
        });
    }

    /**
     * 初始化界面控件的显示数据
     */
    private void refreshView() {
        //如果用户自定了title和message
        if (!TextUtils.isEmpty(title)) {
            tv_title.setText(title);
            tv_title.setVisibility(View.VISIBLE);
        }
        if (!TextUtils.isEmpty(subTitle)) {
            tv_sub_title.setText(subTitle);
        }
        if (!TextUtils.isEmpty(message)) {
            tv_message.setText(message);
        }
        if (!TextUtils.isEmpty(rightText)) {
            btn_right.setText(rightText);
        }
        if (!TextUtils.isEmpty(leftText)) {
            btn_left.setText(leftText);
        }
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
        btn_left = findViewById(R.id.btn_left);
        btn_right = findViewById(R.id.btn_right);
        tv_title = findViewById(R.id.tv_title);
        tv_sub_title = findViewById(R.id.tv_sub_title);
        tv_message = findViewById(R.id.tv_message);
        imageIv = findViewById(R.id.image);
    }

    /**
     * 设置确定取消按钮的回调
     */
    private OnClickBottomListener onClickBottomListener;

    public JHCommonDialog setOnClickBottomListener(OnClickBottomListener onClickBottomListener) {
        this.onClickBottomListener = onClickBottomListener;
        return this;
    }



    public interface OnClickBottomListener {
        /**
         * 点击确定按钮事件
         */
        void onRightClick(Dialog dialog);

        /**
         * 点击取消按钮事件
         */
        void onLeftClick(Dialog dialog);
    }

    public JHCommonDialog setTitle(String title) {
        this.title = title;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public JHCommonDialog setSubTitle(String subTitle) {
        this.subTitle = subTitle;
        return this;
    }

    public String getSubTitle() {
        return subTitle;
    }

    public String getMessage() {
        return message;
    }

    public JHCommonDialog setMessage(String message) {
        this.message = message;
        return this;
    }

    public JHCommonDialog setRightText(String rightText) {
        this.rightText = rightText;
        return this;
    }

    public JHCommonDialog setLeftText(String leftText) {
        this.leftText = leftText;
        return this;
    }
}

