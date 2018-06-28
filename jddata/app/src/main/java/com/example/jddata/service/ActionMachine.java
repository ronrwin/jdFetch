package com.example.jddata.service;

import com.example.jddata.BusHandler;
import com.example.jddata.excel.DmpSheet;

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
            this(scene, false, commandCodes);
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
        mCommandArrayList.addAll(initStep());
        if (Action.SEARCH_HAIFEISI.equals(type)) {
            mCommandArrayList.add(new MachineState(AccService.JD_HOME,  ServiceCommand.CLICK_SEARCH));
            MachineState input = new MachineState(AccService.SEARCH,  ServiceCommand.INPUT);
            input.obj = "海飞丝";
            mCommandArrayList.add(input);
            mCommandArrayList.add(new MachineState(AccService.SEARCH, ServiceCommand.SEARCH));
            mCommandArrayList.add(new MachineState(AccService.PRODUCT_LIST,  ServiceCommand.SEARCH_DATA));
        } else if (Action.CART.equals(type)) {
            mCommandArrayList.add(new MachineState(AccService.JD_HOME, ServiceCommand.CART_TAB));
            mCommandArrayList.add(new MachineState(AccService.JD_HOME, ServiceCommand.CART_SCROLL));
        } else if (Action.HOME.equals(type)) {
            mCommandArrayList.add(new MachineState(AccService.JD_HOME, ServiceCommand.HOME_SCROLL));
        } else if (Action.BRAND_KILL.equals(type)) {
            BusHandler.getInstance().mBrandEntitys.clear();
            BusHandler.getInstance().mBrandSheet = null;
            mCommandArrayList.add(new MachineState(AccService.JD_HOME, ServiceCommand.HOME_BRAND_KILL));
            mCommandArrayList.add(new MachineState(AccService.MIAOSHA, true, ServiceCommand.HOME_BRAND_KILL_SCROLL));
        } else if (Action.LEADERBOARD.equals(type)) {
            mCommandArrayList.add(new MachineState(AccService.JD_HOME, ServiceCommand.LEADERBOARD));
            mCommandArrayList.add(new MachineState(AccService.NATIVE_COMMON, false, 8000L, ServiceCommand.LEADERBOARD_TAB));
        } else if (Action.JD_KILL.equals(type)) {
            mCommandArrayList.add(new MachineState(AccService.JD_HOME, ServiceCommand.HOME_JD_KILL));
            mCommandArrayList.add(new MachineState(AccService.MIAOSHA, ServiceCommand.JD_KILL_SCROLL));
        } else if (Action.WORTH_BUY.equals(type)) {
            mCommandArrayList.add(new MachineState(AccService.JD_HOME, ServiceCommand.WORTH_BUY));
            mCommandArrayList.add(new MachineState(AccService.WORTHBUY, ServiceCommand.WORTH_BUY_SCROLL));
        } else if (Action.NICE_BUY.equals(type)) {
            BusHandler.getInstance().mNiceBuyTitles.clear();
            BusHandler.getInstance().mNiceBuySheet = null;
            mCommandArrayList.add(new MachineState(AccService.JD_HOME, ServiceCommand.NICE_BUY));
            mCommandArrayList.add(new MachineState(AccService.WORTHBUY, true, ServiceCommand.NICE_BUY_SCROLL));
        } else if (Action.TYPE_KILL.equals(type)) {
            BusHandler.getInstance().mTypePrices.clear();
            BusHandler.getInstance().mTypeSheet = null;
            mCommandArrayList.add(new MachineState(AccService.JD_HOME, ServiceCommand.HOME_TYPE_KILL));
            mCommandArrayList.add(new MachineState(AccService.MIAOSHA, true, ServiceCommand.HOME_TYPE_KILL_SCROLL));
        } else if (Action.DMP.equals(type)) {
            BusHandler.getInstance().mDmpSheet = new DmpSheet("dmp");
            for (int i = 0; i < 8; i++) {
                mCommandArrayList.add(new MachineState(AccService.JD_HOME, false, 5000L, ServiceCommand.DMP_CLICK));
                MachineState title = new MachineState(AccService.BABEL_ACTIVITY, ServiceCommand.DMP_TITLE);
                title.extraScene = new String[] {AccService.WEBVIEW_ACTIVITY};
                mCommandArrayList.add(title);
                MachineState back = new MachineState(AccService.BABEL_ACTIVITY, ServiceCommand.GO_BACK);
                back.extraScene = new String[] {AccService.WEBVIEW_ACTIVITY};
                mCommandArrayList.add(back);
            }
        }
    }

    // 解决广告弹出阻碍步骤
    private ArrayList<MachineState> initStep() {
        ArrayList<MachineState> list = new ArrayList<>();
        MachineState privacy = new MachineState(AccService.PRIVACY,  ServiceCommand.AGREE);
        privacy.canSkip = true;
        list.add(privacy);
        list.add(new MachineState(AccService.JD_HOME, ServiceCommand.HOME_TAB));
        list.add(new MachineState(AccService.JD_HOME, false, 5000L, ServiceCommand.CLOSE_AD));
        return list;
    }

    public MachineState getCurrentMachineState() {
        if (mCommandArrayList.isEmpty()) {
            return null;
        }
        return mCommandArrayList.get(0);
    }

    public MachineState getState(int index) {
        if (mCommandArrayList.isEmpty()) {
            return null;
        }
        MachineState target = null;
        if (mCommandArrayList.size() > index) {
            target = mCommandArrayList.get(index);
        }
        return target;
    }

    public void removeCurrentState() {
        if (!mCommandArrayList.isEmpty()) {
            mCommandArrayList.remove(0);
        }
    }

    public ArrayList<MachineState> getCommandArrayList() {
        return mCommandArrayList;
    }

    public Action getCurrentAction() {
        return mCurrentAction;
    }
}
