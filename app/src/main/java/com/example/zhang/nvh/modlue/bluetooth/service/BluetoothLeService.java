package com.example.zhang.nvh.modlue.bluetooth.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.RequiresApi;
import android.util.Log;

/**
 * 特别说明：HC_BLE助手是广州汇承信息科技有限公司独自研发的手机APP，方便用户调试08蓝牙模块。
 * 本软件只能支持安卓版本4.3并且有蓝牙4.0的手机使用。
 * 另外对于自家的05、06模块，要使用另外一套蓝牙2.0的手机APP，用户可以在汇承官方网的下载中心自行下载。
 * 本软件提供代码和注释，免费给购买汇承08模块的用户学习和研究，但不能用于商业开发，最终解析权在广州汇承信息科技有限公司。
 * **/
public class BluetoothLeService extends Service {
    private final static String TAG = "BluetoothLeService";// luetoothLeService.class.getSimpleName();
    private List<Sensor> mEnabledSensors = new ArrayList<>();
    //蓝牙相关类
    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private String mBluetoothDeviceAddress;
    private BluetoothGatt mBluetoothGatt;
    private int mConnectionState = STATE_DISCONNECTED;

    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;

    public final static String ACTION_GATT_CONNECTED = "com.example.bluetooth.le.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED = "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED = "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE = "com.example.bluetooth.le.ACTION_DATA_AVAILABLE";
    public final static String EXTRA_DATA = "com.example.bluetooth.le.EXTRA_DATA";

    // Implements callback methods for GATT events that the app cares about. For
    // example,
    // connection change and services discovered.

    public interface OnDataAvailableListener {
        void onCharacteristicRead(BluetoothGatt gatt,
                                  BluetoothGattCharacteristic characteristic, int status);

         void onCharacteristicWrite(BluetoothGatt gatt,
                                          BluetoothGattCharacteristic characteristic);

         void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic);
    }

    public void setOnDataAvailableListener(OnDataAvailableListener l){
        OnDataAvailableListener mOnDataAvailableListener = l;
    }

    /* 连接远程设备的回调函数 */
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback(){
        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status,  int newState){
            String intentAction;
            if (newState == BluetoothProfile.STATE_CONNECTED){//连接成功
                intentAction = ACTION_GATT_CONNECTED;
                mConnectionState = STATE_CONNECTED;
				/* 通过广播更新连接状态 */
                broadcastUpdate(intentAction);
                Log.i(TAG, "Connected to GATT server.");
                // Attempts to discover services after successful connection.
                Log.i(TAG, "Attempting to start service discovery:"
                        + mBluetoothGatt.discoverServices());

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED){//连接失败
                intentAction = ACTION_GATT_DISCONNECTED;
                mConnectionState = STATE_DISCONNECTED;
                Log.i(TAG, "Disconnected from GATT server.");
                broadcastUpdate(intentAction);
            }
        }
        /*
         * 重写onServicesDiscovered，发现蓝牙服务
         * */
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS){//发现到服务
                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
                Log.i(TAG, "--onServicesDiscovered called--");
            } else{
                Log.w(TAG, "onServicesDiscovered received: " + status);
                System.out.println("onServicesDiscovered received: " + status);
            }
        }
        /*
         * 特征值的读写
         * */
        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic, int status){
            if (status == BluetoothGatt.GATT_SUCCESS){
                Log.i(TAG, "--onCharacteristicRead called--");
                //从特征值读取数据
                byte[] sucString = characteristic.getValue();
                String string = new String(sucString);
                //将数据通过广播到Ble_Activity
                broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
            }
        }
        /*
         * 特征值的改变
         * */
        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            System.out.println("++++++++++++++++");
            broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);

        }
        /*
         * 特征值的写
         * */
        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt,BluetoothGattCharacteristic characteristic, int status) {
            Log.w(TAG, "--onCharacteristicWrite--: " + status);
            // 以下语句实现 发送完数据或也显示到界面上
            broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
        }
        /*
         * 读描述值
         * */
        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
        @Override
        public void onDescriptorRead(BluetoothGatt gatt,  BluetoothGattDescriptor descriptor, int status){
            // super.onDescriptorRead(gatt, descriptor, status);
            Log.w(TAG, "----onDescriptorRead status: " + status);
            byte[] desc = descriptor.getValue();
            if (desc != null) {
                Log.w(TAG, "----onDescriptorRead value: " + new String(desc));
            }
        }
        /*
         * 写描述值
         * */
        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status){
            // super.onDescriptorWrite(gatt, descriptor, status);
            Log.w(TAG, "--onDescriptorWrite--: " + status);
        }
        /*
         * 读写蓝牙信号值
         * */
        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            // super.onReadRemoteRssi(gatt, rssi, status);
            Log.w(TAG, "--onReadRemoteRssi--: " + status);
            broadcastUpdate(ACTION_DATA_AVAILABLE, rssi);
        }

        @Override
        public void onReliableWriteCompleted(BluetoothGatt gatt, int status)  {
            // super.onReliableWriteCompleted(gatt, status);
            Log.w(TAG, "--onReliableWriteCompleted--: " + status);
        }

    };
    //广播意图
    private void broadcastUpdate(final String action, int rssi){
        final Intent intent = new Intent(action);
        intent.putExtra(EXTRA_DATA, String.valueOf(rssi));
        sendBroadcast(intent);
    }
    //广播意图
    private void broadcastUpdate(final String action){
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }

    /* 广播远程发送过来的数据 */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void broadcastUpdate(final String action, final BluetoothGattCharacteristic characteristic){
        final Intent intent = new Intent(action);
        //从特征值获取数据
        final byte[] data = characteristic.getValue();
        if (data != null && data.length > 0){
            final StringBuilder stringBuilder = new StringBuilder(data.length);
            for (byte byteChar : data) {
                stringBuilder.append(String.format("%02X ", byteChar));

                Log.i(TAG, "***broadcastUpdate: byteChar = " + byteChar);
            }
            intent.putExtra(EXTRA_DATA, new String(data));
            System.out.println("broadcastUpdate for  read data:" + new String(data));
        }
        sendBroadcast(intent);
    }

     public class LocalBinder extends Binder {
        public BluetoothLeService getService(){
            return BluetoothLeService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    public boolean onUnbind(Intent intent) {
        close();
        return super.onUnbind(intent);
    }

    private final IBinder mBinder = new LocalBinder();

    /**
     * Initializes a reference to the local Bluetooth adapter.
     * @return Return true if the initialization is successful.
     */
	/* service 中蓝牙初始化 */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public boolean initialize()  {
        // For API level 18 and above, get a reference to BluetoothAdapter
        // through
        // BluetoothManager.
        if (mBluetoothManager == null){   //获取系统的蓝牙管理器
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null){
                Log.e(TAG, "Unable to initialize BluetoothManager.");
                return false;
            }
        }

        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
            return false;
        }
        return true;
    }

    /**
     * Connects to the GATT server hosted on the Bluetooth LE device.
     * @param address The device address of the destination device.
     * @return Return true if the connection is initiated successfully. The
     *         connection result is reported asynchronously through the
     *         {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     *         callback.
     */
    // 连接远程蓝牙
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public boolean connect(final String address) {
        if (mBluetoothAdapter == null || address == null) {
            Log.e(TAG,"BluetoothAdapter not initialized or unspecified address.");
            return false;
        }

        // Previously connected device. Try to reconnect.
        //先前连接的设备。试着重新连接。
        if (mBluetoothDeviceAddress != null && address.equals(mBluetoothDeviceAddress)&& mBluetoothGatt != null){
            Log.e(TAG,"Trying to use an existing mBluetoothGatt for connection.");
            if (mBluetoothGatt.connect()){//连接蓝牙，其实就是调用BluetoothGatt的连接方法
                mConnectionState = STATE_CONNECTING;
                return true;
            } else  {
                return false;
            }
        }
		/* 获取远端的蓝牙设备 */
        final BluetoothDevice device = mBluetoothAdapter .getRemoteDevice(address);
        if (device == null) {
            Log.e(TAG, "Device not found.  Unable to connect.");
            return false;
        }
        // We want to directly connect to the device, so we are setting the
        // autoConnect
        // parameter to false.
		/* 调用device中的connectGatt连接到远程设备 */
        mBluetoothGatt = device.connectGatt(this, false, mGattCallback);
        Log.e(TAG, "Trying to create a new connection.");
        mBluetoothDeviceAddress = address;
        mConnectionState = STATE_CONNECTING;
        Log.e(TAG, "device.getBondState==" + device.getBondState());
        return true;
    }

    /**
     * Disconnects an existing connection or cancel a pending connection. The
     * disconnection result is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
	/*
	 * 取消连接
	 *
	 * */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void disconnect() {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.disconnect();
    }

    /**
     * After using a given BLE device, the app must call this method to ensure
     * resources are released properly.
     */

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void close(){
        if (mBluetoothGatt == null){
            return;
        }
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }

    /**
     * Request a read on a given {@code BluetoothGattCharacteristic}. The read
     * result is reported asynchronously through the
     * {@code BluetoothGattCallback#onCharacteristicRead(android.bluetooth.BluetoothGatt, android.bluetooth.BluetoothGattCharacteristic, int)}
     * callback.
     *
     * @param characteristic
     *            The characteristic to read from.
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void readCharacteristic(BluetoothGattCharacteristic characteristic){
        if (mBluetoothAdapter == null || mBluetoothGatt == null){
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.readCharacteristic(characteristic);

    }

    // 写入特征值
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void writeCharacteristic(BluetoothGattCharacteristic characteristic){
        if (mBluetoothAdapter == null || mBluetoothGatt == null)  {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.writeCharacteristic(characteristic);
    }

    // 读取RSSi
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void readRssi() {
        if (mBluetoothAdapter == null || mBluetoothGatt == null)  {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.readRemoteRssi();
    }

    /**
     * Enables or disables notification on a give characteristic.
     *
     * @param characteristic
     *            Characteristic to act on.
     * @param enabled
     *            If true, enable notification. False otherwise.
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void setCharacteristicNotification(  BluetoothGattCharacteristic characteristic, boolean enabled){
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);

        BluetoothGattDescriptor clientConfig = characteristic
                .getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"));

        if (enabled) {
            clientConfig.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        } else {
            clientConfig.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
        }
        mBluetoothGatt.writeDescriptor(clientConfig);
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void getCharacteristicDescriptor(BluetoothGattDescriptor descriptor) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.readDescriptor(descriptor);
    }

    /**
     * Retrieves a list of supported GATT services on the connected device. This
     * should be invoked only after {@code BluetoothGatt#discoverServices()}
     * completes successfully.
     *
     * @return A {@code List} of supported services.
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public List<BluetoothGattService> getSupportedGattServices(){
        if (mBluetoothGatt == null)
            return null;
        return mBluetoothGatt.getServices();

    }

}
