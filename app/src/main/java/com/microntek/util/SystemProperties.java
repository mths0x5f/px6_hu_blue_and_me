package com.microntek.util;

import android.annotation.SuppressLint;
import android.util.Log;

import java.lang.reflect.Method;
import java.util.Objects;

@SuppressLint("PrivateApi")
public class SystemProperties {

    private static final String TAG = "SystemProperties";

    private static Class<?> cls;
    private static Method getMethod;
    private static Method getIntMethod;
    private static Method getBoolMethod;

    static {
        try {
            cls = Class.forName("android.os.SystemProperties");
            getMethod = cls.getDeclaredMethod("get", String.class);
            getIntMethod = cls.getDeclaredMethod("getInt", String.class, Integer.TYPE);
            getBoolMethod = cls.getDeclaredMethod("getBoolean", String.class, Boolean.TYPE);
        } catch (Exception e) {
            Log.e(TAG, "static initializer failed!");
        }
    }

    public static String get(String key) {
        try {
            return (String) getMethod.invoke(cls, key);
        } catch (Exception e) {
            return "";
        }
    }

    public static String get(String key, String def) {
        try {
            return (String) getMethod.invoke(cls, key);
        } catch (Exception e) {
            return def;
        }
    }

    public static int getInt(String key, int def) {
        try {
            Integer value = (Integer) getIntMethod.invoke(cls, key, def);
            return Objects.requireNonNull(value);
        } catch (Exception e) {
            return def;
        }
    }

    public static boolean getBoolean(String key, boolean def) {
        try {
            Boolean v = (Boolean) getBoolMethod.invoke(cls, key, def);
            return Objects.requireNonNull(v);
        } catch (Exception e) {
            return def;
        }
    }

    public static int getSdkVersion() {
        return getInt("ro.build.version.sdk", -1);
    }

}
