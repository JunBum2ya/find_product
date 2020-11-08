/**
 * @Copyright 2014 by SEG, Inc. All rights reserved.
**/
package com.seg.bleterminal;

import com.seg.blecommunication.SEGLeDeviceListAdapter;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

/**
 * <pre>
 * 블루투스 장치를 검색하고 정보를 보여줄 프래그 먼트
 * DevInfoSectionFragment.java
 * @author bume16
 * @date 2014. 6. 4.
 * @version 
 * </pre>
 **/
public class AppInfoSectionFragment extends Fragment{

	private Context mCtx = null;
	/**
	 * 찾은 블루투스 장치를 보여줄 리스트
	 */
	private ListView mFindDevList = null;
	/**
	 * 블루투스 장치 어답터
	 */
	private SEGLeDeviceListAdapter mDevListAdapter = null;
	
    // newInstance constructor for creating fragment with arguments
    public static AppInfoSectionFragment newInstance() {
    	
    	AppInfoSectionFragment fragment = new AppInfoSectionFragment();
    	
    	// Supply index input as an argument.
        Bundle args = new Bundle();
        
        fragment.setArguments(args);
        
        return fragment;
    }

    public AppInfoSectionFragment()
    {
    	
    }
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		View rootView = inflater.inflate(R.layout.device_information_fragment, container, false);
		
		//initial fragment layout
		mFindDevList = (ListView)rootView.findViewById(R.id.listFindedLeDevices);
		mDevListAdapter = new SEGLeDeviceListAdapter(mCtx);
		mFindDevList.setAdapter(mDevListAdapter);
		
		return rootView;//super.onCreateView(inflater, container, savedInstanceState);
	}
	/**
	 * <PRE>
	 * Comment : <br>
	 * 리스트에 어댑터를 연결한다.
	 * @author bume16
	 * @date 2014. 6. 4.
	 * </PRE>
	 * @param adapter
	 */
	public void setListAdapter(SEGLeDeviceListAdapter adapter)
	{
		//initial fragment layout
		if(mFindDevList != null)
			mFindDevList.setAdapter(adapter);
	}
	
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
	
	
	
}
