/**
 * @Copyright 2014 by SEG, Inc. All rights reserved.
**/
package com.seg.bleterminal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.seg.blecommunication.SEGGattAttributes;
import com.seg.blecommunication.SEGLeDeviceListAdapter;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;

/**
 * <pre>
 * 블루투스 장치를 검색하고 정보를 보여줄 프래그 먼트
 * DevInfoSectionFragment.java
 * @author bume16
 * @date 2014. 6. 4.
 * @version 
 * </pre>
 **/
public class DevInfoSectionFragment extends Fragment implements OnItemClickListener{

	
	/**
	 * 메인 컨텍스트 
	 */
	private Context mCtx = null;
	/**
	 * 찾은 블루투스 장치를 보여줄 리스트
	 */
	private ListView mFindDevList = null;
	/**
	 * 블루투스 장치 어답터
	 */
	private SEGLeDeviceListAdapter mDevListAdapter = null;
	
	/**
	 * 장치 스캔 레이아웃
	 */
	private LinearLayout mScanLayout = null;
	/**
	 * 장치 정보 레이아웃
	 */
	private LinearLayout mInfoLayout = null;
	
	private TextView mDeviceAddress = null;
	private TextView mConnectionState = null;
	private TextView mDataValue = null;
    private ExpandableListView mGattServicesList;
    private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics =
            new ArrayList<ArrayList<BluetoothGattCharacteristic>>();
    private BluetoothGattCharacteristic mNotifyCharacteristic;
    private final String LIST_NAME = "NAME";
    private final String LIST_UUID = "UUID";
	
    
	public interface OnDevListClickListener{
		public void OnListItemClicked(BluetoothDevice device);
	}
	private OnDevListClickListener mListener = null;
	public void setOnDevListClickListener(OnDevListClickListener listener)
	{
		mListener = listener;
	}
    // newInstance constructor for creating fragment with arguments
    public static DevInfoSectionFragment newInstance() {
    	
    	DevInfoSectionFragment fragment = new DevInfoSectionFragment();
    	
    	// Supply index input as an argument.
        Bundle args = new Bundle();
        
        fragment.setArguments(args);
        
        return fragment;
    }

    public DevInfoSectionFragment()
    {
    	
    }
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		View rootView = inflater.inflate(R.layout.device_information_fragment, container, false);
		
		//initial fragment layout
		mFindDevList = (ListView)rootView.findViewById(R.id.listFindedLeDevices);
		mFindDevList.setOnItemClickListener(this);
		mDevListAdapter = new SEGLeDeviceListAdapter(mCtx);		
		mFindDevList.setAdapter(mDevListAdapter);
		mScanLayout = (LinearLayout)rootView.findViewById(R.id.DevScanLayout);
		mInfoLayout = (LinearLayout)rootView.findViewById(R.id.DevInfoLayout);
		
		mDeviceAddress = (TextView)rootView.findViewById(R.id.device_address);
		mConnectionState = (TextView)rootView.findViewById(R.id.connection_state);
		mDataValue = (TextView)rootView.findViewById(R.id.data_value);
		
		mGattServicesList = (ExpandableListView)rootView.findViewById(R.id.gatt_services_list);
    	mGattServicesList.setOnChildClickListener(mServicesListClickListner);
		
		return rootView;//super.onCreateView(inflater, container, savedInstanceState);
	}

	/**
	 * <PRE>
	 * Comment : <br>
	 * 검색된 장치를 리스트에 추가한다.
	 * @author bume16
	 * @date 2014. 6. 8.
	 * </PRE>
	 * @param device
	 */
	public void addBluetoothDevice(BluetoothDevice device)
	{
		mDevListAdapter.addDevice(device);
    	mDevListAdapter.notifyDataSetChanged();
	}

	@Override
	public void onAttach(Activity activity) {
		// TODO Auto-generated method stub
		super.onAttach(activity);
		mCtx = activity;
	}

	@Override
	public void onDetach() {
		// TODO Auto-generated method stub
		super.onDetach();
		mCtx = null;
	}

	@Override
	public void onItemClick(AdapterView<?> l, View v, int position, long id) {
		// TODO Auto-generated method stub
		if(mListener != null)
			mListener.OnListItemClicked(mDevListAdapter.getDevice(position));
		
	}
	
	/**
	 * <PRE>
	 * Comment : <br>
	 * 장치 검색 레이아웃을 보여준다
	 * @author bume16
	 * @date 2014. 6. 9.
	 * </PRE>
	 */
	public void showScanLayout()
	{
		mScanLayout.setVisibility(View.VISIBLE);
		mInfoLayout.setVisibility(View.INVISIBLE);
	}
	
	/**
	 * <PRE>
	 * Comment : <br>
	 * 장치정보 레이아웃을 보여준다.
	 * @author bume16
	 * @date 2014. 6. 9.
	 * </PRE>
	 */
	public void showInfoLayout()
	{
		mScanLayout.setVisibility(View.INVISIBLE);
		mInfoLayout.setVisibility(View.VISIBLE);
	}
	
	public ArrayList<ArrayList<BluetoothGattCharacteristic>> getGattCharacteristics()
	{
		return mGattCharacteristics;
	}
	
	
	public void updateConnectionState(String str)
	{
		mConnectionState.setText(str);        
	}
	
    public void displayData(String data) {
        if (data != null) {
        	mDataValue.setText(data);
        }
    }

    // Demonstrates how to iterate through the supported GATT Services/Characteristics.
    // In this sample, we populate the data structure that is bound to the ExpandableListView
    // on the UI.
    public void displayGattServices(List<BluetoothGattService> gattServices) {
        if (gattServices == null) return;
        String uuid = null;
        String unknownServiceString = getResources().getString(R.string.unknown_service);
        String unknownCharaString = getResources().getString(R.string.unknown_characteristic);
        ArrayList<HashMap<String, String>> gattServiceData = new ArrayList<HashMap<String, String>>();
        ArrayList<ArrayList<HashMap<String, String>>> gattCharacteristicData
                = new ArrayList<ArrayList<HashMap<String, String>>>();
        mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();

        // Loops through available GATT Services.
        for (BluetoothGattService gattService : gattServices) {
            HashMap<String, String> currentServiceData = new HashMap<String, String>();
            uuid = gattService.getUuid().toString();
            currentServiceData.put(
                    LIST_NAME, SEGGattAttributes.lookup(uuid, unknownServiceString));
            currentServiceData.put(LIST_UUID, uuid);
            gattServiceData.add(currentServiceData);

            ArrayList<HashMap<String, String>> gattCharacteristicGroupData =
                    new ArrayList<HashMap<String, String>>();
            List<BluetoothGattCharacteristic> gattCharacteristics =
                    gattService.getCharacteristics();
            ArrayList<BluetoothGattCharacteristic> charas =
                    new ArrayList<BluetoothGattCharacteristic>();

            // Loops through available Characteristics.
            for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                charas.add(gattCharacteristic);
                HashMap<String, String> currentCharaData = new HashMap<String, String>();
                uuid = gattCharacteristic.getUuid().toString();
                currentCharaData.put(
                        LIST_NAME, SEGGattAttributes.lookup(uuid, unknownCharaString));
                currentCharaData.put(LIST_UUID, uuid);
                gattCharacteristicGroupData.add(currentCharaData);
            }
            mGattCharacteristics.add(charas);
            gattCharacteristicData.add(gattCharacteristicGroupData);
        }

        SimpleExpandableListAdapter gattServiceAdapter = new SimpleExpandableListAdapter(
                mCtx,
                gattServiceData,
                android.R.layout.simple_expandable_list_item_2,
                new String[] {LIST_NAME, LIST_UUID},
                new int[] { android.R.id.text1, android.R.id.text2 },
                gattCharacteristicData,
                android.R.layout.simple_expandable_list_item_2,
                new String[] {LIST_NAME, LIST_UUID},
                new int[] { android.R.id.text1, android.R.id.text2 }
        );
        mGattServicesList.setAdapter(gattServiceAdapter);
    }
	
    private ExpandableListView.OnChildClickListener mServicesListClickListner = null;
    public void setOnExpandableListViewChildClickListener(ExpandableListView.OnChildClickListener listener)
    {

    	mServicesListClickListner = listener;
    }
}
