package com.zuomu.smartpen;

public class NativeUtils {

    static {
        System.loadLibrary("native-lib");
    }

    public static native void startReadingMouseEvents(MouseEventCallback callback);
    public static native void stopReadingMouseEvents();
}