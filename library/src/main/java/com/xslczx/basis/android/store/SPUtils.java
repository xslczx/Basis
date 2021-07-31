package com.xslczx.basis.android.store;

import com.xslczx.basis.android.AppBasis;

/**
 * SharedPreferences 工具类
 */
public final class SPUtils
        extends IPreferenceHolder {

    private SPUtils() {
    }

    public static IPreference getDefault() {
        return getPreference(AppBasis.getApp());
    }
}