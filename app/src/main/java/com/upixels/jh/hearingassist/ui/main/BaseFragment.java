package com.upixels.jh.hearingassist.ui.main;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.upixels.jh.hearingassist.databinding.FragmentModeBinding;
import com.upixels.jh.hearingassist.util.DeviceManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import me.forrest.commonlib.jh.BTProtocol;
import me.forrest.commonlib.jh.SceneMode;

public abstract class BaseFragment extends Fragment {
    private boolean isVisible       = false;

    protected SceneMode             leftMode;
    protected SceneMode             rightMode;

    @Override
    public void onStart() {
        super.onStart();
        DeviceManager.getInstance().addListener(deviceChangeListener);
    }

    @Override
    public void onResume() {
        super.onResume();
        leftMode = DeviceManager.getInstance().getLeftMode();
        rightMode = DeviceManager.getInstance().getRightMode();
        updateView(leftMode, rightMode);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        DeviceManager.getInstance().removeListener(deviceChangeListener);
    }

    // 更新模式变化，子类重写该方法
    protected void updateView(SceneMode leftMode, SceneMode rightMode) {}

    // 更新控制回调，子类重写该方法
    protected void updateCtlFeedback(String leftResult, String rightResult) {}

    protected void updateModeFile(BTProtocol.ModeFileContent leftContent, BTProtocol.ModeFileContent rightContent) {}

    private final DeviceManager.DeviceChangeListener deviceChangeListener = new DeviceManager.DeviceChangeListener() {

        @Override
        public void onReadingStatus(boolean isReading) {

        }

        @Override
        public void onChangeBat(int leftBat, int rightBat) {

        }

        @Override
        public void onChangeSceneMode(SceneMode leftMode, SceneMode rightMode) {
            BaseFragment.this.leftMode = leftMode;
            BaseFragment.this.rightMode = rightMode;
            requireActivity().runOnUiThread(() -> updateView(leftMode, rightMode));
        }

        @Override
        public void onChangeModeFile(BTProtocol.ModeFileContent leftContent, BTProtocol.ModeFileContent rightContent) {
            requireActivity().runOnUiThread(() -> {
                updateModeFile(leftContent, rightContent);
            });
        }

        @Override
        public void onCtlFeedback(String leftResult, String rightResult) {
            updateCtlFeedback(leftResult, rightResult);
        }
    };

}
