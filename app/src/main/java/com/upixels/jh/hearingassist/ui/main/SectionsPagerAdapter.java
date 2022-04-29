package com.upixels.jh.hearingassist.ui.main;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;



public class SectionsPagerAdapter extends FragmentStateAdapter {


    public SectionsPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    public SectionsPagerAdapter(@NonNull Fragment fragment) {
        super(fragment);
    }

    public SectionsPagerAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle) {
        super(fragmentManager, lifecycle);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == 0) {
            return new ModeFragment();
        } else if (position == 1) {
            return VolumeFragment.newInstance("1", "2");
        } else if (position == 2) {
            return BandFragment.newInstance();
        }
        return PlaceholderFragment.newInstance(position+1);
    }

    @Override
    public int getItemCount() {
        return 6;
    }
}