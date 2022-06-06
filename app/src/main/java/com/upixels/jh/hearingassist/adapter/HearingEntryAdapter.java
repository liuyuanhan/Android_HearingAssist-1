package com.upixels.jh.hearingassist.adapter;

import android.content.Context;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.upixels.jh.hearingassist.R;
import com.upixels.jh.hearingassist.entity.HearingAssetEntity;

import java.util.List;

public class HearingEntryAdapter extends RecyclerView.Adapter <RecyclerView.ViewHolder> {

    // RecycleView Item 点击事件处理接口
    public interface ItemOnClickListener {
        void onClick(View v, int position);
    }

    private Context context;
    private List<HearingAssetEntity> list;
    private ItemOnClickListener itemOnClickListener;

    public void setItemOnClickListener(ItemOnClickListener listener) {
        this.itemOnClickListener = listener;
    }

    public HearingEntryAdapter(Context context, List<HearingAssetEntity> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ViewHolder viewHolder = null;
        if (viewType == HearingAssetEntity.VIEW_TYPE_0) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_hearing_entry, parent, false);
            viewHolder= new ViewHolder(view);
        } else if (viewType == HearingAssetEntity.VIEW_TYPE_1) {

        }
        return viewHolder;
    }


    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder.getItemViewType() == HearingAssetEntity.VIEW_TYPE_0) {
            HearingEntryAdapter.ViewHolder viewHolder = (HearingEntryAdapter.ViewHolder) holder;
            if (position == 0) {
                viewHolder.imageView.setImageResource(R.drawable.icon_hearing_entry_audiogram);
                viewHolder.button.setText(R.string.hearing_input_audiogram);
            } else if (position == 1) {
                viewHolder.imageView.setImageResource(R.drawable.icon_hearing_entry_check);
                viewHolder.button.setText(R.string.hearing_check);
            }
            viewHolder.button.setOnClickListener(v -> {
                if (itemOnClickListener != null) { itemOnClickListener.onClick(v, position);}
            });
            
        } else if (holder.getItemViewType() == HearingAssetEntity.VIEW_TYPE_1) {

        }

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    @Override
    public int getItemViewType(int position) {
        return list.get(position).viewType;
    }

    public class ViewHolder extends  RecyclerView.ViewHolder {
        protected Button button;
        protected ImageView imageView;

        public ViewHolder(@NonNull View view) {
            super(view);
            imageView = view.findViewById(R.id.imageView);
            button = view.findViewById(R.id.button);
        }
    }

    public class ViewHolder1 extends  RecyclerView.ViewHolder {
        protected Button button;
        protected ImageView imageView;

        public ViewHolder1(@NonNull View view) {
            super(view);
            imageView = view.findViewById(R.id.imageView);
            button = view.findViewById(R.id.button);
        }
    }
}
