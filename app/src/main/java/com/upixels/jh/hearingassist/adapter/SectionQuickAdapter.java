package com.upixels.jh.hearingassist.adapter;

import android.util.Log;
import android.view.View;

import com.chad.library.adapter.base.BaseSectionQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.upixels.jh.hearingassist.R;
import com.upixels.jh.hearingassist.entity.BLEDeviceEntity;


import org.jetbrains.annotations.NotNull;

import java.util.List;

import me.forrest.commonlib.util.BLEUtil;
import me.forrest.commonlib.view.EasySwipeMenuLayout;

public class SectionQuickAdapter extends BaseSectionQuickAdapter<BLEDeviceEntity, BaseViewHolder> {
    private final static String TAG = SectionQuickAdapter.class.getSimpleName();
    /**
     * Same as QuickAdapter#QuickAdapter(Context,int) but with
     * some initialization data.
     *
     * @param sectionHeadResId The section head layout id for each item
     * @param layoutResId      The layout resource id of each item.
     * @param data             A new list is created out of this one to avoid mutable list
     */
    public SectionQuickAdapter(int layoutResId, int sectionHeadResId, List<BLEDeviceEntity> data) {
        super(sectionHeadResId, data);
        setNormalLayout(layoutResId);
        // 添加需要触发的子控件
//        addChildClickViewIds(R.id.content, R.id.right);
        addChildClickViewIds(R.id.content);
    }

    @Override
    protected void convertHeader(@NotNull BaseViewHolder helper, @NotNull BLEDeviceEntity item) {
        Log.d(TAG, item.toString());
        helper.setText(R.id.tv_item_head, item.header);
    }

    @Override
    protected void convert(@NotNull BaseViewHolder helper, @NotNull BLEDeviceEntity item) {
        Log.d(TAG, "ItemView: " + helper.itemView + " " + helper.getAdapterPosition());
        helper.setText(R.id.tv_device_name, item.deviceName);
//        helper.setText(R.id.tv_device_mac, item.mac);
        ((EasySwipeMenuLayout) helper.getView(R.id.layout_device)).setCanLeftSwipe(false);
        if (item.section == 0) {
            if (item.connectStatus == BLEUtil.STATE_CONNECTED || item.connectStatus == BLEUtil.STATE_GET_GATT_SERVICES_OVER) {
                helper.setText(R.id.tv_device_status, R.string.Connected);
//                helper.getView(R.id.iv_device_status).setVisibility(View.VISIBLE);
//                helper.setText(R.id.right_text, R.string.disconnect);
            } else if (item.connectStatus == BLEUtil.STATE_CONNECTING || item.connectStatus == BLEUtil.STATE_RECONNECTING) {
                helper.setText(R.id.tv_device_status, R.string.Connecting);
//                helper.getView(R.id.iv_device_status).setVisibility(View.GONE);
            } else if (item.connectStatus == BLEUtil.STATE_DISCONNECTING) {
                helper.setText(R.id.tv_device_status, R.string.Disconnecting);
//                helper.getView(R.id.iv_device_status).setVisibility(View.GONE);
            } else if (item.connectStatus == BLEUtil.STATE_DISCONNECTED) {
                helper.setText(R.id.tv_device_status, R.string.tap_to_connect);
//                helper.getView(R.id.iv_device_status).setVisibility(View.GONE);
//                helper.setText(R.id.right_text, R.string.delete);
            }
        }
//        else if (item.section == 1) {
//            helper.setText(R.id.tv_device_status, R.string.Disconnected);
//            helper.getView(R.id.iv_device_status).setVisibility(View.GONE);
//            ((EasySwipeMenuLayout) helper.getView(R.id.layout_device)).setCanLeftSwipe(false);
//        }
    }
}
