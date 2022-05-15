package me.android.demo.util;

import android.os.Build;
import android.util.Log;

public class Logger {
    private static final String TAG_S = "ZY.DEBUG";
    private static final String DOT = ".";
    public static boolean isDebug = true | (!Build.TYPE.equals("user"));

    public static void d(String tag, String msg) {
        if (isDebug) {
            Log.d(TAG_S + DOT + tag, msg);
        }
    }

    public static void d(String msg) {
        if (isDebug) {
            Log.d(TAG_S, msg);
        }
    }

    public static void e(String tag, String msg) {
        Log.e(TAG_S + DOT + tag, msg);
    }

    public static void e(String tag, String msg, Throwable th) {
        Log.e(TAG_S + DOT + tag, msg, th);
    }

    public static void e(String msg) {
        Log.e(TAG_S, msg);
    }

    public static void i(String tag, String msg) {
        Log.i(TAG_S + DOT + tag, msg);
    }

    public static void i(String msg) {
        Log.i(TAG_S, msg);
    }

    public static void w(String tag, String msg) {
        Log.w(TAG_S + DOT + tag, msg);
    }
}
