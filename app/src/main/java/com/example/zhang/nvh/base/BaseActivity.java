package com.example.zhang.nvh.base;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;

import com.example.zhang.nvh.base.contract.BaseContract;
import com.example.zhang.nvh.util.ToastUtils;

/**
 * Created by yang
 * Time 2017/8/24.
 */

public abstract class BaseActivity extends AppCompatActivity implements BaseContract.View{

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        initLayout(savedInstanceState);
        initView();
        initListener();

    }

    /** 初始化布局 */
    protected abstract void initLayout(Bundle savedInstanceState);
    /** 初始化View 主要是使用findviewbyid的 如果你用的是ButterKnife 这个方法可以忽略*/
    protected abstract void initView() ;
    /** 所有监听的方法都在该类中实现*/
    protected abstract void initListener();


    @Override
    public void onShowToast(String str) {
        ToastUtils.toast(getApplicationContext(),str);
    }
    @Override
    public void onSkipActicity(Class<?> clasz,BluetoothDevice device) {
    }

    @Override
    public void onSkipActicity2(Class<?> clasz, int i) {
    }
}
