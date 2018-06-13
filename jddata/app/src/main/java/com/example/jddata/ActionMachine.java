package com.example.jddata;

import java.util.ArrayList;

public class ActionMachine {

    private Action mCurrentAction;
    private ArrayList<MachineState> mCommandArrayList = new ArrayList<>();

    public static class MachineState {
        public String scene;
        public int commandCode;
        public long delay;
        public boolean canSkip;
        public boolean concernResult;      // 如果事件失败，则任务中断失败
        public boolean waitForContentChange;
        public Object obj;
        public String[] extraScene;         // 有可能有多个场景可执行相同的步骤

        public MachineState(String scene, Integer commandCodes) {
            this(scene, true, commandCodes);
        }

        public MachineState(String scene, boolean concernResult, Integer commandCodes) {
            this(scene, concernResult, AccessibilityCommandHandler.DEFAULT_COMMAND_INTERVAL, commandCodes);
        }

        public MachineState(String scene, boolean concernResult, long delay, Integer commandCodes) {
            this.concernResult = concernResult;
            this.commandCode = commandCodes;
            this.scene = scene;
            this.delay = delay;
        }

        private boolean hasExtraSceneMatch(String scene) {
            if (extraScene == null) return false;
            for (String s : extraScene) {
                if (s.equals(scene)) {
                    return true;
                }
            }
            return false;
        }

        public boolean isSceneMatch(String scene) {
            return this.scene.equals(scene) || hasExtraSceneMatch(scene);
        }
    }

    public ActionMachine(Action action) {
        mCurrentAction = action;
        mCommandArrayList.clear();
        String type = mCurrentAction.actionType;
        if ("1".equals(type)) {
//            mCommandArrayList.add(new MachineState("com.jingdong.app.mall.MainFrameActivity", ServiceCommand.CATEGORY));
            mCommandArrayList.add(new MachineState("com.jingdong.app.mall.MainFrameActivity", false, 3000L, ServiceCommand.CLICK_SEARCH));
        }
    }

    public MachineState getCurrentMachineState() {
        if (mCommandArrayList.isEmpty()) {
            return null;
        }
        return mCommandArrayList.get(0);
    }

    public MachineState getNextState() {
        if (mCommandArrayList.isEmpty()) {
            return null;
        }
        MachineState target = null;
        if (mCommandArrayList.size() > 1) {
            target = mCommandArrayList.get(1);
        }
        return target;
    }

    public void removeCurrentState() {
        if (!mCommandArrayList.isEmpty()) {
            mCommandArrayList.remove(0);
        }
    }

    public Action getCurrentAction() {
        return mCurrentAction;
    }
}
