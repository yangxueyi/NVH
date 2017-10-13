package com.example.zhang.nvh.modlue.setting;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.zhang.nvh.R;
import com.example.zhang.nvh.base.BaseActivity;
import com.example.zhang.nvh.modlue.setting.contract.SettingContract;
import com.example.zhang.nvh.modlue.setting.presenter.SettingPresenter;
import com.example.zhang.nvh.util.BluetoothAdapterUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by yang
 * Time 2017/8/24.
 */

public class SettingActivity extends BaseActivity implements SettingContract.View,View.OnClickListener {

    private Button btnSyn;
    private Button btnManage;
    private Button btnStop;
    private Button btnStart;
    private LinearLayout lin_layout_wifi;
    private LinearLayout lin_layout_microphone;
    private LinearLayout lin_layout_datas;
    private LinearLayout lin_layout_frequency;
    private LinearLayout lin_layout_bluetooth;
    private SettingPresenter settingPresenter;
    private TextView tv_blue;
    private Chronometer timer;
    private long chronometerSeconds;
    //设置一个变量，记录按钮是开始、暂停、继续
    int startOrPauseSound = 1;

    private final UUID MY_UUID = UUID
            .fromString("abcd1234-ab12-ab12-ab12-abcdef123456");//和客户端相同的UUID
    private final String NAME = "Bluetooth_Socket";
    private BluetoothServerSocket serverSocket;
    private BluetoothSocket socket;

    private BluetoothAdapter mBluetoothAdapter;


    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            Toast.makeText(getApplicationContext(), String.valueOf(msg.obj),
                    Toast.LENGTH_LONG).show();
            super.handleMessage(msg);
        }
    };

    @Override
    protected void initLayout(Bundle savedInstanceState) {
        setContentView(R.layout.activity_setting);
    }

    //初始化View
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    protected void initView() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.tool_bar);
        btnStart = (Button) findViewById(R.id.btn_start);
        btnStop = (Button) findViewById(R.id.btn_stop);
        btnManage = (Button) findViewById(R.id.btn_manage);
        btnSyn = (Button) findViewById(R.id.btn_syn);
        lin_layout_bluetooth = (LinearLayout) findViewById(R.id.lin_layout_bluetooth);
        lin_layout_frequency = (LinearLayout) findViewById(R.id.lin_layout_frequency);
        lin_layout_datas = (LinearLayout) findViewById(R.id.lin_layout_datas);
        lin_layout_microphone = (LinearLayout) findViewById(R.id.lin_layout_microphone);
        lin_layout_wifi = (LinearLayout) findViewById(R.id.lin_layout_wifi);
        timer = (Chronometer) findViewById(R.id.timer);
        tv_blue = (TextView) findViewById(R.id.tv_blue);


        //设置toolbar

//        toolbar.setNavigationIcon(R.mipmap.back);//返回按钮
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        settingPresenter = new SettingPresenter(this, this);

        //获取蓝牙适配器
        getBluetoothAdapter();

        requestPermission();

        //创建音频文件临时保存路径
        settingPresenter.setPath();

    }

    @Override
    protected void initListener() {
        btnStart.setOnClickListener(this);
        btnStop.setOnClickListener(this);
        btnManage.setOnClickListener(this);
        btnSyn.setOnClickListener(this);
        lin_layout_bluetooth.setOnClickListener(this);
        lin_layout_frequency.setOnClickListener(this);
        lin_layout_datas.setOnClickListener(this);
        lin_layout_microphone.setOnClickListener(this);
        lin_layout_wifi.setOnClickListener(this);
    }

    /**
     * 动态授权处理所需权限
     */
    private void requestPermission() {

        //声明一个数组permissions，将需要的权限都放在里面
        String[] permissions = new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        //创建一个list，逐个判断哪些权限未授予，未授予的权限存储到mPerrrmissionList中
        List<String> mPermissionList = new ArrayList<>();
        mPermissionList.clear();

        /* 判断哪些权限未授予 */
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                mPermissionList.add(permission);
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            /* 判断是否为空*/
            if (mPermissionList.isEmpty()) {//未授予的权限为空，表示都授予了
                //如果已经有了权限直接进行操作
                btnStart.setText("开始");
            } else {//请求权限方法
                permissions = mPermissionList.toArray(new String[mPermissionList.size()]);
                ActivityCompat.requestPermissions(this, permissions, 1);
            }
        } else {
            //版本小于23直接进行操作
            btnStart.setText("开始");
        }
    }

    /**
     * 动态授权返回结果
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.e(getPackageName(), "开启权限permission granted!");
                    //可以开始操作
                    btnStart.setText("开始");
                } else {
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
    private void getAppDetailSettingIntent() {
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (Build.VERSION.SDK_INT >= 9) {
            intent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
            intent.setData(Uri.fromParts("package", getPackageName(), null));
        } else if (Build.VERSION.SDK_INT <= 8) {
            intent.setAction(Intent.ACTION_VIEW);
            intent.setClassName("com.android.settings", "com.android.settings.InstalledAppDetails");
            intent.putExtra("com.android.settings.ApplicationPkgName", getPackageName());
        }
        startActivity(intent);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_start:
                settingPresenter.onClickStart();
                break;
            case R.id.btn_stop:
                settingPresenter.onClickStop();
                break;
            case R.id.btn_manage:
                settingPresenter.onClickManage(R.id.btn_manage);
                break;
            case R.id.btn_syn:

                break;
            case R.id.lin_layout_bluetooth:
                settingPresenter.onItemClick(R.id.lin_layout_bluetooth);
                break;
            case R.id.lin_layout_frequency:
                settingPresenter.onItemClick(R.id.lin_layout_frequency);
                break;
            case R.id.lin_layout_datas:
                settingPresenter.onItemClick(R.id.lin_layout_datas);
                break;
            case R.id.lin_layout_microphone:
                settingPresenter.onItemClick(R.id.lin_layout_microphone);
                break;
            case R.id.lin_layout_wifi:
                settingPresenter.onItemClick(R.id.lin_layout_wifi);
                break;

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        int RESULT_OK_1 = 1;
        if (resultCode == RESULT_OK_1) {
            Bundle extras = data.getExtras();
            boolean enabled = extras.getBoolean("enabled");//拿到上一个界面传过来的信息
            if (enabled) {//根据传过来的信息确定显示的文字
                tv_blue.setText("已开启");
            } else {
                tv_blue.setText("关闭");
            }
        }

        super.onActivityResult(requestCode, resultCode, data);

    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void getBluetoothAdapter() {
        BluetoothAdapterUtils bluetoothAdapterUtils = new BluetoothAdapterUtils(this, null);
        mBluetoothAdapter = bluetoothAdapterUtils.getBluetoothAdapter();

        //判断蓝牙是否打开
        boolean enabled = mBluetoothAdapter.isEnabled();
        Log.e( "getBluetoothAdapter: ","enabled = " +enabled );
        if (enabled) {
            tv_blue.setText("已开启");
        } else {
            tv_blue.setText("关闭");
        }
    }

    @Override
    public void startSound() {
        if (startOrPauseSound == 1) {
            timer.setBase(SystemClock.elapsedRealtime());//计时器清零
            int hour = (int) ((SystemClock.elapsedRealtime() - timer.getBase()) / 1000 / 60);
            timer.setFormat("0" + String.valueOf(hour) + ":%s");
            timer.start();
//            startRecord();


            settingPresenter.startRecord();
            btnStart.setText("暂停");
            startOrPauseSound = 2;
        } else if (startOrPauseSound == 2) {
            //获取Chronometer 计时器的计时时间
            chronometerSeconds = SystemClock.elapsedRealtime() - timer.getBase();
            timer.stop();
            settingPresenter.pauseRecord();
            btnStart.setText("继续");
            startOrPauseSound = 3;
        } else if (startOrPauseSound == 3) {
            timer.setBase(SystemClock.elapsedRealtime() - chronometerSeconds);//继续计时
            timer.start();
            settingPresenter.startRecord();
            btnStart.setText("暂停");
            startOrPauseSound = 2;

        }
    }

    @Override
    public void stopSound() {
        //结束计时后，将时间置为零
        chronometerSeconds = 0;
        Log.e("onClick: ", "time = " + chronometerSeconds);
        timer.stop();
        timer.setBase(SystemClock.elapsedRealtime());//计时器清零
        settingPresenter.stopRecord();
        btnStart.setText("开始");
        startOrPauseSound = 1;
    }

    // 来电暂停
    @Override
    protected void onPause() {
        settingPresenter.callPhone();
        super.onPause();
    }




    public class AcceptThread extends Thread {

        private InputStream is;

        public AcceptThread() {
            try {
                serverSocket = mBluetoothAdapter.listenUsingRfcommWithServiceRecord(NAME, MY_UUID);
            } catch (Exception e) {
            }
        }

        public void run() {
            try {
                socket = serverSocket.accept();
                is = socket.getInputStream();
                while(true) {
                    byte[] buffer =new byte[1024*10];
                    int count = is.read(buffer);
                    Message msg = new Message();
                    msg.obj = new String(buffer, 0, count, "utf-8");
                    Log.e("hahahaha", "信息="+ String.valueOf(msg.obj));
                    handler.sendMessage(msg);
                }
            }catch (Exception e) {
            }finally {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
