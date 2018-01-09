package com.example.audiolibrary;

import android.app.Application;
import android.content.Context;

/**
 * Created by YangXueYi
 * Time： 2018/1/8.
 */

public class MyApplication extends Application {

    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
    }

    /*获取全局变量*/
    public static Context getContext(){
        return context;
    }
}
