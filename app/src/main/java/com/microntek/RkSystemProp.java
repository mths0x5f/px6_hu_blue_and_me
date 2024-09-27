package com.microntek;

import java.lang.reflect.Method;

/* loaded from: classes.dex */
public class RkSystemProp {
    private static final String TAG = "RkSystemProp";
    private static Class<?> mClassType = null;
    private static Method mGetMethod = null;
    private static Method mGetIntMethod = null;
    private static Method mGetBoolMethod = null;
    private static Method mSetMethod = null;

    public static String get(String key) {
        init();
        try {
            String value = (String) mGetMethod.invoke(mClassType, key);
            return value;
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public static String get(String key, String def) {
        init();
        try {
            String value = (String) mGetMethod.invoke(mClassType, key);
            return value;
        } catch (Exception e) {
            e.printStackTrace();
            return def;
        }
    }

    public static int getInt(String key, int def) {
        init();
        try {
            Integer v = (Integer) mGetIntMethod.invoke(mClassType, key, Integer.valueOf(def));
            int value = v.intValue();
            return value;
        } catch (Exception e) {
            e.printStackTrace();
            return def;
        }
    }

    public static boolean getBoolean(String key, boolean def) {
        init();
        try {
            Boolean v = (Boolean) mGetBoolMethod.invoke(mClassType, key, Boolean.valueOf(def));
            boolean value = v.booleanValue();
            return value;
        } catch (Exception e) {
            e.printStackTrace();
            return def;
        }
    }

    public static int getSdkVersion() {
        return getInt("ro.build.version.sdk", -1);
    }

    public static void set(String key, String def) {
        init();
        try {
            mSetMethod.invoke(mClassType, key);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void init() {
        try {
            if (mClassType == null) {
                Class<?> cls = Class.forName("android.os.SystemProperties");
                mClassType = cls;
                mGetMethod = cls.getDeclaredMethod("get", String.class);
                mGetIntMethod = mClassType.getDeclaredMethod("getInt", String.class, Integer.TYPE);
                mGetBoolMethod = mClassType.getDeclaredMethod("getBoolean", String.class, Boolean.TYPE);
                mSetMethod = mClassType.getDeclaredMethod("set", String.class, String.class);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
