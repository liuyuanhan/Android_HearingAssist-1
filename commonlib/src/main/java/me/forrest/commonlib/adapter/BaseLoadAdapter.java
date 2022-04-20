package me.forrest.commonlib.adapter;

import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import me.forrest.commonlib.log.LogTool;

public abstract class BaseLoadAdapter extends BaseAdapter {

    private boolean isLoading = false;   // 是否在正在加载
    private boolean loadEnable = true;   // 是否可以加载
    RecyclerListener.OnLoadListener onLoadListener;

    public void setOnLoadListener(RecyclerListener.OnLoadListener onLoadListener) {
        this.onLoadListener = onLoadListener;
    }

    public boolean isLoading() {
        return isLoading;
    }

    /**
     * 设置是否可以加载
     * @param enable
     */
    public void setLoadEnable(boolean enable) {
        this.loadEnable = enable;
        isLoading = false;
    }

    // 设置加载完成
    public void finishLoadMore() {
        isLoading = false;
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
        startLoadMore(recyclerView, layoutManager);
    }

    /**
     * 判断列表是否滑动到底部
     */
    private void startLoadMore(RecyclerView recyclerView, final RecyclerView.LayoutManager layoutManager) {

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
//                    isOnReachBottom();
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                isOnReachBottom();
            }
        });
    }

    private void isOnReachBottom() {
        int l = recyclerView.computeVerticalScrollExtent() + recyclerView.computeVerticalScrollOffset();
        int r = recyclerView.computeVerticalScrollRange();
        int h = recyclerView.getHeight();
        LogTool.debug(String.format("[BaseLoadAdapter] l(%d) r(%d) h(%d)", l, r, h));
        if (recyclerView != null && l >= r && l >= h) {
            scrollLoadMore();
        }
    }

    /**
     * 到达底部开始加载更多
     */
    private void scrollLoadMore() {
        if (null == onLoadListener) {
            return;
        }
        if (!loadEnable) {
            return;
        }
        if (isLoading) {
            return;
        }
        isLoading = true;
        new Handler().postDelayed(() -> {
            onLoadListener.onLoad();  //  延迟100毫秒执行用于解决bug 一开始不执行方法 点击屏幕再执行
        }, 100);
    }
}
