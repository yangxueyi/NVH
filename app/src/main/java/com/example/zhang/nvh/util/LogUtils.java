package com.example.zhang.nvh.util;

import android.util.Log;

/**
 * Created by yang
 * Time 2017/8/24.
 */

public class LogUtils {

    private final static boolean OPENLOG = true;

    public static void i(String tag, String content) {
        if (OPENLOG) {
            Log.i(tag, content);
        }
    }
    public static void v(String tag, String content) {
        if (OPENLOG) {
            Log.v(tag, content);
        }
    }
    public static void d(String tag, String content) {
        if (OPENLOG) {
            Log.d(tag, content);
        }
    }
    public static void w(String tag, String content) {
        if (OPENLOG) {
            Log.w(tag, content);
        }
    }
    public static void e(String tag, String content) {
        if (OPENLOG) {
            Log.e(tag, content);
        }
    }
}