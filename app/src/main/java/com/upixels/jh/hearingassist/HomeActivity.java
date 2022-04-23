package com.upixels.jh.hearingassist;

import android.os.Bundle;

import com.google.android.material.snackbar.Snackbar;
import com.upixels.jh.hearingassist.databinding.ActivityHomeBinding;

import androidx.appcompat.app.AppCompatActivity;

import android.view.View;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;



public class HomeActivity extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;
    private ActivityHomeBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        initView();
    }

    private void initView() {
        binding.ivMore.setOnClickListener(v -> {
            binding.drawerLayout.open();
        });
    }
}