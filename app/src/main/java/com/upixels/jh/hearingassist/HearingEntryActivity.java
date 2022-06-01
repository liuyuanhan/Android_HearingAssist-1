package com.upixels.jh.hearingassist;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import com.upixels.jh.hearingassist.adapter.HearingEntryAdapter;
import com.upixels.jh.hearingassist.databinding.ActivityHearingEntryBinding;

public class HearingEntryActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ActivityHearingEntryBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHearingEntryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        makeUI();
        bindViewModel();
    }

    public void makeUI() {
        recyclerView = binding.recyclerView;
        HearingEntryAdapter adapter = new HearingEntryAdapter(this);
        recyclerView.setAdapter(adapter);
    }

    public void bindViewModel() {
        binding.backImageView.setOnClickListener( v -> finish());
    }
}