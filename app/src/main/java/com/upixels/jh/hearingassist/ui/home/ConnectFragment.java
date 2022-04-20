package com.upixels.jh.hearingassist.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import me.forrest.commonlib.util.FileUtil;

import com.upixels.jh.hearingassist.R;
import com.upixels.jh.hearingassist.adapter.SectionQuickAdapter;
import com.upixels.jh.hearingassist.databinding.FragmentSecondBinding;
import com.upixels.jh.hearingassist.entity.BLEDeviceEntity;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class ConnectFragment extends Fragment {

    private FragmentSecondBinding binding;
    private SectionQuickAdapter mAdapter;
    private List<BLEDeviceEntity> mDeviceList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentSecondBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView();
//        binding.buttonSecond.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                NavHostFragment.findNavController(ConnectFragment.this)
//                        .navigate(R.id.action_SecondFragment_to_FirstFragment);
//            }
//        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void initView() {
        mDeviceList = initData();
        binding.rlvBleDevice.setLayoutManager(new LinearLayoutManager(this.getContext(), LinearLayoutManager.VERTICAL, false));
        mAdapter = new SectionQuickAdapter(R.layout.item_section_content_device, R.layout.item_section_head_device, mDeviceList);
        binding.rlvBleDevice.setAdapter(mAdapter);
    }

    private List<BLEDeviceEntity> initData() {
        List<BLEDeviceEntity> list = new LinkedList<>();
        BLEDeviceEntity headerSection = new BLEDeviceEntity(true);
        headerSection.section = 0;
        headerSection.mac = "";
        headerSection.header = getResources().getString(R.string.search_for_a_device);
        list.add(headerSection);

        BLEDeviceEntity d0 = new BLEDeviceEntity(false);
        d0.section = 0;
        d0.mac = "";
        d0.deviceName = "HA-Connect-L";
        d0.connectStatus = 0;
        list.add(d0);

        BLEDeviceEntity d1 = new BLEDeviceEntity(false);
        d1.section = 0;
        d1.mac = "";
        d1.deviceName = "HA-Connect-L";
        d1.connectStatus = 0;
        list.add(d1);

//        headerSection1 = new BLEDeviceEntity(true);
//        headerSection1.section = 1;
//        headerSection1.mac = "";
//        headerSection1.header = getResources().getString(R.string.other_device);
//        list.add(headerSection1);

        // 获取配对过的设备
//        leftPairedDevices = ((MyApplication)getApplication()).getLeftPairedDevices();
//        rightPairedDevices = ((MyApplication)getApplication()).getRightPairedDevices();
//        FileUtil.readPairedDevices(this, "Left", leftPairedDevices);
//        FileUtil.readPairedDevices(this, "Right", rightPairedDevices);

        return list;
    }

}