package cn.cc.sdk.demo.util;

import android.util.Log;

public class MyLogger {
    private static final String TAG = "sdk";
    //    public static final boolean DEBUG = BuildConfig.DEBUG;
    public static final boolean DEBUG = true;

    public static void v(String className, String log) {
        if (DEBUG) {
            Log.v(TAG, "[" + className + "]:" + log);
        }
    }

    public static void d(String className, String log) {
        if (DEBUG) {
            Log.d(TAG, "[" + className + "]:" + log);
        }
    }

    public static void i(String className, String log) {
        if (DEBUG) {
            Log.i(TAG, "[" + className + "]:" + log);
        }
    }

    public static void i(String className, String log, Throwable tr) {
        if (DEBUG) {
            Log.i(TAG, "[" + className + "]:" + log + "\n" + Log.getStackTraceString(tr));
        }
    }

    public static void w(String className, String log) {
        if (DEBUG) {
            Log.w(TAG, "[" + className + "]:" + log);
        }
    }

    public static void w(String className, String log, Throwable tr) {
        if (DEBUG) {
            Log.w(TAG, "[" + className + "]:" + log + "\n" + Log.getStackTraceString(tr));
        }
    }

    public static void e(String className, String log) {
        if (DEBUG) {
            Log.e(TAG, "[" + className + "]:" + log);
        }
    }

    public static void e(String className, String log, Throwable tr) {
        if (DEBUG) {
            Log.e(TAG, "[" + className + "]:" + log + "\n" + Log.getStackTraceString(tr));
        }
    }
}
