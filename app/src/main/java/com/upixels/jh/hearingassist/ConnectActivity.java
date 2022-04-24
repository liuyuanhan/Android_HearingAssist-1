package com.upixels.jh.hearingassist;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.upixels.jh.hearingassist.databinding.ActivityConnectBinding;

public class ConnectActivity extends AppCompatActivity {

    private ActivityConnectBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityConnectBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        initView();
    }

    private void initView() {
        binding.ivBack.setOnClickListener(v -> finish());
    }

}