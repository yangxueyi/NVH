package com.example.zhang.nvh.modlue.bluetooth.receiver;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.example.zhang.nvh.util.ClsUtils;

/**
 * Created by yang
 * Time 2017/9/4.
 */

public class RequestReceiver extends BroadcastReceiver {
    @android.support.annotation.RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action.equals("android.bluetooth.device.action.PAIRING_REQUEST")) {
            BluetoothDevice btDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            try {
                int mType = intent.getIntExtra(BluetoothDevice.EXTRA_PAIRING_VARIANT, BluetoothDevice.ERROR);

                String password = "123456";
                boolean isSuccess = false;
                switch (mType) {
                    case 0:
                        //当接收到配对请求时自动配对
                        isSuccess = ClsUtils.setPin(btDevice.getClass(), btDevice, password );
                        break;
                    case 1:
                        int passKey = Integer.parseInt(password);
                        isSuccess = ClsUtils.setPassKey(btDevice.getClass(), btDevice, passKey);
                        break;
                }
                if (isSuccess) {
                    abortBroadcast();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

