package com.microntek.cvbs;

/* loaded from: classes.dex */
public class JniUtil {
    private static final String[] CHANNEL_STRING = {"backview", "frontview", "rightview", "leftview", "avin", "dtv", "dvr", "dvd", "view_back", "view_front", "view_right", "view_left"};
    private static final int[] CHANNEL_INT = {129, 134, 135, 136, 2, 3, 4, 5, 1, 6, 7, 8};

    public native int close();

    public native void on_draw_frame(boolean z, boolean z2, int i, int i2, int i3, int i4);

    public native void on_surface_changed(int i, int i2, int i3, int i4);

    public native void on_surface_created();

    public native int open(OnFrameListener onFrameListener, int i);

    static {
        System.loadLibrary("cvbs_jni");
    }

    public int open(OnFrameListener listener, String channel_str) {
        int i = 0;
        while (true) {
            String[] strArr = CHANNEL_STRING;
            if (i < strArr.length) {
                if (!channel_str.equals(strArr[i])) {
                    i++;
                } else {
                    return open(listener, CHANNEL_INT[i]);
                }
            } else {
                return -1;
            }
        }
    }
}
