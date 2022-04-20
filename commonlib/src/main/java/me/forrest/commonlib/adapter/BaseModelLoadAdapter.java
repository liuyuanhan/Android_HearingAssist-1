package me.forrest.commonlib.adapter;

import android.graphics.Color;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import me.forrest.commonlib.R;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public abstract class BaseModelLoadAdapter<T> extends BaseLoadAdapter {
    protected List<T> list = new ArrayList<>();
    protected @LayoutRes
    int layoutId;
    public static final int TYPE_FOOT_ERROR = -4;
    public static final int TYPE_FOOT_EMPTY = -3;
    public static final int TYPE_FOOT_LOAD = -2;
    public static final int TYPE_EMPTY = -1;
    public static final int TYPE_ITEM = 0;
    protected int foot_type = TYPE_FOOT_EMPTY;

    public List<T> getList() {
        return list;
    }

    public BaseModelLoadAdapter(int layoutId) {
        this.layoutId = layoutId;
    }

    public BaseModelLoadAdapter(List<T> list, int layoutId) {
        this.layoutId = layoutId;
        if (null != list && list.size() != 0) {
            Iterator<T> it = list.iterator();
            while (it.hasNext()) {
                if (null == it.next()) {
                    it.remove();
                }
            }
            this.list.addAll(list);
        } else {
            setTypeEmpty();
        }
    }

    public void refurbish(List<T> list) {
        this.list.clear();
        if (list == null || list.size() == 0) {
//            setFootEmpty();
            scheduleLayoutAnimation();
            return;
        }
        // 去除空对象
        Iterator<T> it = list.iterator();
        while (it.hasNext()) {
            if (null == it.next()) {
                it.remove();
            }
        }

        this.list.addAll(list);
        setFootEmpty();
        scheduleLayoutAnimation();
//        isOnReachBottom();
    }

    public void addList(List<T> list) {
        if (list == null || list.size() == 0) {
            setFootEmpty();
            notifyItemChanged(getItemCount());
            return;
        }
        Iterator<T> it = list.iterator();
        while (it.hasNext()) {
            if (null == it.next()) {
                it.remove();
            }
        }
        this.list.addAll(list);
        setFootEmpty();
        notifyItemChanged(getItemCount());
    }

    public void setTypeEmpty() {
        foot_type = TYPE_EMPTY;
    }

    public void setFootError() {
        foot_type = TYPE_FOOT_ERROR;
        notifyItemChanged(getItemCount());
    }

    public void setFootEmpty() {
        foot_type = TYPE_FOOT_EMPTY;
    }

    public void setFootLoading() {
        foot_type = TYPE_FOOT_LOAD;
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType) {
            case TYPE_FOOT_ERROR:
                return onCreateFootErrorViewHolder(parent, viewType);
            case TYPE_FOOT_EMPTY:
                return onCreateFootEmptyViewHolder(parent, viewType);
            case TYPE_FOOT_LOAD:
                return onCreateFootLoadViewHolder(parent, viewType);
            case TYPE_EMPTY:
                return onCreateEmptyViewHolder(parent, viewType);
            case TYPE_ITEM:
                return onCreateItemViewHolder(parent, viewType);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        switch (getItemViewType(position)) {
            case TYPE_FOOT_ERROR:
                onBindFootErrorViewHolder((BaseHolder) holder, position);
                break;
            case TYPE_FOOT_EMPTY:
                onBindFootEmptyViewHolder((BaseHolder) holder, position);
                break;
            case TYPE_FOOT_LOAD:
                onBindFootLoadViewHolder((BaseHolder) holder, position);
                break;
            case TYPE_EMPTY:
                onBindEmptyViewHolder((BaseHolder) holder, position);
                break;
            case TYPE_ITEM:
                onBindItemViewHolder((BaseHolder) holder, position);
                break;
        }
    }

    protected abstract void onBindItemViewHolder(@NonNull BaseHolder holder, int position);


    protected void onBindEmptyViewHolder(@NonNull BaseHolder holder, int position) {
    }

    protected void onBindFootErrorViewHolder(@NonNull BaseHolder holder, int position) {
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onLoadListener != null) {
                    setFootLoading();
                    notifyItemChanged(getItemCount());
                    onLoadListener.onLoad();
                }
            }
        });
    }

    protected void onBindFootLoadViewHolder(@NonNull BaseHolder holder, int position) {
    }

    protected void onBindFootEmptyViewHolder(@NonNull BaseHolder holder, int position) {
    }

    @Override
    public int getItemCount() {
        if (list.size() == 0) {
            return 1;
        } else {
            return list.size() + 1;
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (list.size() == 0) {
            return foot_type;
        } else {
            if (position == getItemCount() - 1) {
                return foot_type;
            } else {
                return TYPE_ITEM;
            }
        }
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        final RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
        if (layoutManager instanceof GridLayoutManager) {
            final GridLayoutManager gridManager = ((GridLayoutManager) layoutManager);
            gridManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {
                    if (getItemViewType(position) == TYPE_EMPTY || getItemViewType(position) == TYPE_FOOT_ERROR ||
                            getItemViewType(position) == TYPE_FOOT_EMPTY || getItemViewType(position) == TYPE_FOOT_LOAD) {
                        return gridManager.getSpanCount();
                    }
                    return 1;
                }
            });
        }
    }

    protected BaseHolder onCreateItemViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(layoutId, parent, false);
        return new BaseHolder(view);
    }

    protected BaseHolder onCreateEmptyViewHolder(@NonNull ViewGroup parent, int viewType) {
        TextView textView = new TextView(context);
        textView.setText("没有数据哦");
        textView.setTextSize(16);
        textView.setTextColor(Color.parseColor("#333333"));
        textView.setGravity(Gravity.CENTER);
        textView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        return new BaseHolder(textView);
    }

    protected BaseHolder onCreateFootLoadViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.layout_load_load, parent, false);
        return new BaseHolder(view);
    }

    protected BaseHolder onCreateFootEmptyViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.layout_load_end, parent, false);
        return new BaseHolder(view);
    }

    protected BaseHolder onCreateFootErrorViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.layout_load_error, parent, false);
        return new BaseHolder(view);
    }
}
