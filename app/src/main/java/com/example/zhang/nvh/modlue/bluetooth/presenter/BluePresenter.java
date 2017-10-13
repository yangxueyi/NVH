package com.example.zhang.nvh.modlue.bluetooth.presenter;

import android.appwidget.AppWidgetManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.example.zhang.nvh.listener.MyItemOnClickListener;
import com.example.zhang.nvh.modlue.bluetooth.activity.Ble_Activity;
import com.example.zhang.nvh.modlue.bluetooth.adapter.PairAdapter;
import com.example.zhang.nvh.modlue.bluetooth.adapter.UseableAdapter;
import com.example.zhang.nvh.modlue.bluetooth.contract.BlueContract;

import java.util.Set;

/**
 * Created by yang
 * Time 2017/8/29.
 */

public class BluePresenter implements BlueContract.Presenter {

    //搜索时间
    private static final long SCAN_PERIOD = 30000;

    private Context context;
    private BlueContract.View mView;
    private UseableAdapter mUseableAdapter;
    private PairAdapter mPairAdapter;
    private BluetoothDevice device;
    private TextView tv_pair_usable;
    private BluetoothGatt mBluetoothGatt;


    public BluePresenter(Context context, BlueContract.View view){
        this.context = context;
        this.mView = view;
        this.mUseableAdapter = new UseableAdapter();
        this.mPairAdapter = new PairAdapter();

      openSearchBluetooth();

    }

    private void openSearchBluetooth(){
        //每搜索到一个设备就会发送一个该广播
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        context.registerReceiver(receiver, filter);
        //当全部搜索完后发送该广播
         filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        context.registerReceiver(receiver, filter);
        //配对请求发送该广播
//        filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
//        context.registerReceiver(receiver, filter);

       /* filter.addAction(BluetoothDevice.ACTION_PAIRING_REQUEST);
        filter.setPriority(Integer.MAX_VALUE);
        context.registerReceiver(receiver, filter);*/
    }

    @Override
    public void onBack() {
        mView.onToolbarBack();
    }

    @Override
    public void onOpenOrCloseBluetooth(){
        mView.openOrCloseBluetooth();
    }

    @Override
    public void onSearchBluetooth(BluetoothAdapter mBluetoothAdapter) {


        //如果当前在搜索，就先取消搜索
        if (mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.cancelDiscovery();
            //关闭广播
            stopBroadcastReceiver();
        }
        //开启广播
        openSearchBluetooth();
        //开启搜索
        mBluetoothAdapter.startDiscovery();
        //将arrayAdapter传过去
        mView.giveAdapter(mUseableAdapter);
    }

    @Override
    public void onStopSearchBluetooth(BluetoothAdapter mBluetoothAdapter) {
        //关闭广播
        stopBroadcastReceiver();
        //如果当前在搜索，就取消搜索
        if (mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.cancelDiscovery();
        }
    }


    //获取已配对设备
    @Override
    public void getPairBluetooth(BluetoothAdapter mBluetoothAdapter) {
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            for(BluetoothDevice device :pairedDevices){
                mPairAdapter.addDevice(device);
            }
        }
        mView.giveAdapter2(mPairAdapter);
    }

    @Override
    public void onUsableItemClick() {
        mUseableAdapter.setItemOnClickListener(new MyItemOnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
            @Override
            public void onItemOnClick(View view, int postion,int isClick) throws Exception {

//                //点击拿到条目信息：BluetoothDevice
                device = mUseableAdapter.getDevice(postion);
                String address = device.getAddress();
                mView.onSkipActicity(Ble_Activity.class,device);

                mView.onShowToast("address = "+ address);


//                //拿到控件
//                tv_pair_usable = (TextView) view.findViewById(R.id.tv_pair_usable);
////                connect(device);
//                try {
//                    ClsUtils.setPin(device.getClass(),device,strPaw);//与蓝牙采集器配对
//                    ClsUtils.createBond(BluetoothDevice.class, device);
//
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
            }

            @Override
            public void onItemOnLongClick(View view, int postion) throws Exception {

            }
        });
    }

    @Override
    public void onPairItemClick() {

        mPairAdapter.setItemOnClickListener(new MyItemOnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
            @Override
            public void onItemOnClick(View view, int postion, int isClick) throws Exception {
//                mView.onShowToast("postion = " +postion);
                BluetoothDevice device = mPairAdapter.getDevice(postion);
                mView.onSkipActicity(Ble_Activity.class,device);
                mView.onShowToast("address = "+ device.getAddress());
            }

            @Override
            public void onItemOnLongClick(View view, int postion) throws Exception {

            }
        });
    }

    //停止广播
    private void stopBroadcastReceiver(){
        context.unregisterReceiver(receiver);
    }

    /**
     * 定义蓝牙搜索广播接收器
     */
    private final BroadcastReceiver receiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();
            //发现蓝牙设备
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    mUseableAdapter.addDevice(device);
                    mUseableAdapter.notifyDataSetChanged();//更新适配器
                }
                //搜索完成
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                //已搜索完成
    //                mView.onShowToast("0");
                mView.showProgressbar();
                //蓝牙配对是否成功或取消
            }/* else if(BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)){
                    switch (device.getBondState()) {
                        case BluetoothDevice.BOND_BONDING:
                            Log.e("dfsdfds", " 正在配对");
                            tv_pair_usable.setVisibility(View.VISIBLE);//设置条目的显示与隐藏
                            break;
                        case BluetoothDevice.BOND_BONDED:
                            Log.e("dfsdfds", " 完成配对");
                            tv_pair_usable.setVisibility(View.GONE);//设置条目的显示与隐藏
                            //配对成功，将点击的蓝牙设备从可用列表中删除，并添加到已配对设备
                            mUseableAdapter.deleteDevic(device);
                            mPairAdapter.addDevice(device);
                            mUseableAdapter.notifyDataSetChanged();
                            mPairAdapter.notifyDataSetChanged();
                            break;
                        case BluetoothDevice.BOND_NONE:
                            Log.e("dfsdfds", " 取消配对");
                            tv_pair_usable.setVisibility(View.GONE);//设置条目的显示与隐藏
                            break;
                    }
            }*/
        }
    };


    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
     private void connect(BluetoothDevice device ) {
        if (device == null) {
            return;
        }
        mBluetoothGatt = device.connectGatt(context, false, mGattCallback);
    }

    private final BluetoothGattCallback mGattCallback= new BluetoothGattCallback() {
        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
        @Override  //当连接上设备或者失去连接时会回调该函数
        public void onConnectionStateChange(BluetoothGatt gatt, int status,int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) { //连接成功
                mBluetoothGatt.discoverServices(); //连接成功后就去找出该设备中的服务 private BluetoothGatt mBluetoothGatt;
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {  //连接失败
            }
        }
        @Override  //当设备是否找到服务时，会回调该函数
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {   //找到服务了
                //在这里可以对服务进行解析，寻找到你需要的服务

            } else {
            }
        }
        @Override  //当读取设备时会回调该函数
        public void onCharacteristicRead(BluetoothGatt gatt,BluetoothGattCharacteristic characteristic, int status) {
            System.out.println("onCharacteristicRead");
            if (status == BluetoothGatt.GATT_SUCCESS) {
                //读取到的数据存在characteristic当中，可以通过characteristic.getValue();函数取出。然后再进行解析操作。
                //int charaProp = characteristic.getProperties();if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0)表示可发出通知。  判断该Characteristic属性
            }
        }

        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
        @Override //当向设备Descriptor中写数据时，会回调该函数
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            System.out.println("onDescriptorWriteonDescriptorWrite = " + status + ", descriptor =" + descriptor.getUuid().toString());
        }

        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
        @Override //设备发出通知时会调用到该接口
        public void onCharacteristicChanged(BluetoothGatt gatt,BluetoothGattCharacteristic characteristic) {
            if (characteristic.getValue() != null) {
                System.out.println(characteristic.getStringValue(0));
            }
            System.out.println("--------onCharacteristicChanged-----");
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            System.out.println("rssi = " + rssi);
        }
        @Override //当向Characteristic写数据时会回调该函数
        public void onCharacteristicWrite(BluetoothGatt gatt,BluetoothGattCharacteristic characteristic, int status) {
            System.out.println("--------write success----- status:" + status);
        }
    };

}
