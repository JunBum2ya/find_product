package com.seg.bleterminal;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import com.seg.blecommunication.SEGBleService;
import com.seg.blecommunication.SEGBleService.OnSerialListener;
import com.seg.blecommunication.SEGGattAttributes;
import com.seg.bleterminal.DevInfoSectionFragment.OnDevListClickListener;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class MainActivity extends FragmentActivity implements OnDevListClickListener,ExpandableListView.OnChildClickListener {

	/**
	 * The {@link android.support.v4.view.PagerAdapter} that will provide
	 * fragments for each of the sections. We use a
	 * {@link android.support.v4.app.FragmentPagerAdapter} derivative, which
	 * will keep every loaded fragment in memory. If this becomes too memory
	 * intensive, it may be best to switch to a
	 * {@link android.support.v4.app.FragmentStatePagerAdapter}.
	 */
	private SectionsPagerAdapter mSectionsPagerAdapter;

	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	private ViewPager mViewPager;
	
    /**
     * 블루투스 장치 어댑터
     */
    private BluetoothAdapter mBluetoothAdapter = null;
    /**
     * 검색중 플래그
     */
    private boolean mScanning = false;
    /**
     * 메인 핸들러
     */
    private Handler mHandler = null;
    
    
    private SEGBleService mBluetoothLeService = null;
    private boolean mConnected = false;
    private BluetoothGattCharacteristic mNotifyCharacteristic;
    private String mDeviceAddress = "00:00:00:00:00:00";
    private String mDeviceName = "null";
    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((SEGBleService.LocalBinder)service).getService();
            if (!mBluetoothLeService.initialize()) {
                finish();
            }
            // Automatically connects to the device upon successful start-up initialization.
            mBluetoothLeService.connect(mDeviceAddress);
            mBluetoothLeService.setOnSerialListener(mSerialListener);

            
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
        
    };
    
	
    private static final int REQUEST_ENABLE_BT = 1;
    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 10000;

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBluetoothLeService != null) {
            final boolean result = mBluetoothLeService.connect(mDeviceAddress);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mGattUpdateReceiver);
    }

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mHandler = new Handler();
		//BLE 초기화
		initBle();
		
		// Create the adapter that will return a fragment for each of the three
		// primary sections of the app.
		mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);
		((DevInfoSectionFragment)mSectionsPagerAdapter.getItem(0)).setOnDevListClickListener(this);
		((DevInfoSectionFragment)mSectionsPagerAdapter.getItem(0)).setOnExpandableListViewChildClickListener(this);
		
		mViewPager.setOnPageChangeListener(new OnPageChangeListener() {
			
			@Override
			public void onPageSelected(int position) {
				// TODO Auto-generated method stub
				if(position == 1){	//serial screen
					if(mBluetoothLeService != null)
						setListenKeyInput();
				}
			}
			
			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onPageScrollStateChanged(int arg0) {
				// TODO Auto-generated method stub
				
			}
		});
		
		
		// ble connect service start
        Intent gattServiceIntent = new Intent(this, SEGBleService.class);
        if(bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE))
        {
        	
        }
        else{
        	Toast.makeText(this, "service bind fail", Toast.LENGTH_SHORT).show();
        }
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	/**
	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
	 * one of the sections/tabs/pages.
	 */
	public class SectionsPagerAdapter extends FragmentPagerAdapter  {

		private Map<Integer, Fragment> mSections = new HashMap<Integer, Fragment>();
		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
			mSections.put(0, DevInfoSectionFragment.newInstance());
			mSections.put(1, SerialCommSectionFragment.newInstance());
			mSections.put(2, AppInfoSectionFragment.newInstance());
		}

		@Override
		public Fragment getItem(int position) {
			return mSections.get(position);
		}

		@Override
		public int getCount() {
			// Show 3 total pages.
			return 3;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			Locale l = Locale.getDefault();
			switch (position) {
			case 0:
				return getString(R.string.title_section1).toUpperCase(l);
			case 1:
				return getString(R.string.title_section2).toUpperCase(l);
			case 2:
				return getString(R.string.title_section3).toUpperCase(l);
			}
			return null;
		}
	}
	
	/**
	 * <PRE>
	 * Comment : <br>
	 * 스마트폰은 블루투스 장치를 초기화 한다.
	 * @author bume16
	 * @date 2014. 6. 8.
	 * </PRE>
	 */
	private void initBle()
	{
		// Use this check to determine whether BLE is supported on the device.  Then you can
        // selectively disable BLE-related features.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }

        // Initializes a Bluetooth adapter.  For API level 18 and above, get a reference to
        // BluetoothAdapter through BluetoothManager.
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        // Checks if Bluetooth is supported on the device.
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }
	}
	
    /**
     * <PRE>
     * Comment : <br>
     * BLE 장치를 스캔한다.
     * @author bume16
     * @date 2014. 6. 8.
     * </PRE>
     * @param enable
     */
    private void scanLeDevice(final boolean enable) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                }
            }, SCAN_PERIOD);

            mScanning = true;
            mBluetoothAdapter.startLeScan(mLeScanCallback);
        } else {
            mScanning = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
    }
    
    // Device scan callback.
    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                	((DevInfoSectionFragment)mSectionsPagerAdapter.getItem(0)).addBluetoothDevice(device);
                }
            });
        }
    };

    /**
     * <PRE>
     * Comment : <br>
     * BLE 스캔 시작 버튼
     * @author bume16
     * @date 2014. 6. 8.
     * </PRE>
     * @param view
     */
    public void onBleScan(View view)
    {
    	scanLeDevice(true);
    }

	@Override
	public void OnListItemClicked(BluetoothDevice device) {
		// TODO Auto-generated method stub
		mDeviceAddress = device.getAddress();
		mDeviceName = device.getName();
		
		if(mConnected){			
			mProcessHandler.sendEmptyMessage(ProcessHandler.BLE_CONNECT_STOP);
			mProcessHandler.sendEmptyMessageDelayed(ProcessHandler.BLE_CONNECT_START,500);
		}
		else{			
			mProcessHandler.sendEmptyMessage(ProcessHandler.BLE_CONNECT_START);
		}
			
		
	}
	
	/**
	 * 2014.06.08
	 * 메시지 처리 핸들러 정의
	 */
	private Handler mProcessHandler = new Handler(new ProcessHandler());
	private class ProcessHandler implements Handler.Callback{
		
		public static final int BLE_CONNECT_START = 0;
		public static final int BLE_CONNECT_STOP = 1;
		public static final int BLE_SERIAL_READ = 2;

		@Override
		public boolean handleMessage(Message msg) {
			// TODO Auto-generated method stub
			switch(msg.what){
			case BLE_CONNECT_START:
				if(mBluetoothLeService != null)
					mBluetoothLeService.connect(mDeviceAddress);
				break;
			case BLE_CONNECT_STOP:
				if(mBluetoothLeService != null)
					mBluetoothLeService.close();
				break;
			case BLE_SERIAL_READ:
				((SerialCommSectionFragment)mSectionsPagerAdapter.getItem(1)).updateLogbox((String)msg.obj);
				break;
			}
			
			return false;
		}
	}

    // Handles various events fired by the Service.
    // ACTION_GATT_CONNECTED: connected to a GATT server.
    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
    // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
    // ACTION_DATA_AVAILABLE: received data from the device.  This can be a result of read
    //                        or notification operations.
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (SEGBleService.ACTION_GATT_CONNECTED.equals(action)) {
                mConnected = true;
                ((DevInfoSectionFragment)mSectionsPagerAdapter.getItem(0)).showInfoLayout();
                ((DevInfoSectionFragment)mSectionsPagerAdapter.getItem(0)).updateConnectionState("Connected");
                
            } else if (SEGBleService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;
                ((DevInfoSectionFragment)mSectionsPagerAdapter.getItem(0)).showScanLayout();
                
            } else if (SEGBleService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // Show all the supported services and characteristics on the user interface.
            	((DevInfoSectionFragment)mSectionsPagerAdapter.getItem(0)).displayGattServices(mBluetoothLeService.getSupportedGattServices());
//                displayGattServices(mBluetoothLeService.getSupportedGattServices());
                
            } else if (SEGBleService.ACTION_DATA_AVAILABLE.equals(action)) {
            	((DevInfoSectionFragment)mSectionsPagerAdapter.getItem(0)).displayData(intent.getStringExtra(SEGBleService.EXTRA_DATA));
//                displayData(intent.getStringExtra(SEGBleService.EXTRA_DATA));
                
            }
        }
    };
    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(SEGBleService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(SEGBleService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(SEGBleService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(SEGBleService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
        unbindService(mServiceConnection);
        mBluetoothLeService = null;
	}

	@Override
	public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
		// TODO Auto-generated method stub
		DevInfoSectionFragment fm = ((DevInfoSectionFragment)mSectionsPagerAdapter.getItem(0));
        
		if (fm.getGattCharacteristics() != null) {
          final BluetoothGattCharacteristic characteristic =  fm.getGattCharacteristics().get(groupPosition).get(childPosition);
          final int charaProp = characteristic.getProperties();
			if ((charaProp | BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
				// If there is an active notification on a characteristic,
				// clear
				// it first so it doesn't update the data field on the user
				// interface.
				if (mNotifyCharacteristic != null) {
					mBluetoothLeService.setCharacteristicNotification(
							mNotifyCharacteristic, false);
					mNotifyCharacteristic = null;
				}
				mBluetoothLeService.readCharacteristic(characteristic);
			}

			if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
				mNotifyCharacteristic = characteristic;
				mBluetoothLeService.setCharacteristicNotification(characteristic, true);
			}
          
          return true;
      }
      return false;
	}
	
	public void setListenKeyInput()
	{
		mBluetoothLeService.setStartofSimpleKeysServiceNotify();
	}
	private OnSerialListener mSerialListener = new OnSerialListener(){

		@Override
		public void OnReceivedValues(byte[] data) {
			// TODO Auto-generated method stub
			mProcessHandler.obtainMessage(ProcessHandler.BLE_SERIAL_READ, new String(data)).sendToTarget();
		}
		
	};
	
	public void OnSendMessage(View view)
	{
		if(mBluetoothLeService != null){
			String msg = ((SerialCommSectionFragment)mSectionsPagerAdapter.getItem(1)).getInputMessage();
			if(isEnter)
			{
				msg += "\r\n";
			}
			if(isEcho){
				mProcessHandler.obtainMessage(ProcessHandler.BLE_SERIAL_READ, msg).sendToTarget();
			}
			mBluetoothLeService.writeCharacteristic(msg.getBytes());	
		}
		
	}
	
	private boolean isEcho = false;
	private boolean isEnter = false;
	
	public void OnEnterToggle(View view)
	{
		isEnter = ((ToggleButton)view).isChecked();
	}
	
	public void OnEchoToggle(View view)
	{
		isEcho = ((ToggleButton)view).isChecked();
	}

}
