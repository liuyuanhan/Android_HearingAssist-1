package com.upixels.jh.hearingassist.ui.main;

import com.upixels.jh.hearingassist.MainActivity;
import com.upixels.jh.hearingassist.util.DeviceManager;

import androidx.fragment.app.Fragment;
import me.forrest.commonlib.jh.AIDMode;
import me.forrest.commonlib.jh.BTProtocol;

public abstract class BaseFragment extends Fragment {
//    private boolean                         isVisible       = false;

    protected boolean                       isSimulateModeFlag = true; // 是否进入模拟控制模式
    protected AIDMode                       leftMode;
    protected AIDMode                       rightMode;
    protected BTProtocol.ModeFileContent    leftContent;
    protected BTProtocol.ModeFileContent    rightContent;
    protected BTProtocol.ModeFileContent    curContent;

    protected boolean                       isActionCombined = true;  // 是否一起动作

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        isSimulateModeFlag = ((MainActivity)requireActivity()).isSimulateMode();
        if (!isSimulateModeFlag) {
            DeviceManager.getInstance().addListener(deviceChangeListener);
            leftMode = DeviceManager.getInstance().getLeftMode();
            rightMode = DeviceManager.getInstance().getRightMode();
            updateView(leftMode, rightMode);
        } else {
            leftMode = ((MainActivity)requireActivity()).getSimulateLeftMode();
            rightMode = ((MainActivity)requireActivity()).getSimulateRightMode();
            leftContent = ((MainActivity)requireActivity()).getSimulateLeftModeFileContent();
            rightContent = ((MainActivity)requireActivity()).getSimulateRightModeFileContent();
            updateView(leftMode, rightMode);
            updateModeFile(leftContent, rightContent);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (!isSimulateModeFlag) { DeviceManager.getInstance().removeListener(deviceChangeListener); }
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    // ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    //               ***  子类根据需要重写一下方法 ***
    // 更新模式变化，子类重写该方法
    protected void updateView(AIDMode leftMode, AIDMode rightMode) {}

    // 更新控制回调，子类重写该方法
    protected void updateCtlFeedback(String leftResult, String rightResult) {}

    protected void updateModeFile(BTProtocol.ModeFileContent leftContent, BTProtocol.ModeFileContent rightContent) {}

    protected void updateWriteFeedback(String leftResult, String rightResult) {}
    // ---------------------------------------------------------------------------

    private final DeviceManager.DeviceChangeListener deviceChangeListener = new DeviceManager.DeviceChangeListener() {

        @Override
        public void onConnectStatus(boolean leftConnected, boolean rightConnected) {
            leftMode = DeviceManager.getInstance().getLeftMode();
            rightMode = DeviceManager.getInstance().getRightMode();
            requireActivity().runOnUiThread(() -> updateView(leftMode, rightMode));
        }

        @Override
        public void onReadingStatus(boolean isReading) {

        }

        @Override
        public void onChangeBat(int leftBat, int rightBat) {

        }

        @Override
        public void onChangeSceneMode(AIDMode leftMode, AIDMode rightMode) {
            BaseFragment.this.leftMode = leftMode;
            BaseFragment.this.rightMode = rightMode;
            requireActivity().runOnUiThread(() -> updateView(leftMode, rightMode));
        }

        @Override
        public void onChangeModeFile(BTProtocol.ModeFileContent leftContent, BTProtocol.ModeFileContent rightContent) {
            requireActivity().runOnUiThread(() -> updateModeFile(leftContent, rightContent));
        }

        @Override
        public void onCtlFeedback(String leftResult, String rightResult) {
            updateCtlFeedback(leftResult, rightResult);
        }

        @Override
        public void onWriteFeedback(String leftResult, String rightResult) {
            updateWriteFeedback(leftResult, rightResult);
        }

    };

}
