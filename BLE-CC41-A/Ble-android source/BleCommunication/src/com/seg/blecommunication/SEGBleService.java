/**
 * @Copyright 2014 by SEG, Inc. All rights reserved.
**/
package com.seg.blecommunication;

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
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

/**
 * <pre>
 * 
 * @author bume16
 * @date 2014. 6. 8.
 * @version 
 * </pre>
 **/
public class SEGBleService extends Service{
	private final static String TAG = SEGBleService.class.getSimpleName();
	/**
	 * 플래그 define
	 */
    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;

    public final static String ACTION_GATT_CONNECTED =
            "com.seg.blecommunication.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED =
            "com.seg.blecommunication.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED =
            "com.seg.blecommunication.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE =
            "com.seg.blecommunication.ACTION_DATA_AVAILABLE";
    public final static String EXTRA_DATA =
            "com.seg.blecommunication.EXTRA_DATA";
    
    public final static UUID UUID_HEART_RATE_MEASUREMENT =
            UUID.fromString(SEGGattAttributes.HEART_RATE_MEASUREMENT);
    public final static UUID UUID_START_OF_SIMPLE_KEYS_SERVICE =
            UUID.fromString(SEGGattAttributes.UUID_START_OF_SIMPLE_KEYS_SERVICE);
    public final static UUID UUID_KEY_PRESS_STATE_CHARACTERISTIC_VALUE =
            UUID.fromString(SEGGattAttributes.UUID_KEY_PRESS_STATE_CHARACTERISTIC_VALUE);
    /**
     * 블루투스 장치 매니저 선언 
     */
    private BluetoothManager mBluetoothManager = null;
    /**
     * 블루투스 장치 어댑터
     */
    private BluetoothAdapter mBluetoothAdapter = null;
    /**
     * 블루투스 장치의 주소 
     */
    private String mBluetoothDeviceAddress = null;    
    /**
     * 블루투스 일반 프로파일 클래스
     */
    private BluetoothGatt mBluetoothGatt = null;
    /**
     * 연결 상태 변수
     */
    private int mConnectionState = STATE_DISCONNECTED;
    
    public interface OnSerialListener{
    	public void OnReceivedValues(byte data[]);
    }
    private OnSerialListener mSerialListener = null;
    public void setOnSerialListener(OnSerialListener listener)
    {
    	mSerialListener = listener;
    }
    // Implements callback methods for GATT events that the app cares about.  For example,
    // connection change and services discovered.
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback(){

		@Override
		public void onCharacteristicChanged(BluetoothGatt gatt,
				BluetoothGattCharacteristic characteristic) {
			// TODO Auto-generated method stub
			if(characteristic.getUuid().equals(UUID_KEY_PRESS_STATE_CHARACTERISTIC_VALUE)){
				if(mSerialListener != null){
					mSerialListener.OnReceivedValues(characteristic.getValue());
				}
			}
			broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
		}

		@Override
		public void onCharacteristicRead(BluetoothGatt gatt,
				BluetoothGattCharacteristic characteristic, int status) {
			// TODO Auto-generated method stub
			if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
            }
		}

		@Override
		public void onCharacteristicWrite(BluetoothGatt gatt,
				BluetoothGattCharacteristic characteristic, int status) {
			// TODO Auto-generated method stub
			super.onCharacteristicWrite(gatt, characteristic, status);
		}

		@Override
		public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
			// TODO Auto-generated method stub
			String intentAction;
			//프로파일이 연결되었다!
			if(newState == BluetoothProfile.STATE_CONNECTED)
			{
				intentAction = ACTION_GATT_CONNECTED;
				mConnectionState = STATE_CONNECTED;
				broadcastUpdate(intentAction);
				//연결된 후 프로파일 서비스를 검색한다. 
				mBluetoothGatt.discoverServices();
			}
			//프로파일연결이 끊겼다.
			else if(newState == BluetoothProfile.STATE_DISCONNECTED)
			{
				intentAction = ACTION_GATT_DISCONNECTED;
				mConnectionState = STATE_DISCONNECTED;
				broadcastUpdate(intentAction);
			}
//			super.onConnectionStateChange(gatt, status, newState);
		}

		@Override
		public void onServicesDiscovered(BluetoothGatt gatt, int status) {
			// TODO Auto-generated method stub
			if(status == BluetoothGatt.GATT_SUCCESS){
				broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
			}else{
				//서비스 검색 실패
			}
		}
    	
    };
    
    /**
     * <PRE>
     * Comment : <br>
     * 브로드캐스트 메시지로 인텐트를 전송한다.
     * @author bume16
     * @date 2014. 6. 8.
     * </PRE>
     * @param action
     */
    private void broadcastUpdate(final String action)
    {
    	final Intent intent = new Intent(action);
    	sendBroadcast(intent);
    }
    
    /**
     * <PRE>
     * Comment : <br>
     * 브로드캐스트 메시지로 인텐트를 전송한다.
     * @author bume16
     * @date 2014. 6. 8.
     * </PRE>
     * @param action
     * @param characteristic
     */
    private void broadcastUpdate(final String action, final BluetoothGattCharacteristic characteristic)
    {
    	final Intent intent = new Intent(action);
    	
    	// This is special handling for the Heart Rate Measurement profile.  Data parsing is
        // carried out as per profile specifications:
        // http://developer.bluetooth.org/gatt/characteristics/Pages/CharacteristicViewer.aspx?u=org.bluetooth.characteristic.heart_rate_measurement.xml
        if (UUID_HEART_RATE_MEASUREMENT.equals(characteristic.getUuid())) {
            int flag = characteristic.getProperties();
            int format = -1;
            if ((flag & 0x01) != 0) {
                format = BluetoothGattCharacteristic.FORMAT_UINT16;
                Log.d(TAG, "Heart rate format UINT16.");
            } else {
                format = BluetoothGattCharacteristic.FORMAT_UINT8;
                Log.d(TAG, "Heart rate format UINT8.");
            }
            final int heartRate = characteristic.getIntValue(format, 1);
            Log.d(TAG, String.format("Received heart rate: %d", heartRate));
            intent.putExtra(EXTRA_DATA, String.valueOf(heartRate));
        }
        else {
            // For all other profiles, writes the data formatted in HEX.
            final byte[] data = characteristic.getValue();
            if (data != null && data.length > 0) {
                final StringBuilder stringBuilder = new StringBuilder(data.length);
                for(byte byteChar : data)
                    stringBuilder.append(String.format("%02X ", byteChar));
                intent.putExtra(EXTRA_DATA, new String(data) + "\n" + stringBuilder.toString());
            }
        }
        sendBroadcast(intent);
    }
    
    public class LocalBinder extends Binder {
    	public SEGBleService getService() {
            return SEGBleService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        // After using a given device, you should make sure that BluetoothGatt.close() is called
        // such that resources are cleaned up properly.  In this particular example, close() is
        // invoked when the UI is disconnected from the Service.
        close();
        return super.onUnbind(intent);
    }

    private final IBinder mBinder = new LocalBinder();
	
    /**
     * Initializes a reference to the local Bluetooth adapter.
     *
     * @return Return true if the initialization is successful.
     */
    public boolean initialize() {
        // For API level 18 and above, get a reference to BluetoothAdapter through
        // BluetoothManager.
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
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
     *
     * @param address The device address of the destination device.
     *
     * @return Return true if the connection is initiated successfully. The connection result
     *         is reported asynchronously through the
     *         {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     *         callback.
     */
    public boolean connect(final String address) {
        if (mBluetoothAdapter == null || address == null) {
            Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }

        // Previously connected device.  Try to reconnect.
        if (mBluetoothDeviceAddress != null && address.equals(mBluetoothDeviceAddress)
                && mBluetoothGatt != null) {
            Log.d(TAG, "Trying to use an existing mBluetoothGatt for connection.");
            if (mBluetoothGatt.connect()) {
                mConnectionState = STATE_CONNECTING;
                return true;
            } else {
                return false;
            }
        }

        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        if (device == null) {
            Log.w(TAG, "Device not found.  Unable to connect.");
            return false;
        }
        // We want to directly connect to the device, so we are setting the autoConnect
        // parameter to false.
        mBluetoothGatt = device.connectGatt(this, false, mGattCallback);
        Log.d(TAG, "Trying to create a new connection.");
        mBluetoothDeviceAddress = address;
        mConnectionState = STATE_CONNECTING;
        return true;
    }

    /**
     * Disconnects an existing connection or cancel a pending connection. The disconnection result
     * is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
    public void disconnect() {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.disconnect();
    }

    /**
     * After using a given BLE device, the app must call this method to ensure resources are
     * released properly.
     */
    public void close() {
        if (mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }
    
    /**
     * Request a read on a given {@code BluetoothGattCharacteristic}. The read result is reported
     * asynchronously through the {@code BluetoothGattCallback#onCharacteristicRead(android.bluetooth.BluetoothGatt, android.bluetooth.BluetoothGattCharacteristic, int)}
     * callback.
     *
     * @param characteristic The characteristic to read from.
     */
    public void readCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.readCharacteristic(characteristic);
    }

    /**
     * Enables or disables notification on a give characteristic.
     *
     * @param characteristic Characteristic to act on.
     * @param enabled If true, enable notification.  False otherwise.
     */
    public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic,
                                              boolean enabled) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);

        // This is specific to Heart Rate Measurement.
        if (UUID_HEART_RATE_MEASUREMENT.equals(characteristic.getUuid())) {
            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(
                    UUID.fromString(SEGGattAttributes.CLIENT_CHARACTERISTIC_CONFIG));
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            mBluetoothGatt.writeDescriptor(descriptor);
        }
    }

	public boolean writeCharacteristic(BluetoothGattCharacteristic characteristic){

    //check mBluetoothGatt is available
    if (mBluetoothGatt == null) {
        Log.e(TAG, "lost connection");
        return false;
    }
    if (characteristic == null) {
        Log.e(TAG, "char not found!");
        return false;
    }

    byte[] value = {0x31, 0x32};
    
    characteristic.setValue(value);
    boolean status = mBluetoothGatt.writeCharacteristic(characteristic);
    return status;
    }
	
	
	public boolean writeCharacteristic(byte[] writeBuffer)
	{
	    //check mBluetoothGatt is available
	    if (mBluetoothGatt == null) {
	        Log.e(TAG, "lost connection");
	        return false;
	    }
	    BluetoothGattService Service = mBluetoothGatt.getService(UUID_START_OF_SIMPLE_KEYS_SERVICE);	
	    if(Service == null){
	    	Log.e(TAG, "service not found!");
	        return false;
	    }
	    BluetoothGattCharacteristic charac = Service.getCharacteristic(UUID_KEY_PRESS_STATE_CHARACTERISTIC_VALUE);
	    if (charac == null) {
	        Log.e(TAG, "char not found!");
	        return false;
	    }
	    charac.setValue(writeBuffer);
		boolean status = mBluetoothGatt.writeCharacteristic(charac);
	    return status;
	}
	
	/**
	 * <PRE>
	 * Comment : <br>
	 * 키 입력 서비스를 notify로 등록한다
	 * @author bume16
	 * @date 2014. 6. 10.
	 * </PRE>
	 */
	public boolean setStartofSimpleKeysServiceNotify()
	{
	    //check mBluetoothGatt is available
	    if (mBluetoothGatt == null) {
	        Log.e(TAG, "lost connection");
	        return false;
	    }
	    BluetoothGattService Service = mBluetoothGatt.getService(UUID_START_OF_SIMPLE_KEYS_SERVICE);
	    if(Service == null){
	    	Log.e(TAG, "service not found!");
	        return false;
	    }
	    BluetoothGattCharacteristic charac = Service.getCharacteristic(UUID_KEY_PRESS_STATE_CHARACTERISTIC_VALUE);
	    if (charac == null) {
	        Log.e(TAG, "char not found!");
	        return false;
	    }
	    
	    	   
	    	    
		setCharacteristicNotification(charac, true);
		return true;
	}

    /**
     * Retrieves a list of supported GATT services on the connected device. This should be
     * invoked only after {@code BluetoothGatt#discoverServices()} completes successfully.
     *
     * @return A {@code List} of supported services.
     */
    public List<BluetoothGattService> getSupportedGattServices() {
        if (mBluetoothGatt == null) return null;

        return mBluetoothGatt.getServices();
    }
    
    
}
