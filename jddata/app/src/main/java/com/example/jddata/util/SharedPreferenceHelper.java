package com.example.jddata.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.jddata.MainApplication;

public class SharedPreferenceHelper {
    public static final String PHONE_NUM = "phone_num";

    private static class Holder {
        private static SharedPreferenceHelper mInstance = new SharedPreferenceHelper();

        private Holder() {
        }
    }

    private SharedPreferences mSharedPreferences;

    public static SharedPreferenceHelper getInstance() {
        return SharedPreferenceHelper.Holder.mInstance;
    }

    SharedPreferenceHelper() {
        mSharedPreferences = MainApplication.getContext().getSharedPreferences("jdfetch", Context.MODE_PRIVATE);
    }

    public void saveValue(String key, String value) {
        if (mSharedPreferences != null) {
            SharedPreferences.Editor editor = mSharedPreferences.edit();
            editor.putString(key, value);
            editor.apply();
        }
    }

    public String getValue(String key) {
        if (mSharedPreferences != null) {
            return mSharedPreferences.getString(key, "");
        }
        return "";
    }
}
