package com.example.zhang.nvh.modlue.bluetooth.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;


import com.example.zhang.nvh.R;
import com.example.zhang.nvh.modlue.bluetooth.service.BluetoothLeService;
import com.example.zhang.nvh.util.ToastUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * 特别说明：HC_BLE助手是广州汇承信息科技有限公司独自研发的手机APP，方便用户调试08蓝牙模块。
 * 本软件只能支持安卓版本4.3并且有蓝牙4.0的手机使用。
 * 另外对于自家的05、06模块，要使用另外一套蓝牙2.0的手机APP，用户可以在汇承官方网的下载中心自行下载。
 * 本软件提供代码和注释，免费给购买汇承08模块的用户学习和研究，但不能用于商业开发，最终解析权在广州汇承信息科技有限公司。
 * **/
@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
public class Ble_Activity extends Activity implements OnClickListener {

	private final static String TAG = Ble_Activity.class.getSimpleName();
    //蓝牙4.0的UUID,其中0000ffe1-0000-1000-8000-00805f9b34fb是广州汇承信息科技有限公司08蓝牙模块的UUID
    public static String HEART_RATE_MEASUREMENT = "0000ffe1-0000-1000-8000-00805f9b34fb";
    public static String EXTRAS_DEVICE_NAME = "DEVICE_NAME";;
    public static String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";
    public static String EXTRAS_DEVICE_RSSI = "RSSI";
    //蓝牙连接状态
    private boolean mConnected = false;
    private String status = "disconnected";
    //蓝牙名字
    private String mDeviceName;
    //蓝牙地址
    private String mDeviceAddress;
    //蓝牙信号值
    private String mRssi;
    private String rev_str = "";
    //蓝牙service,负责后台的蓝牙服务
    private static BluetoothLeService mBluetoothLeService;
    //文本框，显示接受的内容
    private TextView rev_tv, connect_state;
    //发送按钮
    private Button send_btn;
    //文本编辑框
    private EditText send_et;
    private ScrollView rev_sv;
    private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics = new ArrayList<>();
    //蓝牙特征值
    private static BluetoothGattCharacteristic target_chara = null;
    private Handler mhandler = new Handler();
    @SuppressLint("HandlerLeak")
    private Handler myHandler = new Handler(){
        // 2.重写消息处理函数
        public void handleMessage(Message msg){
            switch (msg.what) {
                // 判断发送的消息
                case 1:{
                    // 更新View
                    String state = msg.getData().getString("connect_state");
                    connect_state.setText(state);

                    break;
                }
            }
            super.handleMessage(msg);
        }

    };

	@Override
	protected void onCreate(Bundle savedInstanceState){
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ble_activity);
        Bundle b = getIntent().getExtras();
        //从意图获取显示的蓝牙信息
		mDeviceName = b.getString(EXTRAS_DEVICE_NAME);
		mDeviceAddress = b.getString(EXTRAS_DEVICE_ADDRESS);

		/* 启动蓝牙service */
		Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
		bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
		init();

	}

	@Override
	protected void onDestroy(){
		super.onDestroy();
        //解除广播接收器
		unregisterReceiver(mGattUpdateReceiver);
		unbindService(mServiceConnection);
		mBluetoothLeService = null;
	}

    // Activity出来时候，绑定广播接收器，监听蓝牙连接服务传过来的事件
	@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
	protected void onResume(){
		super.onResume();
        //绑定广播接收器
		registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
		if (mBluetoothLeService != null){
            //根据蓝牙地址，建立连接
			final boolean result = mBluetoothLeService.connect(mDeviceAddress);
			Log.e(TAG, "Connect request result=" + result);
		}
	}

	private void init(){
		rev_sv = (ScrollView) this.findViewById(R.id.rev_sv);
		rev_tv = (TextView) this.findViewById(R.id.rev_tv);
		connect_state = (TextView) this.findViewById(R.id.connect_state);
		send_btn = (Button) this.findViewById(R.id.send_btn);
		connect_state.setText(status);
		send_btn.setOnClickListener(this);
	}

    /* BluetoothLeService绑定的回调函数 */
	private final ServiceConnection mServiceConnection = new ServiceConnection(){

		@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
        @Override
		public void onServiceConnected(ComponentName componentName,IBinder service){
			mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
			if (!mBluetoothLeService.initialize()){
				Log.e(TAG, "Unable to initialize Bluetooth");
				finish();
			}
            // 根据蓝牙地址，连接设备
			mBluetoothLeService.connect(mDeviceAddress);
            Log.e(TAG, "123456789");
		}
		@Override
		public void onServiceDisconnected(ComponentName componentName){
            Log.e(TAG, "987654321");
			mBluetoothLeService = null;
		}
	};

    /**
     * 广播接收器，负责接收BluetoothLeService类发送的数据
     */
	private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver(){
		@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
        @Override
		public void onReceive(Context context, Intent intent){
			final String action = intent.getAction();
			if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)){//Gatt连接成功
				mConnected = true;
				status = "connected";
                //更新连接状态
				updateConnectionState(status);
                ToastUtils.toast(Ble_Activity.this,"连接成功");
				System.out.println("BroadcastReceiver :" + "device connected");
			} else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)){//Gatt连接失败
				mConnected = false;
				status = "disconnected";
                //更新连接状态
				updateConnectionState(status);
                ToastUtils.toast(Ble_Activity.this,"连接断开");
				System.out.println("BroadcastReceiver :"+ "device disconnected");
			} else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)){//发现GATT服务器
				// Show all the supported services and characteristics on the
				// user interface.
				///获取设备的所有蓝牙服务
				displayGattServices(mBluetoothLeService.getSupportedGattServices());
				System.out.println("BroadcastReceiver :"+ "device SERVICES_DISCOVERED");
			} else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)){    //有效数据
                //处理发送过来的数据
				displayData(intent.getExtras().getString(BluetoothLeService.EXTRA_DATA));
				System.out.println("BroadcastReceiver onData:"+ intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
			}
		}
	};

    /* 更新连接状态 */
	private void updateConnectionState(String status)
	{
		Message msg = new Message();
		msg.what = 1;
		Bundle b = new Bundle();
		b.putString("connect_state", status);
		msg.setData(b);
        //将连接状态更新的UI的textview上
		myHandler.sendMessage(msg);
		System.out.println("connect_state:" + status);
	}

    /* 意图过滤器 */
	private static IntentFilter makeGattUpdateIntentFilter()
	{
		final IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
		intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
		intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
		intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
		return intentFilter;
	}


	//处理接受的数据
	private void displayData(String rev_string){
		rev_str += rev_string ;
		runOnUiThread(new Runnable(){
			@Override
			public void run(){
				rev_tv.setText(rev_str);
				rev_sv.scrollTo(0, rev_tv.getMeasuredHeight());
				System.out.println("rev:" + rev_str);
			}
		});
	}

	//处理蓝牙服务
	@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void displayGattServices(List<BluetoothGattService> gattServices){

		if (gattServices == null)
            return;
    String uuid = null;
    String unknownServiceString = "unknown_service";
    String unknownCharaString = "unknown_characteristic";

    // 服务数据,可扩展下拉列表的第一级数据
    ArrayList<HashMap<String, String>> gattServiceData = new ArrayList<>();

    // 特征数据（隶属于某一级服务下面的特征值集合）
    ArrayList<ArrayList<HashMap<String, String>>> gattCharacteristicData = new ArrayList<>();

    // 部分层次，所有特征值集合
    mGattCharacteristics = new ArrayList<>();

    // Loops through available GATT Services.
		for (BluetoothGattService gattService : gattServices) {

            // 获取服务列表
            HashMap<String, String> currentServiceData = new HashMap<String, String>();
            uuid = gattService.getUuid().toString();

            // 查表，根据该uuid获取对应的服务名称。SampleGattAttributes这个表需要自定义。

            gattServiceData.add(currentServiceData);

            System.out.println("Service uuid:" + uuid);

            ArrayList<HashMap<String, String>> gattCharacteristicGroupData = new ArrayList<HashMap<String, String>>();

            // 从当前循环所指向的服务中读取特征值列表
            List<BluetoothGattCharacteristic> gattCharacteristics = gattService
                    .getCharacteristics();

            ArrayList<BluetoothGattCharacteristic> charas = new ArrayList<>();

            // Loops through available Characteristics.
            // 对于当前循环所指向的服务中的每一个特征值
            for (final BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                charas.add(gattCharacteristic);
                HashMap<String, String> currentCharaData = new HashMap<>();
                uuid = gattCharacteristic.getUuid().toString();

                if (gattCharacteristic.getUuid().toString()
                        .equals(HEART_RATE_MEASUREMENT)) {
                    // 测试读取当前Characteristic数据，会触发mOnDataAvailable.onCharacteristicRead()
                    mhandler.postDelayed(new Runnable() {

                        @Override
                        public void run() {
                            // TODO Auto-generated method stub
                            mBluetoothLeService
                                    .readCharacteristic(gattCharacteristic);
                        }
                    }, 200);

                    // 接受Characteristic被写的通知,收到蓝牙模块的数据后会触发mOnDataAvailable.onCharacteristicWrite()
                    mBluetoothLeService.setCharacteristicNotification(
                            gattCharacteristic, true);
                    target_chara = gattCharacteristic;
                    // 设置数据内容
                    // 往蓝牙模块写入数据
                    // mBluetoothLeService.writeCharacteristic(gattCharacteristic);
                }
                List<BluetoothGattDescriptor> descriptors = gattCharacteristic
                        .getDescriptors();
                for (BluetoothGattDescriptor descriptor : descriptors) {
                    System.out.println("---descriptor UUID:"
                            + descriptor.getUuid());
                    // 获取特征值的描述
                    mBluetoothLeService.getCharacteristicDescriptor(descriptor);
                    // mBluetoothLeService.setCharacteristicNotification(gattCharacteristic,
                    // true);
                }

                gattCharacteristicGroupData.add(currentCharaData);
            }
            // 按先后顺序，分层次放入特征值集合中，只有特征值
            mGattCharacteristics.add(charas);
            // 构件第二级扩展列表（服务下面的特征值）
            gattCharacteristicData.add(gattCharacteristicGroupData);
        }
    }

	/* 
	 *  手动连接
	 */
	@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
	public void onClick(View v){
        mBluetoothLeService.connect(mDeviceAddress);
        Log.e(TAG, "onClick: " + "连接" );
    }
}
