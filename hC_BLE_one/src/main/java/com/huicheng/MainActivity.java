package com.huicheng;

import java.util.ArrayList;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.huicheng.ui.Ble_Activity;

/**
 * �ر�˵����HC_BLE�����ǹ��ݻ����Ϣ�Ƽ����޹�˾�����з����ֻ�APP�������û�����08����ģ�顣
 * �����ֻ��֧�ְ�׿�汾4.3����������4.0���ֻ�ʹ�á�
 * ��������Լҵ�05��06ģ�飬Ҫʹ������һ������2.0���ֻ�APP���û������ڻ�йٷ��������������������ء�
 * ������ṩ�����ע�ͣ���Ѹ�������08ģ����û�ѧϰ���о���������������ҵ���������ս���Ȩ�ڹ��ݻ����Ϣ�Ƽ����޹�˾��
 * **/
/**
 * @Description: TODO<MainActivity��ʵ�ִ�������ɨ������>
 * @author ���ݻ����Ϣ�Ƽ����޹�˾
 * @data: 2014-10-12 ����10:28:18
 * @version: V1.0
 * 
 */
public class MainActivity extends Activity implements OnClickListener {
	// ɨ��������ť
	private Button scan_btn;
	// ����������
	BluetoothAdapter mBluetoothAdapter;
	// �����ź�ǿ��
	private ArrayList<Integer> rssis;
	// �Զ���Adapter
	LeDeviceListAdapter mleDeviceListAdapter;
	// listview��ʾɨ�赽��������Ϣ
	ListView lv;
	// ����ɨ��������״̬
	private boolean mScanning;
	private boolean scan_flag;
	private Handler mHandler;
	int REQUEST_ENABLE_BT = 1;
	// ����ɨ��ʱ��
	private static final long SCAN_PERIOD = 10000;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		// ��ʼ���ؼ�
		init();
		// ��ʼ������
		init_ble();
		scan_flag = true;
		// �Զ���������
		mleDeviceListAdapter = new LeDeviceListAdapter();
		// Ϊlistviewָ��������
		lv.setAdapter(mleDeviceListAdapter);

		/* listview������� */
		lv.setOnItemClickListener(new AdapterView.OnItemClickListener()
		{

			@Override
			public void onItemClick(AdapterView<?> arg0, View v, int position,
					long id){
				// TODO Auto-generated method stub
				final BluetoothDevice device = mleDeviceListAdapter
						.getDevice(position);
				if (device == null)
					return;
				final Intent intent = new Intent(MainActivity.this,
						Ble_Activity.class);
				intent.putExtra(Ble_Activity.EXTRAS_DEVICE_NAME,device.getName());
				intent.putExtra(Ble_Activity.EXTRAS_DEVICE_ADDRESS,device.getAddress());
				intent.putExtra(Ble_Activity.EXTRAS_DEVICE_RSSI,rssis.get(position).toString());
				if (mScanning){
					/* ֹͣɨ���豸 */
					mBluetoothAdapter.stopLeScan(mLeScanCallback);
					mScanning = false;
				}

				try
				{
					// ����Ble_Activity
					startActivity(intent);
				} catch (Exception e)
				{
					e.printStackTrace();
					// TODO: handle exception
				}

			}
		});

	}

	/**
	 * @Title: init
	 * @Description: TODO(��ʼ��UI�ؼ�)
	 * @param ��
	 * @return void
	 * @throws
	 */
	private void init()
	{
		scan_btn = (Button) this.findViewById(R.id.scan_dev_btn);
		scan_btn.setOnClickListener(this);
		lv = (ListView) this.findViewById(R.id.lv);
		mHandler = new Handler();
	}

	/**
	 * @Title: init_ble
	 * @Description: TODO(��ʼ������)
	 * @param ��
	 * @return void
	 * @throws
	 */
	private void init_ble()
	{
		// �ֻ�Ӳ��֧������
		if (!getPackageManager().hasSystemFeature(
				PackageManager.FEATURE_BLUETOOTH_LE))
		{
			Toast.makeText(this, "��֧��BLE", Toast.LENGTH_SHORT).show();
			finish();
		}
		// Initializes Bluetooth adapter.
		// ��ȡ�ֻ����ص�����������
		final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
		mBluetoothAdapter = bluetoothManager.getAdapter();
		// ������Ȩ��
		if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled())
		{
			Intent enableBtIntent = new Intent(
					BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
		}

	}

	/*
	 * ��ť��Ӧ�¼�
	 */
	@Override
	public void onClick(View v)
	{
		// TODO Auto-generated method stub

		if (scan_flag)
		{
			mleDeviceListAdapter = new LeDeviceListAdapter();
			lv.setAdapter(mleDeviceListAdapter);
			scanLeDevice(true);
		} else
		{

			scanLeDevice(false);
			scan_btn.setText("ɨ���豸");
		}
	}

	/**
	 * @Title: scanLeDevice
	 * @Description: TODO(ɨ�������豸 )
	 * @param enable
	 *            (ɨ��ʹ�ܣ�true:ɨ�迪ʼ,false:ɨ��ֹͣ)
	 * @return void
	 * @throws
	 */
	private void scanLeDevice(final boolean enable)
	{
		if (enable)
		{
			// Stops scanning after a pre-defined scan period.
			mHandler.postDelayed(new Runnable()
			{
				@Override
				public void run()
				{
					mScanning = false;
					scan_flag = true;
					scan_btn.setText("ɨ���豸");
					Log.i("SCAN", "stop.....................");
					mBluetoothAdapter.stopLeScan(mLeScanCallback);
				}
			}, SCAN_PERIOD);
			/* ��ʼɨ�������豸����mLeScanCallback �ص����� */
			Log.i("SCAN", "begin.....................");
			mScanning = true;
			scan_flag = false;
			scan_btn.setText("ֹͣɨ��");
			mBluetoothAdapter.startLeScan(mLeScanCallback);
		} else
		{
			Log.i("Stop", "stoping................");
			mScanning = false;
			mBluetoothAdapter.stopLeScan(mLeScanCallback);
			scan_flag = true;
		}

	}

	/**
	 * ����ɨ��ص����� ʵ��ɨ�������豸���ص�����BluetoothDevice�����Ի�ȡname MAC����Ϣ
	 * 
	 * **/
	private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback()
	{

		@Override
		public void onLeScan(final BluetoothDevice device, final int rssi,
				byte[] scanRecord)
		{
			// TODO Auto-generated method stub

			runOnUiThread(new Runnable()
			{
				@Override
				public void run()
				{
					/* ��ɨ�赽�豸����Ϣ�����listview�������� */
					mleDeviceListAdapter.addDevice(device, rssi);
					mleDeviceListAdapter.notifyDataSetChanged();
				}
			});

			System.out.println("Address:" + device.getAddress());
			System.out.println("Name:" + device.getName());
			System.out.println("rssi:" + rssi);

		}
	};

	/**
	 * @Description: TODO<�Զ���������Adapter,��Ϊlistview��������>
	 * @author ���ݻ����Ϣ�Ƽ����޹�˾
	 * @data: 2014-10-12 ����10:46:30
	 * @version: V1.0
	 */
	private class LeDeviceListAdapter extends BaseAdapter {
		private ArrayList<BluetoothDevice> mLeDevices;

		private LayoutInflater mInflator;

		public LeDeviceListAdapter()
		{
			super();
			rssis = new ArrayList<Integer>();
			mLeDevices = new ArrayList<BluetoothDevice>();
			mInflator = getLayoutInflater();
		}

		public void addDevice(BluetoothDevice device, int rssi)
		{
			if (!mLeDevices.contains(device))
			{
				mLeDevices.add(device);
				rssis.add(rssi);
			}
		}

		public BluetoothDevice getDevice(int position)
		{
			return mLeDevices.get(position);
		}

		public void clear()
		{
			mLeDevices.clear();
			rssis.clear();
		}

		@Override
		public int getCount()
		{
			return mLeDevices.size();
		}

		@Override
		public Object getItem(int i)
		{
			return mLeDevices.get(i);
		}

		@Override
		public long getItemId(int i)
		{
			return i;
		}

		/**
		 * ��дgetview
		 * 
		 * **/
		@Override
		public View getView(int i, View view, ViewGroup viewGroup)
		{

			// General ListView optimization code.
			// ����listviewÿһ�����ͼ
			view = mInflator.inflate(R.layout.listitem, null);
			// ��ʼ������textview��ʾ������Ϣ
			TextView deviceAddress = (TextView) view
					.findViewById(R.id.tv_deviceAddr);
			TextView deviceName = (TextView) view
					.findViewById(R.id.tv_deviceName);
			TextView rssi = (TextView) view.findViewById(R.id.tv_rssi);

			BluetoothDevice device = mLeDevices.get(i);
			deviceAddress.setText(device.getAddress());
			deviceName.setText(device.getName());
			rssi.setText("" + rssis.get(i));

			return view;
		}
	}

}
