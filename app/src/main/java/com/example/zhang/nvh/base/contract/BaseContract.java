package com.example.zhang.nvh.base.contract;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;

/**
 * Created by yang
 * Time 2017/8/24.
 */

public interface BaseContract {
    interface Model {
    }

    interface View {
        void onShowToast(String str);
        void onSkipActicity(Class<?> clasz,BluetoothDevice device);//页面跳转
        void onSkipActicity2(Class<?> clasz,int i);//页面跳转
    }

    interface Presenter {

    }
}
