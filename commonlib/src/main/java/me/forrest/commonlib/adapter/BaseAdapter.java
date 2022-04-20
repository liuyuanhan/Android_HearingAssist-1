package me.forrest.commonlib.adapter;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public abstract class BaseAdapter extends RecyclerView.Adapter {
    protected Context context;
    //  protected Activity activity;
    protected RecyclerView recyclerView;

    public Context getContext() {
        return context;
    }

    public RecyclerView getRecyclerView() {
        return recyclerView;
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        this.recyclerView = recyclerView;
        this.context = recyclerView.getContext();
        //    this.activity = (AppCompatActivity) context;
    }

    public void scheduleLayoutAnimation() {
        if (recyclerView != null) {
            recyclerView.scheduleLayoutAnimation();
        }

        this.notifyDataSetChanged();
    }

}
