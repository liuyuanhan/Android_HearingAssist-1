package com.upixels.jh.hearingassist;

import android.os.Bundle;

import com.google.android.material.tabs.TabLayout;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import android.view.View;

import com.google.android.material.tabs.TabLayoutMediator;
import com.upixels.jh.hearingassist.ui.main.SectionsPagerAdapter;
import com.upixels.jh.hearingassist.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(this);
        ViewPager2 viewPager = binding.viewPager;
        viewPager.setAdapter(sectionsPagerAdapter);
        TabLayout tabLayout = binding.tabLayout;

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            if (position == 0) {
                tab.setText(R.string.tab_text_Mode);
                tab.setIcon(R.drawable.tab_icon_mode);
            } else if (position == 1) {
                tab.setText(R.string.tab_text_Volume);
                tab.setIcon(R.drawable.tab_icon_volume);
            } else if (position == 2) {
                tab.setText(R.string.tab_text_Band);
                tab.setIcon(R.drawable.tab_icon_band);
            } else if (position == 3) {
                tab.setText(R.string.tab_text_Loud);
                tab.setIcon(R.drawable.tab_icon_loud);
            } else if (position == 4) {
                tab.setText(R.string.tab_text_Focus);
                tab.setIcon(R.drawable.tab_icon_focus);
            } else if (position == 5) {
                tab.setText(R.string.tab_text_Noise);
                tab.setIcon(R.drawable.tab_icon_noise);
            }
        }).attach();
    }
}