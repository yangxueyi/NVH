package com.example.zhang.nvh.modlue.setting.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

/**
 * Created by YangXueYi
 * Time： 2018/1/9.
 */

public class CallPhoneService extends Service {

    private TelephonyManager telephonyManager;
    private MyListener listener;

    //来电广播
    public final static String  CALL_PHONE = "com.example.communication.CALL_PHONE";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        //拿到电话管理器
        telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        listener = new MyListener();
        telephonyManager.listen(listener, PhoneStateListener.LISTEN_CALL_STATE);//监听电话状态
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        telephonyManager.listen(listener,PhoneStateListener.LISTEN_NONE);//取消监听
        listener = null;
    }



    class MyListener extends PhoneStateListener {

        private  String TAG = "MyListener";
        /*
         * 电话状态改变调用此方法
         */
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            super.onCallStateChanged(state, incomingNumber);
            //0代表空闲，1代表来电，2代表通话中
            switch (state){
                case TelephonyManager.CALL_STATE_IDLE://0

                    Log.e(TAG, "onCallStateChanged: 0");

                    break;
                case TelephonyManager.CALL_STATE_RINGING://1
                    Log.e(TAG, "onCallStateChanged: 1");
                    //来电就发送广播
                    broadcastUpdate(CALL_PHONE);
                    break;
                case TelephonyManager.CALL_STATE_OFFHOOK://2
                    Log.e(TAG, "onCallStateChanged: 2");
                    break;
                default:
                    break;
            }
        }
    }



    //广播意图
    private void broadcastUpdate(final String action){
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }
}
