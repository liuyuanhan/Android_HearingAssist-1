package com.upixels.jh.hearingassist;

import android.content.Intent;
import android.os.Bundle;

import com.upixels.jh.hearingassist.databinding.ActivityHomeBinding;

import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;



import androidx.navigation.ui.AppBarConfiguration;




public class HomeActivity extends AppCompatActivity {
    private final static String TAG = "HomeActivity";
    private AppBarConfiguration appBarConfiguration;
    private ActivityHomeBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "[onCreate]");
        super.onCreate(savedInstanceState);
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        initView();
    }

    private void initView() {
        binding.ivMore.setOnClickListener(v -> {
            binding.drawerLayout.open();
        });
        binding.ivSettings.setOnClickListener( v -> {
            startActivity(new Intent(this, ConnectActivity.class));
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "[onDestroy]");
    }
}