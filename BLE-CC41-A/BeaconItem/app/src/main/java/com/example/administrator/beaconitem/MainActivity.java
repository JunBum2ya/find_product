package com.example.administrator.beaconitem;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.content.Intent;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.perples.recosdk.RECOBeacon;
import com.perples.recosdk.RECOBeaconManager;
import com.perples.recosdk.RECOBeaconRegion;
import com.perples.recosdk.RECOErrorCode;
import com.perples.recosdk.RECOMonitoringListener;
import com.perples.recosdk.RECORangingListener;
import com.perples.recosdk.RECOServiceConnectListener;

import java.util.ArrayList;
import java.util.Collection;

//public class MainActivity extends AppCompatActivity{
public class MainActivity extends AppCompatActivity implements RECOServiceConnectListener, RECORangingListener {
    private static final int REQUEST_ENABLE_BT = 2;
    boolean mScanRecoOnly = true;
    boolean mEnableBackgroundTimeout = true;
    private BluetoothAdapter mBluetoothAdapter = null;
    private TextView tv;
    private RECOBeaconManager recoManager;
    private ArrayList<RECOBeaconRegion> rangingRegions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
            tv = (TextView) findViewById(R.id.textView);
            tv.setText("Beacon시작");

            //RECOBeaconManager의 인스턴스를 생성합니다
            //RECOBeaconManager.getInstance(Context, boolean, boolean)의 경우,
            //Context, RECO 비콘만을 대상으로 동작 여부를 설정하는 값, 그리고 백그라운드 monitoring 중 ranging 시 timeout을 설정하는 값을 매개변수로 받습니다.
            recoManager = RECOBeaconManager.getInstance(this, mScanRecoOnly, mEnableBackgroundTimeout);
            recoManager.bind(this);
            recoManager.setRangingListener(this);
        }
/*
        @Override
        public boolean onCreateOptionsMenu(Menu menu) {
            getMenuInflater().inflate(R.menu.main, menu);
            return true;
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == R.id.action_settings) {
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
*/

        //연결이 되었을때 제공되는 서비스 부분
        @Override
        public void onServiceConnect() {
            //RECOBeaconService와 연결 시 코드 작성
            tv.setText(tv.getText() + "\n연결 되었습니다");

            //리스트 생성해서 비콘 값 저장
            rangingRegions = new ArrayList<RECOBeaconRegion>();
            //비콘의UUID, Major, 이름
            rangingRegions.add(new RECOBeaconRegion("24DDF411-8CF1-440C-87CD-E368DAF9C93E", 2063, "연두색"));
            rangingRegions.add(new RECOBeaconRegion("24DDF411-8CF1-440C-87CD-E368DAF9C93E", 2064, "자주"));
            rangingRegions.add(new RECOBeaconRegion("24DDF411-8CF1-440C-87CD-E368DAF9C93E", 2065, "치즈"));

            for (RECOBeaconRegion region : rangingRegions) {
                try {
                    recoManager.startRangingBeaconsInRegion(region);
                    recoManager.requestStateForRegion(region);
                } catch (RemoteException e) {
                    // RemoteException 발생 시 작성 코드

                } catch (NullPointerException e) {
                    // NullPointerException 발생 시 작성 코드

                }
            }
        }

        @Override
        public void onServiceFail(RECOErrorCode arg0) {
            // RECOBeaconService와 연결 시 코드 작성
            tv.setText(tv.getText()+"\n연결 실패했습니다.");
        }


        //1초마다 비콘변화 감지
        @Override
        public void didRangeBeaconsInRegion(Collection<RECOBeacon> arg0, RECOBeaconRegion arg1) {
            // TODO Auto-generated method stub

            if (arg0.size() == 0) {
                tv.setText(tv.getText() + "\n" + arg1.getUniqueIdentifier() + " 탈주!");
            } else {
                tv.setText(tv.getText() + "\n" + arg1.getUniqueIdentifier() + " 있음!");
            }
//		if (arg1.getMajor() == 2062) {
//			if (arg0.size() == 0) {
//				tv.setText(tv.getText() + "\n켠성 탈주!");
//			} else {
//				tv.setText(tv.getText() + "\n켠성 범위안에있음!");
//			}
//
//		} else if (arg1.getMajor() == 2064) {
//			if (arg0.size() == 0) {
//				tv.setText(tv.getText() + "\n규도 탈주!");
//			} else {
//				tv.setText(tv.getText() + "\n규도 범위안에있음!");
//			}
//		} else if (arg1.getMajor() == 2065) {
//			if (arg0.size() == 0) {
//				tv.setText(tv.getText() + "\n대영 탈주!");
//			} else {
//				tv.setText(tv.getText() + "\n대영 범위안에있음!");
//			}
//		}
        }

        @Override
        public void rangingBeaconsDidFailForRegion(RECOBeaconRegion arg0, RECOErrorCode arg1) {
            // TODO Auto-generated method stub
            tv.setText(tv.getText() + "\n실패하였습니다.");
        }
}
