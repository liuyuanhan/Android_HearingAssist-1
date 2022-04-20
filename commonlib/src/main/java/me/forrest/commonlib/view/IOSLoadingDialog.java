package me.forrest.commonlib.view;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import java.lang.reflect.Field;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import me.forrest.commonlib.R;

public class IOSLoadingDialog extends DialogFragment implements DialogInterface.OnKeyListener, DialogInterface.OnDismissListener {

    private boolean isShow = false;
    public final static IOSLoadingDialog instance = new IOSLoadingDialog();

    public synchronized void showDialog(FragmentManager manager, String msg) {
        if (isAdded() || isShow) return;
        instance.setHintMsg(msg);
        instance.show(manager, "IOSLoadingDialog");
        isShow = true;
    }

    public synchronized void dismissDialog() {
        if (isShow) { instance.dismiss(); }
        isShow = false;
    }

    private IOSLoadingDialog() {}

    /**
     * 默认点击外面无效
     */
    private boolean onTouchOutside = false;

    /**
     * 加载框提示信息 设置默认
     */
    private String hintMsg = "正在加载";

    /**
     * 设置是否允许点击外面取消
     * @param onTouchOutside
     * @return
     */
    public IOSLoadingDialog setOnTouchOutside(boolean onTouchOutside) {
        this.onTouchOutside = onTouchOutside;
        return this;
    }

    /**
     * 设置加载框提示信息
     * @param hintMsg
     */
    public IOSLoadingDialog setHintMsg(String hintMsg) {
        this.hintMsg = hintMsg;
        return this;
    }

    /**
     * 利用反射区调用DialogFragment的commitAllowingStateLoss()方法
     * @param manager
     * @param tag
     */
    public void showAllowingStateLoss(FragmentManager manager, String tag){
        try {
            Field dismissed = DialogFragment.class.getDeclaredField("mDismissed");
            dismissed.setAccessible(true);
            dismissed.set(this, false);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
        try {
            Field shown = DialogFragment.class.getDeclaredField("mShownByMe");
            shown.setAccessible(true);
            shown.set(this, true);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
        FragmentTransaction ft = manager.beginTransaction();
        ft.add(this, tag);
        ft.commitAllowingStateLoss();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Dialog dialog = getDialog();
        // 设置背景透明
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        // 去掉标题
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCanceledOnTouchOutside(onTouchOutside);

        View loadingView = inflater.inflate(R.layout.ios_dialog_loading, container);
        TextView hintTextView = loadingView.findViewById(R.id.tv_ios_loading_dialog_hint);
        if (!hintMsg.isEmpty()) {
            hintTextView.setText(hintMsg);
            hintTextView.setVisibility(View.VISIBLE);
        } else {
            hintTextView.setVisibility(View.GONE);
        }
        //不响应返回键
        dialog.setOnKeyListener(this);
        dialog.setOnDismissListener(this);
        return loadingView;
    }

    @Override
    public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return true;
        }
        return false;
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        isShow = false;
    }
}
