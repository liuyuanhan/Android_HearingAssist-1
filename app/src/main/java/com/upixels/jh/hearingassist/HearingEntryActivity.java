package com.upixels.jh.hearingassist;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;

import com.upixels.jh.hearingassist.adapter.HearingEntryAdapter;
import com.upixels.jh.hearingassist.databinding.ActivityHearingEntryBinding;
import com.upixels.jh.hearingassist.entity.HearingAssetEntity;

import java.util.ArrayList;
import java.util.List;

public class HearingEntryActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ActivityHearingEntryBinding binding;
    private List<HearingAssetEntity> list = new ArrayList<>(2);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHearingEntryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        initData();
        makeUI();
        bindViewModel();
    }

    public void makeUI() {
        recyclerView = binding.recyclerView;
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, true);
        recyclerView.setLayoutManager(layoutManager);
        HearingEntryAdapter adapter = new HearingEntryAdapter(this, list);
        recyclerView.setAdapter(adapter);
        adapter.setItemOnClickListener((v, position) -> {
            if (position == 0) {

            }
        });
    }

    private void initData() {
        HearingAssetEntity entity0 = new HearingAssetEntity();
        entity0.viewType = HearingAssetEntity.VIEW_TYPE_0;
        list.add(entity0);
        HearingAssetEntity entity1 = new HearingAssetEntity();
        entity1.viewType = HearingAssetEntity.VIEW_TYPE_0;
        list.add(entity1);
    }

    public void bindViewModel() {
        binding.backImageView.setOnClickListener( v -> finish());
    }
}