package com.example.zhang.nvh.modlue.bluetooth.contract;

import android.bluetooth.BluetoothAdapter;
import android.support.v7.widget.RecyclerView;

import com.example.zhang.nvh.base.contract.BaseContract;

/**
 * Created by yang
 * Time 2017/8/29.
 */

public interface BlueContract {
    interface Model {
    }

    interface View extends BaseContract.View{
        void onToolbarBack();
        void openOrCloseBluetooth();
        void giveAdapter( RecyclerView.Adapter useableAdapter);//将可用设备适配器传过去
        void giveAdapter2( RecyclerView.Adapter pairAdapter);//将已配对设备适配器传过去
        void showProgressbar();
    }

    interface Presenter extends BaseContract.Presenter{
        void onBack();
        void onOpenOrCloseBluetooth();//判断打开或关闭蓝牙
        void onUsableItemClick();//可用设备列表点击事件
        void onPairItemClick();//已配对设备列表点击事件
        void onSearchBluetooth(BluetoothAdapter mBluetoothAdapter);//搜索可用设备
        void onStopSearchBluetooth(BluetoothAdapter mBluetoothAdapter);//停止搜索
        void getPairBluetooth(BluetoothAdapter mBluetoothAdapter);//获取已配对蓝牙设备
    }
}
