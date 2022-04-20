package me.forrest.commonlib.view;

import android.view.View;

/**
 * 防止连击 间隔 MIN_CLICK_DELAY_TIME 处理一次点击事件
 */
public abstract class OnMultiClickListener implements View.OnClickListener {

    private static final int MIN_CLICK_DELAY_TIME = 1000;
    private long lastClickTime = 0;

    @Override
    public void onClick(View v) {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastClickTime > MIN_CLICK_DELAY_TIME) {
            lastClickTime = currentTime;
            onMultiClick(v);
        }
    }

   public abstract void onMultiClick(View v);
}