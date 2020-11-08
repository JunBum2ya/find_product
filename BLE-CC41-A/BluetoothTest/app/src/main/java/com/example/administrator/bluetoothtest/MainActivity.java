package com.example.administrator.bluetoothtest;
 
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends Activity {
	// Debugging
	private static final String TAG = "Main";
	
	// Intent request code
	private static final int REQUEST_CONNECT_DEVICE = 1;
	private static final int REQUEST_ENABLE_BT = 2;
	
	// Layout
	private Button btn_Connect;
	private TextView txt_Result;
	private Button btn_Send;
	private EditText txt_Send;
	private String send;
	private BluetoothService btService = null;
	
	
	private final Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
		}
		
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.e(TAG, "onCreate");
		
		setContentView(R.layout.activity_main);
		
		/** Main Layout **/
		btn_Connect = (Button) findViewById(R.id.button);
		txt_Result = (TextView) findViewById(R.id.textView);
		btn_Send=(Button)findViewById(R.id.button2);
		txt_Send=(EditText)findViewById(R.id.editText);
		btn_Connect.setOnClickListener(new View.OnClickListener(){
			public void onClick(View v){
				if(btService.getDeviceState()) {
					// ��������� ���� ������ ����� ��
					btService.enableBluetooth();
				} else {
					finish();
				}
			}
		});
		btn_Send.setOnClickListener(new View.OnClickListener(){
			public void onClick(View v){
				send=txt_Send.getText().toString();
			}
		});
		
		// BluetoothService Ŭ���� ����
		if(btService == null) {
			btService = new BluetoothService(this, mHandler);
		}
	}
	
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult " + resultCode);
        
        switch (requestCode) {
        
        /** �߰��� �κ� ���� **/
        case REQUEST_CONNECT_DEVICE:
            // When DeviceListActivity returns with a device to connect
            if (resultCode == Activity.RESULT_OK) {
            	btService.getDeviceInfo(data);
            }
            break;
        /** �߰��� �κ� �� **/
        case REQUEST_ENABLE_BT:
            // When the request to enable Bluetooth returns
            if (resultCode == Activity.RESULT_OK) {
            	// Next Step
            	btService.scanDevice();
            } else {

                Log.d(TAG, "Bluetooth is not enabled");
            }
            break;
        }
	}

}
