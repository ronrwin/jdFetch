package com.example.jddata.service;

import com.example.jddata.BusHandler;
import com.example.jddata.MainApplication;
import com.example.jddata.excel.DmpSheet;

import java.util.ArrayList;

public class ActionMachine {

    private String mCurrentActionType;
    private ArrayList<MachineState> mCommandArrayList = new ArrayList<>();

    public ActionMachine(String actionType) {
        mCommandArrayList.clear();
        mCurrentActionType = actionType;
        mCommandArrayList.addAll(initStep());
        if (BaseAction.SEARCH.equals(mCurrentActionType)) {
            mCommandArrayList.add(new MachineState(AccService.JD_HOME,  ServiceCommand.CLICK_SEARCH));
            MachineState input = new MachineState(AccService.SEARCH,  ServiceCommand.INPUT);
            input.obj = MainApplication.sSearchText;
            mCommandArrayList.add(input);
            mCommandArrayList.add(new MachineState(AccService.SEARCH, ServiceCommand.SEARCH));
            mCommandArrayList.add(new MachineState(AccService.PRODUCT_LIST,  ServiceCommand.SEARCH_DATA));
        } else if (BaseAction.SEARCH_AND_SHOP.equals(mCurrentActionType)) {
            mCommandArrayList.add(new MachineState(AccService.JD_HOME,  ServiceCommand.CLICK_SEARCH));
            MachineState input = new MachineState(AccService.SEARCH,  ServiceCommand.INPUT);
            input.obj = MainApplication.sSearchText;
            mCommandArrayList.add(input);
            mCommandArrayList.add(new MachineState(AccService.SEARCH, ServiceCommand.SEARCH));
            mCommandArrayList.add(new MachineState(AccService.PRODUCT_LIST,  ServiceCommand.SEARCH_DATA_RANDOM_BUY));
        }
        else if (BaseAction.CART.equals(mCurrentActionType)) {
            mCommandArrayList.add(new MachineState(AccService.JD_HOME, ServiceCommand.CART_TAB));
            mCommandArrayList.add(new MachineState(AccService.JD_HOME, ServiceCommand.CART_SCROLL));
        } else if (BaseAction.HOME.equals(mCurrentActionType)) {
            mCommandArrayList.add(new MachineState(AccService.JD_HOME, ServiceCommand.HOME_SCROLL));
        } else if (BaseAction.BRAND_KILL.equals(mCurrentActionType)) {
            BusHandler.getInstance().mBrandEntitys.clear();
            BusHandler.getInstance().mBrandSheet = null;
            mCommandArrayList.add(new MachineState(AccService.JD_HOME, ServiceCommand.HOME_BRAND_KILL));
            mCommandArrayList.add(new MachineState(AccService.MIAOSHA, true, ServiceCommand.HOME_BRAND_KILL_SCROLL));
        } else if (BaseAction.BRAND_KILL_AND_SHOP.equals(mCurrentActionType)) {
            BusHandler.getInstance().mBrandEntitys.clear();
            mCommandArrayList.add(new MachineState(AccService.JD_HOME, ServiceCommand.HOME_BRAND_KILL));
            mCommandArrayList.add(new MachineState(AccService.MIAOSHA, true, ServiceCommand.HOME_BRAND_KILL_SCROLL));
        } else if (BaseAction.LEADERBOARD.equals(mCurrentActionType)) {
            mCommandArrayList.add(new MachineState(AccService.JD_HOME, ServiceCommand.LEADERBOARD));
            mCommandArrayList.add(new MachineState(AccService.NATIVE_COMMON, false, 8000L, ServiceCommand.LEADERBOARD_TAB));
        } else if (BaseAction.JD_KILL.equals(mCurrentActionType)) {
            mCommandArrayList.add(new MachineState(AccService.JD_HOME, ServiceCommand.HOME_JD_KILL));
            mCommandArrayList.add(new MachineState(AccService.MIAOSHA, ServiceCommand.JD_KILL_SCROLL));
        } else if (BaseAction.WORTH_BUY.equals(mCurrentActionType)) {
            mCommandArrayList.add(new MachineState(AccService.JD_HOME, ServiceCommand.WORTH_BUY));
            mCommandArrayList.add(new MachineState(AccService.WORTHBUY, ServiceCommand.WORTH_BUY_SCROLL));
        } else if (BaseAction.NICE_BUY.equals(mCurrentActionType)) {
            BusHandler.getInstance().mNiceBuyTitles.clear();
            BusHandler.getInstance().mNiceBuySheet = null;
            mCommandArrayList.add(new MachineState(AccService.JD_HOME, ServiceCommand.NICE_BUY));
            mCommandArrayList.add(new MachineState(AccService.WORTHBUY, true, ServiceCommand.NICE_BUY_SCROLL));
        } else if (BaseAction.TYPE_KILL.equals(mCurrentActionType)) {
            BusHandler.getInstance().mTypePrices.clear();
            BusHandler.getInstance().mTypeSheet = null;
            mCommandArrayList.add(new MachineState(AccService.JD_HOME, ServiceCommand.HOME_TYPE_KILL));
            mCommandArrayList.add(new MachineState(AccService.MIAOSHA, true, ServiceCommand.HOME_TYPE_KILL_SCROLL));
        } else if (BaseAction.DMP.equals(mCurrentActionType)) {
            BusHandler.getInstance().mDmpSheet = new DmpSheet();
            for (int i = 0; i < 8; i++) {
                mCommandArrayList.add(new MachineState(AccService.JD_HOME, false, 5000L, ServiceCommand.DMP_CLICK));
                MachineState title = new MachineState(AccService.BABEL_ACTIVITY, false, 3000L, ServiceCommand.DMP_TITLE);
                title.extraScene = new String[] {AccService.WEBVIEW_ACTIVITY};
                mCommandArrayList.add(title);
                MachineState back = new MachineState(AccService.BABEL_ACTIVITY, ServiceCommand.GO_BACK);
                back.extraScene = new String[] {AccService.WEBVIEW_ACTIVITY};
                mCommandArrayList.add(back);
            }
        } else if (BaseAction.DMP_AND_SHOP.equals(mCurrentActionType)) {
            for (int i = 0; i < 8; i++) {
                mCommandArrayList.add(new MachineState(AccService.JD_HOME, false, 5000L, ServiceCommand.DMP_CLICK));
                MachineState findPrice = new MachineState(AccService.BABEL_ACTIVITY, true, 3000L, ServiceCommand.DMP_FIND_PRICE);
                findPrice.extraScene = new String[] {AccService.WEBVIEW_ACTIVITY};
                mCommandArrayList.add(findPrice);
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

    public String getCurrentActionType() {
        return mCurrentActionType;
    }
}
