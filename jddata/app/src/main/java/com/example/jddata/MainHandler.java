package com.example.jddata;

public class MainHandler {

    public ActionMachine mCurrentMachine;

    private static class Holder {
        private static MainHandler mInstance = new MainHandler();

        private Holder() {
        }
    }

    public static MainHandler getInstance() {
        return Holder.mInstance;
    }
}
