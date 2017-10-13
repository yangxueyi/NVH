package com.example.zhang.nvh.modlue.bluetooth.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import com.example.zhang.nvh.R;
import com.example.zhang.nvh.base.BaseActivity;
import com.example.zhang.nvh.modlue.bluetooth.adapter.PairAdapter;
import com.example.zhang.nvh.modlue.bluetooth.adapter.UseableAdapter;
import com.example.zhang.nvh.modlue.bluetooth.contract.BlueContract;
import com.example.zhang.nvh.modlue.bluetooth.presenter.BluePresenter;
import com.example.zhang.nvh.ui.MyItemDecoration;
import com.example.zhang.nvh.ui.MyLinearLayoutManager;
import com.example.zhang.nvh.util.BluetoothAdapterUtils;
import com.example.zhang.nvh.util.ToastUtils;


/**
 * Created by yang
 * Time 2017/8/29.
 */

public class BluetoothActivtity extends BaseActivity implements BlueContract.View,View.OnClickListener{


    private Toolbar toolbar;
    private LinearLayout lin_blue;
    private ImageButton image_btn_blue;
    private BluePresenter bluePresenter;


    //定义一个boolean确定是打开按钮还是关闭按钮
    private BluetoothAdapter mBluetoothAdapter;
    private final static int RESULT_OK_1 = 1;
    private ScrollView scroll;
    private RecyclerView recycle_usable;
    private RecyclerView recycle_pair;

    private UseableAdapter mUseableAdapter;

    private ProgressBar loading;
    private PairAdapter mPairAdapter;
    private ImageView image_search;

    //定义一个boolean值，以确定是关闭搜索还是开始搜索
    private boolean startOrStop;
    private TextView tv_pair;
    private BluetoothGatt mBluetoothGatt;


    @Override
    protected void initLayout(Bundle savedInstanceState) {
        setContentView(R.layout.activity_bluetooth);
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    protected void initView() {

        toolbar = (Toolbar) findViewById(R.id.tool_bar);
        lin_blue = (LinearLayout) findViewById(R.id.lin_blue);
        tv_pair = (TextView) findViewById(R.id.tv_pair);
        image_btn_blue = (ImageButton) findViewById(R.id.image_btn_blue);
        scroll = (ScrollView) findViewById(R.id.scroll);
        recycle_pair = (RecyclerView) findViewById(R.id.recycle_pair);
        recycle_usable = (RecyclerView) findViewById(R.id.recycle_usable);
        image_search = (ImageView) findViewById(R.id.image_search);

        loading = (ProgressBar) findViewById(R.id.loading);

        //设置toolbar
        toolbar.setTitle("");
        toolbar.setNavigationIcon(R.mipmap.back);//返回按钮
        setSupportActionBar(toolbar);

        //创建Presenter
        bluePresenter = new BluePresenter(this,this);
        //获取蓝牙适配器
        getBluetoothAdapter();

        //判断是否支持BLE特性
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
           onShowToast("不支持");

            finish();
        }
        //动态授权处理所需权限
        requestPermission();
    }

    /**动态授权处理所需权限*/
    protected void requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int checkAccessFinePermission = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
            if (checkAccessFinePermission != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        1);
                Log.e(getPackageName(), "没有权限，请求权限");
                bluePresenter.onStopSearchBluetooth(mBluetoothAdapter);
                loading.setVisibility(View.INVISIBLE);
                return;
            }
            Log.e(getPackageName(), "已有定位权限"); //这里可以开始搜索操作
            //开始搜索
            bluePresenter.onSearchBluetooth(mBluetoothAdapter);
            loading.setVisibility(View.VISIBLE);
        }else{
            //开始搜索
            bluePresenter.onSearchBluetooth(mBluetoothAdapter);
            loading.setVisibility(View.VISIBLE);
        }
    }
    /**动态授权返回结果*/
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.e(getPackageName(), "开启权限permission granted!"); //这里可以开始搜索操作
                    if(mPairAdapter == null){
                        tv_pair.setVisibility(View.GONE);
                    }
                    //开始搜索
                    bluePresenter.onSearchBluetooth(mBluetoothAdapter);
                    loading.setVisibility(View.VISIBLE);
                } else {
                    Log.e(getPackageName(), "没有定位权限，请先开启!");
                    //下面的方法最好写一个跳转，可以直接跳转到权限设置页面，方便用户
                    getAppDetailSettingIntent();
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
    /**
     * 跳转到权限设置界面
     */
    @SuppressLint("ObsoleteSdkInt")
    private void getAppDetailSettingIntent(){
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if(Build.VERSION.SDK_INT >= 9){
            intent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
            intent.setData(Uri.fromParts("package", getPackageName(), null));
        } else if(Build.VERSION.SDK_INT <= 8){
            intent.setAction(Intent.ACTION_VIEW);
            intent.setClassName("com.android.settings","com.android.settings.InstalledAppDetails");
            intent.putExtra("com.android.settings.ApplicationPkgName", getPackageName());
        }
        startActivity(intent);
    }


    @Override
    protected void initListener() {
        //返回按钮
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bluePresenter.onBack();
            }
        });

        lin_blue.setOnClickListener(this);
        image_btn_blue.setOnClickListener(this);
        image_search.setOnClickListener(this);

        //usable列表的点击事件
        bluePresenter.onUsableItemClick();
        //设置已配对设备的点击事件
        bluePresenter.onPairItemClick();

    }
    //toolbar上面的返回键
    @Override
    public void onToolbarBack() {
        //返回上一个界面的时候，再次确认蓝牙是否开启，并将结果传过去
        boolean enabled = mBluetoothAdapter.isEnabled();
        Intent intent = new Intent();
        intent.putExtra("enabled",enabled);
        setResult(RESULT_OK_1,intent);
        finish();
    }
    @Override
    protected void onResume() {
        super.onResume();

        //可用设备列表的设备布局管理器
        MyLinearLayoutManager linearLayoutManager = new MyLinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        linearLayoutManager.setScrollEnabled(false);//设置为false为不可滑动;
        recycle_usable.setLayoutManager(linearLayoutManager);
        recycle_usable.addItemDecoration(new MyItemDecoration(this, LinearLayoutManager.VERTICAL));
    }

    //返回键
    @Override
    public void onBackPressed() {
        //返回上一个界面的时候，再次确认蓝牙是否开启，并将结果传过去
        boolean enabled = mBluetoothAdapter.isEnabled();
        Intent intent = new Intent();
        intent.putExtra("enabled",enabled);
        setResult(RESULT_OK_1,intent);
        finish();
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    public void onSkipActicity(Class<?> clasz,BluetoothDevice device) {
        super.onSkipActicity(clasz,device);
        Intent intent = new Intent(this,clasz);
        intent.putExtra(Ble_Activity.EXTRAS_DEVICE_NAME,device.getName());
        intent.putExtra(Ble_Activity.EXTRAS_DEVICE_ADDRESS,device.getAddress());
        startActivity(intent);
//        intent.putExtra(Ble_Activity.EXTRAS_DEVICE_RSSI,rssis.get(position).toString());

    }

    @Override
    public void openOrCloseBluetooth() {
        //判断蓝牙是否打开，如果没打开就打开，如果打开了就关闭
        if (mBluetoothAdapter.isEnabled()) {//静默开启
            image_btn_blue.setBackgroundResource(R.mipmap.btn_close);
            mBluetoothAdapter.disable();//关闭
            scroll.setVisibility(View.INVISIBLE);
            image_search.setImageResource(R.mipmap.refresh_1);
            image_search.setEnabled(false);
            mUseableAdapter.clearDevic();
            mPairAdapter.clearDevic();
//            bluePresenter.onStopSearchBluetooth(mBluetoothAdapter);
        }else{
            boolean enable = mBluetoothAdapter.enable();//开启
            if (enable) {//因为6.0以上需要动态授权，所以需要判断到底是选择打开还是关闭
                SystemClock.sleep(2000);//必须睡一段时间，确保蓝牙已经打开
                image_btn_blue.setBackgroundResource(R.mipmap.btn_open);
                scroll.setVisibility(View.VISIBLE);
                loading.setVisibility(View.VISIBLE);
                bluePresenter.onSearchBluetooth(mBluetoothAdapter);
                image_search.setImageResource(R.mipmap.stop_1);
                image_search.setEnabled(true);
                startOrStop = false;
                getPairBluetooth();
            } else {
                image_btn_blue.setBackgroundResource(R.mipmap.btn_close);
                scroll.setVisibility(View.INVISIBLE);
                image_search.setImageResource(R.mipmap.refresh_1);
                image_search.setEnabled(false);
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.lin_blue:
                if(mBluetoothAdapter!=null)
                    bluePresenter.onOpenOrCloseBluetooth();
                break;
            case R.id.image_btn_blue:
                if(mBluetoothAdapter!=null)
                    bluePresenter.onOpenOrCloseBluetooth();
                break;
            case R.id.image_search:
                if(startOrStop) {
                    mUseableAdapter.clearDevic();
                    bluePresenter.onSearchBluetooth(mBluetoothAdapter);
                    loading.setVisibility(View.VISIBLE);
                    image_search.setImageResource(R.mipmap.stop_1);
                    startOrStop = false;
                }else{
                    bluePresenter.onStopSearchBluetooth(mBluetoothAdapter);
                    loading.setVisibility(View.INVISIBLE);
                    image_search.setImageResource(R.mipmap.refresh_2);
                    startOrStop = true;
                }
                break;
        }
    }


    //接收传过来的adapter
    @Override
    public void giveAdapter( RecyclerView.Adapter useableAdapter) {
        mUseableAdapter = (UseableAdapter) useableAdapter;
        recycle_usable.setAdapter(mUseableAdapter);
    }
    //接收传过来的adapter
    @Override
    public void giveAdapter2( RecyclerView.Adapter pairAdapter) {
        mPairAdapter = (PairAdapter) pairAdapter;

        if(mPairAdapter.getItemCount() == 0){
            tv_pair.setVisibility(View.GONE);
            Log.e("giveAdapter2: ","mPairAdapter = " +mPairAdapter );
        }else
            recycle_pair.setAdapter(mPairAdapter);
    }

    @Override
    public void showProgressbar() {
        //已经搜索完成
        loading.setVisibility(View.INVISIBLE);
        image_search.setImageResource(R.mipmap.refresh_2);
        startOrStop = true;
    }
    @Override
    public void onShowToast(String str) {
        ToastUtils.toast(this,str);
    }

    /**获取蓝牙适配器并判断蓝牙是否开启*/
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void  getBluetoothAdapter() {
        BluetoothAdapterUtils bluetoothAdapterUtils = new BluetoothAdapterUtils(this, loading);
        mBluetoothAdapter = bluetoothAdapterUtils.getBluetoothAdapter();

        //判断蓝牙是否打开
        boolean enabled = mBluetoothAdapter.isEnabled();
        if(enabled){//打开，按钮就显示亮色
            image_btn_blue.setBackgroundResource(R.mipmap.btn_open);
            scroll.setVisibility(View.VISIBLE);
            image_search.setImageResource(R.mipmap.stop_1);
            //获取已经配对的蓝牙设备
            getPairBluetooth();
        }else{//关闭，就显示暗色
            image_btn_blue.setBackgroundResource(R.mipmap.btn_close);
            scroll.setVisibility(View.INVISIBLE);
            image_search.setImageResource(R.mipmap.refresh_1);
            image_search.setEnabled(false);
            ToastUtils.toast(this,"蓝牙未开启，请开启！");
        }
    }

    public void getPairBluetooth() {
        //设备布局管理器
        MyLinearLayoutManager linearLayoutManager = new MyLinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        linearLayoutManager.setScrollEnabled(false);//设置为false为不可滑动;
        recycle_pair.setLayoutManager(linearLayoutManager);
        recycle_pair.addItemDecoration(new MyItemDecoration(this, LinearLayoutManager.VERTICAL));

        bluePresenter.getPairBluetooth(mBluetoothAdapter);
    }
}
