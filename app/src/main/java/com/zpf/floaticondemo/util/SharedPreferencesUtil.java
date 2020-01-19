package com.zpf.floaticondemo.util;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferencesUtil {
    private static SharedPreferencesUtil sharedPreferencesUtil;
    private Context context;
    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;
    private static final String NAME = "LOCAL_STORAGE";

    public synchronized static SharedPreferencesUtil getInstance(Context context) {
        if (sharedPreferencesUtil == null) {
            sharedPreferencesUtil = new SharedPreferencesUtil();
            sharedPreferencesUtil.context = context;
            sharedPreferencesUtil.prefs = sharedPreferencesUtil.context.getSharedPreferences(NAME, Activity.MODE_PRIVATE);
            sharedPreferencesUtil.editor = sharedPreferencesUtil.prefs.edit();
        }
        return sharedPreferencesUtil;
    }

    private SharedPreferencesUtil() {
    }

    public int getInt(String key, int defaultVal) {
        return this.prefs.getInt(key, defaultVal);
    }

    public int getInt(String key) {
        return this.prefs.getInt(key, 0);
    }


    public void putInt(String key, int value) {
        editor.putInt(key, value);
        editor.commit();
    }

}
