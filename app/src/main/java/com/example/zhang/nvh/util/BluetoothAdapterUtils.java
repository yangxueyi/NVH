package com.example.zhang.nvh.util;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.zhang.nvh.R;
import com.example.zhang.nvh.modlue.bluetooth.adapter.UseableAdapter;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by yang
 * Time 2017/8/30.
 */

public class BluetoothAdapterUtils {

    //搜索时间
    private static final long SCAN_PERIOD = 30000;

    Context context;
    @SuppressLint("HandlerLeak")
    private Handler mHandler;
    private ProgressBar mLoading;
    private boolean isScan;
    private BluetoothAdapter mBluetoothAdapter;

    public BluetoothAdapterUtils(Context context, ProgressBar loading){
        this.context = context;
        this.mLoading = loading;
        this.mHandler = new Handler();
    }


    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public  BluetoothAdapter  getBluetoothAdapter() {
        //判断是否支持BLE特性
        if (!context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(context, "不支持BLE特性", Toast.LENGTH_SHORT).show();
            return null;
        }else{
            //获取BluetoothAdapter
            BluetoothManager bluetoothManager =
                    (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
            mBluetoothAdapter = bluetoothManager.getAdapter();
            return mBluetoothAdapter;
        }
    }
    //搜索蓝牙设备
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void scanBluetooth(boolean enable, final UseableAdapter adapter, final BluetoothAdapter.LeScanCallback mLeScanCallback){

        if(Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP){
            if (enable) {
                // Stops scanning after a pre-defined scan period.
                mHandler.postDelayed(new Runnable() {
                    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
                    @Override
                    public void run() {
                        mBluetoothAdapter.stopLeScan(mLeScanCallback);
                        mLoading.setVisibility(View.INVISIBLE);
                    }
                }, SCAN_PERIOD);
                rotateAnim();
                mBluetoothAdapter.startLeScan(mLeScanCallback);
            } else {
                mBluetoothAdapter.stopLeScan(mLeScanCallback);
            }
        }else {
            //判断蓝牙是否打开
                final BluetoothLeScanner mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
                //5.0以上版本使用
                final ScanCallback mScanCallback = new ScanCallback() {
                    @Override
                    public void onScanResult(int callbackType, ScanResult result) {
                        super.onScanResult(callbackType, result);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            BluetoothDevice device = result.getDevice();
                            adapter.addDevice(device);
                            adapter.notifyDataSetChanged();
                        }
                    }
                    @Override
                    public void onBatchScanResults(List<ScanResult> results) {
                        super.onBatchScanResults(results);
                    }

                    @Override
                    public void onScanFailed(int errorCode) {
                        super.onScanFailed(errorCode);
                    }
                };
                if (enable) {
                    mHandler.postDelayed(new Runnable() {
                        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                        @Override
                        public void run() {
                            boolean enable = mBluetoothAdapter.isEnabled();//再次判断蓝牙是否开启
                            if(enable){
                                mBluetoothLeScanner.stopScan(mScanCallback);
                                mLoading.setVisibility(View.INVISIBLE);
                            }else{
                                ToastUtils.toast(context,"蓝牙已关闭");
                            }
                        }
                    }, SCAN_PERIOD);
                    rotateAnim();
                    mBluetoothLeScanner.startScan(mScanCallback);
                } else {
                    mBluetoothLeScanner.stopScan(mScanCallback);
                }
        }
    }

    /**
     * 与设备配对 参考源码：platform/packages/apps/Settings.git
     * /Settings/src/com/android/settings/bluetooth/CachedBluetoothDevice.java
     */
     public boolean createBond(Class btClass, BluetoothDevice btDevice) throws Exception {
        Method createBondMethod = btClass.getMethod("createBond");
        //invoke()方法主要是为了类反射，这样你可以在不知道具体的类的情况下，根据配置的字符串去调用一个类的方法。在灵活编程的时候非常有用。
        //很多框架代码都是这样去实现的。但是一般的编程，你是不需要这样做的，因为类都是你自己写的，怎么调用，怎么生成都是清楚的。
        Boolean returnValue = (Boolean) createBondMethod.invoke(btDevice);
        return returnValue.booleanValue();
    }

    /**
     * 与设备解除配对 参考源码：platform/packages/apps/Settings.git
     * /Settings/src/com/android/settings/bluetooth/CachedBluetoothDevice.java
     */
     public boolean removeBond(Class btClass, BluetoothDevice btDevice) throws Exception {
        Method removeBondMethod = btClass.getMethod("removeBond");
        Boolean returnValue = (Boolean) removeBondMethod.invoke(btDevice);
        return returnValue.booleanValue();
    }

    //图片的旋转动画
    private void rotateAnim(){
        Animation animation = AnimationUtils.loadAnimation(context, R.anim.loading);
        LinearInterpolator lin = new LinearInterpolator();//设置动画匀速运动
        animation.setInterpolator(lin);
        mLoading.startAnimation(animation);
    }
}
