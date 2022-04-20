package me.forrest.commonlib.adapter;

import android.view.View;

public interface RecyclerListener {

    interface OnClickListener<T>{
        void onClick(View v, T t, int position);
    }

    interface OnLoadListener{
        void onLoad();
    }
}
