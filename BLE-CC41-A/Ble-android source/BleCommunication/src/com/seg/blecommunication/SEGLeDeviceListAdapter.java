/**
 * @Copyright 2014 by SEG, Inc. All rights reserved.
**/
package com.seg.blecommunication;

import java.util.ArrayList;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

/**
 * <pre>
 * 
 * @author bume16
 * @date 2014. 6. 4.
 * @version 
 * </pre>
 **/
public class SEGLeDeviceListAdapter extends BaseAdapter{

    /**
     * �˻��� ������� ��ġ �迭
     */
    private ArrayList<BluetoothDevice> mLeDevices = null;
    
    /**
     * ����Ʈ ������ inflater 
     */
    private LayoutInflater mInflater  = null;;
    
    /**
     * ������
     */
    public SEGLeDeviceListAdapter(Context context){
    	mLeDevices = new ArrayList<BluetoothDevice>();
    	mInflater = ((Activity)context).getLayoutInflater();
    }
    
    /**
     * <PRE>
     * Comment : <br>
     * �̹� �˻��� ��ġ�� �ƴϸ� ����Ʈ�� �߰��Ѵ�.
     * @author bume16
     * @date 2014. 6. 4.
     * </PRE>
     * @param device
     */
    public void addDevice(BluetoothDevice device) {
        if(!mLeDevices.contains(device)) {
            mLeDevices.add(device);
        }
    }
    
    /**
     * <PRE>
     * Comment : <br>
     * ������� ��ġ�� ��ȯ 
     * @author bume16
     * @date 2014. 6. 4.
     * </PRE>
     * @param position
     * @return
     */
    public BluetoothDevice getDevice(int position) {
        return mLeDevices.get(position);
    }
    
    /**
     * <PRE>
     * Comment : <br>
     * �˻��� ��ġ�� Ŭ���� �Ѵ�. 
     * @author bume16
     * @date 2014. 6. 4.
     * </PRE>
     */
    public void clear() {
        mLeDevices.clear();
    }
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mLeDevices.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return mLeDevices.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View view, ViewGroup parent) {
		// TODO Auto-generated method stub
		ViewHolder viewHolder;
        // General ListView optimization code.
        if (view == null) {
            view = mInflater.inflate(R.layout.listitem_device, null);
            viewHolder = new ViewHolder();
            viewHolder.deviceAddress = (TextView) view.findViewById(R.id.device_address);
            viewHolder.deviceName = (TextView) view.findViewById(R.id.device_name);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }

        BluetoothDevice device = mLeDevices.get(position);
        final String deviceName = device.getName();
        if (deviceName != null && deviceName.length() > 0)
            viewHolder.deviceName.setText(deviceName);
        else
            viewHolder.deviceName.setText(R.string.unknown_device);
        viewHolder.deviceAddress.setText(device.getAddress());

        return view;
	}

	/**
	 * �� Ȧ�� Ŭ����
	 * @author bume16
	 *
	 */
	static class ViewHolder {
        TextView deviceName;
        TextView deviceAddress;
    }
}
