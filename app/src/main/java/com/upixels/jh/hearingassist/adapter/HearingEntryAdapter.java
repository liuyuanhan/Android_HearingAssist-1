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

public class HearingEntryAdapter extends RecyclerView.Adapter <HearingEntryAdapter.ViewHolder> {

    private Context context;

    public HearingEntryAdapter(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_hearing_entry, parent, false);
        ViewHolder viewHolder= new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (position == 0) {
            holder.imageView.setImageResource(R.drawable.icon_hearing_entry_audiogram);
            holder.button.setText(R.string.hearing_input_audiogram);
        } else if (position == 1) {
            holder.imageView.setImageResource(R.drawable.icon_hearing_entry_check);
            holder.button.setText(R.string.hearing_check);
        }
    }

    @Override
    public int getItemCount() {
        return 2;
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
}
