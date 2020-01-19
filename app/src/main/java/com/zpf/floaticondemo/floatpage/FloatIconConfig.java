package com.zpf.floaticondemo.floatpage;

import android.content.Context;

import com.zpf.floaticondemo.util.SharedPreferencesUtil;


public class FloatIconConfig {
    private static final String FLOAT_ICON_POS_X = "float_icon_pos_x";
    private static final String FLOAT_ICON_POS_Y = "float_icon_pos_y";

    public static int getLastPosX(Context context) {
        return SharedPreferencesUtil.getInstance(context).getInt(FLOAT_ICON_POS_X, 0);
    }

    public static int getLastPosY(Context context) {
        return SharedPreferencesUtil.getInstance(context).getInt(FLOAT_ICON_POS_Y, 0);
    }

    public static void saveLastPosY(Context context, int val) {
        SharedPreferencesUtil.getInstance(context).putInt(FLOAT_ICON_POS_Y, val);
    }

    public static void saveLastPosX(Context context, int val) {
        SharedPreferencesUtil.getInstance(context).putInt(FLOAT_ICON_POS_X, val);
    }
}
