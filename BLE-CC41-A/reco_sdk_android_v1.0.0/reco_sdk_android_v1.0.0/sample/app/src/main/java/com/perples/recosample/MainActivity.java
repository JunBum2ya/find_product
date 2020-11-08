/**
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2015 Perples, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.perples.recosample;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.perples.recosdk.RECOBeacon;
import com.perples.recosdk.RECOBeaconManager;
import com.perples.recosdk.RECOBeaconRegion;
import com.perples.recosdk.RECOBeaconRegionState;
import com.perples.recosdk.RECOErrorCode;
import com.perples.recosdk.RECOMonitoringListener;
import com.perples.recosdk.RECORangingListener;
import com.perples.recosdk.RECOServiceConnectListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends Activity implements RECOServiceConnectListener,RECORangingListener{
    //This is a default proximity uuid of the RECO
    public static final String RECO_UUID = "24DDF411-8CF1-440C-87CD-E368DAF9C93E";

    /**
     * SCAN_RECO_ONLY:
     *
     * If true, the application scans RECO beacons only, otherwise it scans all beacons.
     * It will be used when the instance of RECOBeaconManager is created.
     *
     * true일 경우 레코 비콘만 스캔하며, false일 경우 모든 비콘을 스캔합니다.
     * RECOBeaconManager 객체 생성 시 사용합니다.
     */
    public static final boolean SCAN_RECO_ONLY = true;

    /**
     * ENABLE_BACKGROUND_RANGING_TIMEOUT:
     *
     * If true, the application stops to range beacons in the entered region automatically in 10 seconds (background),
     * otherwise it continues to range beacons. (It affects the battery consumption.)
     * It will be used when the instance of RECOBeaconManager is created.
     *
     * 백그라운드 ranging timeout을 설정합니다.
     * true일 경우, 백그라운드에서 입장한 region에서 ranging이 실행 되었을 때, 10초 후 자동으로 정지합니다.
     * false일 경우, 계속 ranging을 실행합니다. (배터리 소모율에 영향을 끼칩니다.)
     * RECOBeaconManager 객체 생성 시 사용합니다.
     */
    public static final boolean ENABLE_BACKGROUND_RANGING_TIMEOUT = true;

    /**
     * DISCONTINUOUS_SCAN:
     *
     * There is a known android bug that some android devices scan BLE devices only once.
     * (link: http://code.google.com/p/android/issues/detail?id=65863)
     * To resolve the bug in our SDK, you can use setDiscontinuousScan() method of the RECOBeaconManager.
     * This method is to set whether the device scans BLE devices continuously or discontinuously.
     * The default is set as FALSE. Please set TRUE only for specific devices.
     *
     * 일부 안드로이드 기기에서 BLE 장치들을 스캔할 때, 한 번만 스캔 후 스캔하지 않는 버그(참고: http://code.google.com/p/android/issues/detail?id=65863)가 있습니다.
     * 해당 버그를 SDK에서 해결하기 위해, RECOBeaconManager에 setDiscontinuousScan() 메소드를 이용할 수 있습니다.
     * 해당 메소드는 기기에서 BLE 장치들을 스캔할 때(즉, ranging 시에), 연속적으로 계속 스캔할 것인지, 불연속적으로 스캔할 것인지 설정하는 것입니다.
     * 기본 값은 FALSE로 설정되어 있으며, 특정 장치에 대해 TRUE로 설정하시길 권장합니다.
     */
    public static final boolean DISCONTINUOUS_SCAN = false;

    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_LOCATION = 10;

    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;

    private View mLayout;
    private EditText edit1;
    private EditText edit2;
    private EditText edit3;
    private EditText date1;
    private EditText date2;
    private EditText date3;
    private CheckBox check1;
    private CheckBox check2;
    private CheckBox check3;
    //private TextView tv;
    //private Button bt;
    private RECOBeaconManager recoManager;
    private ArrayList<RECOBeaconRegion> rangingRegions;
    private ArrayList<RECOBeacon> RangedBeacons;

    private int k=0;
    private int j=0;

    private boolean distanceok1=true;
    private boolean distanceok2=true;
    private boolean distanceok3=true;

    private Map<String, Double> rows = new HashMap<String, Double>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mLayout = findViewById(R.id.mainLayout);
        edit1=(EditText)findViewById(R.id.ItemeditText1);
        edit2=(EditText)findViewById(R.id.ItemeditText2);
        edit3=(EditText)findViewById(R.id.ItemeditText3);
        date1=(EditText)findViewById(R.id.editText4);
        date2=(EditText)findViewById(R.id.editText6);
        date3=(EditText)findViewById(R.id.editText8);
        check1=(CheckBox)findViewById(R.id.radioButton1);
        check2=(CheckBox)findViewById(R.id.radioButton2);
        check3=(CheckBox)findViewById(R.id.radioButton3);

        //If a user device turns off bluetooth, request to turn it on.
        //사용자가 블루투스를 켜도록 요청합니다.
        mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();

        if(mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent enableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBTIntent, REQUEST_ENABLE_BT);
        }

        /**
         * In order to use RECO SDK for Android API 23 (Marshmallow) or higher,
         * the location permission (ACCESS_COARSE_LOCATION or ACCESS_FINE_LOCATION) is required.
         * Please refer to the following permission guide and sample code provided by Google.
         *
         * 안드로이드 API 23 (마시멜로우)이상 버전부터, 정상적으로 RECO SDK를 사용하기 위해서는
         * 위치 권한 (ACCESS_COARSE_LOCATION 혹은 ACCESS_FINE_LOCATION)을 요청해야 합니다.
         * 권한 요청의 경우, 구글에서 제공하는 가이드를 참고하시기 바랍니다.
         *
         * http://www.google.com/design/spec/patterns/permissions.html
         * https://github.com/googlesamples/android-RuntimePermissions
         */
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Log.i("MainActivity", "The location permission (ACCESS_COARSE_LOCATION or ACCESS_FINE_LOCATION) is not granted.");
                this.requestLocationPermission();
            } else {
                Log.i("MainActivity", "The location permission (ACCESS_COARSE_LOCATION or ACCESS_FINE_LOCATION) is already granted.");
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
            //If the request to turn on bluetooth is denied, the app will be finished.
            //사용자가 블루투스 요청을 허용하지 않았을 경우, 어플리케이션은 종료됩니다.
            finish();
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch(requestCode) {
            case REQUEST_LOCATION : {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Snackbar.make(mLayout, R.string.location_permission_granted, Snackbar.LENGTH_LONG).show();
                } else {
                    Snackbar.make(mLayout, R.string.location_permission_not_granted, Snackbar.LENGTH_LONG).show();
                }
            }
            default :
                break;
        }


    }
/*
    @Override
    protected void onResume() {
        super.onResume();

        if(this.isBackgroundMonitoringServiceRunning(this)) {
            ToggleButton toggle = (ToggleButton)findViewById(R.id.clearbutton);
            toggle.setChecked(true);
        }

        if(this.isBackgroundRangingServiceRunning(this)) {
            ToggleButton toggle = (ToggleButton)findViewById(R.id.backgroundRangingToggleButton);
            toggle.setChecked(true);
        }
    }
*/
    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        this.stop(rangingRegions);
        this.unbind();
    }

    /**
     * In order to use RECO SDK for Android API 23 (Marshmallow) or higher,
     * the location permission (ACCESS_COARSE_LOCATION or ACCESS_FINE_LOCATION) is required.
     *
     * This sample project requests "ACCESS_COARSE_LOCATION" permission only,
     * but you may request "ACCESS_FINE_LOCATION" permission depending on your application.
     *
     * "ACCESS_COARSE_LOCATION" permission is recommended.
     *
     * 안드로이드 API 23 (마시멜로우)이상 버전부터, 정상적으로 RECO SDK를 사용하기 위해서는
     * 위치 권한 (ACCESS_COARSE_LOCATION 혹은 ACCESS_FINE_LOCATION)을 요청해야 합니다.
     *
     * 본 샘플 프로젝트에서는 "ACCESS_COARSE_LOCATION"을 요청하지만, 필요에 따라 "ACCESS_FINE_LOCATION"을 요청할 수 있습니다.
     *
     * 당사에서는 ACCESS_COARSE_LOCATION 권한을 권장합니다.
     *
     */
    private void requestLocationPermission() {
        if(!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION)) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_LOCATION);
            return;
        }

        Snackbar.make(mLayout, R.string.location_permission_rationale, Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.ok, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_LOCATION);
                    }
                })
                .show();
    }

    public void onMonitoringToggleButtonClicked(View v) {
        ToggleButton toggle = (ToggleButton)v;
        if(toggle.isChecked()) {
            Log.i("MainActivity", "onMonitoringToggleButtonClicked off to on");
            Intent intent = new Intent(this, RecoBackgroundMonitoringService.class);
            startService(intent);
        } else {
            Log.i("MainActivity", "onMonitoringToggleButtonClicked on to off");
            stopService(new Intent(this, RecoBackgroundMonitoringService.class));
        }
    }

    public void onRangingToggleButtonClicked(View v) {
        ToggleButton toggle = (ToggleButton)v;
        if(toggle.isChecked()) {
            Log.i("MainActivity", "onRangingToggleButtonClicked off to on");
            Intent intent = new Intent(this, RecoBackgroundRangingService.class);
            startService(intent);
        } else {
            Log.i("MainActivity", "onRangingToggleButtonClicked on to off");
            stopService(new Intent(this, RecoBackgroundRangingService.class));
        }
    }

    public void onButtonClicked(View v) {
        Button btn = (Button)v;
        if(btn.getId() == R.id.clearbutton) {
            final Intent intent = new Intent(this, RecoMonitoringActivity.class);
            startActivity(intent);
        } else {
            final Intent intent = new Intent(this, RecoRangingActivity.class);
            startActivity(intent);
        }
    }

    private boolean isBackgroundMonitoringServiceRunning(Context context) {
        ActivityManager am = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
        for(ActivityManager.RunningServiceInfo runningService : am.getRunningServices(Integer.MAX_VALUE)) {
            if(RecoBackgroundMonitoringService.class.getName().equals(runningService.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    private boolean isBackgroundRangingServiceRunning(Context context) {
        ActivityManager am = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
        for(ActivityManager.RunningServiceInfo runningService : am.getRunningServices(Integer.MAX_VALUE)) {
            if(RecoBackgroundRangingService.class.getName().equals(runningService.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
    @Override
    public void onStart()
    {
        super.onStart();
        recoManager = RECOBeaconManager.getInstance(this, true, true);
        recoManager.bind(this);
        recoManager.setRangingListener(this);
    }
    @Override
    public void onServiceConnect() {
        //리스트 생성해서 비콘 값 저장
        rangingRegions = new ArrayList<RECOBeaconRegion>();
        //비콘의UUID, Major, 이름
        rangingRegions.add(new RECOBeaconRegion("24DDF411-8CF1-440C-87CD-E368DAF9C93E", 501,14850, "비콘1"));
        rangingRegions.add(new RECOBeaconRegion("24DDF411-8CF1-440C-87CD-E368DAF9C93E", 501,14851, "비콘2"));
        rangingRegions.add(new RECOBeaconRegion("24DDF411-8CF1-440C-87CD-E368DAF9C93E", 501,14852, "비콘3"));
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
        // TODO Auto-generated method stub
    }


    //1초마다 비콘변화 감지
    @Override
    public void didRangeBeaconsInRegion(Collection<RECOBeacon> arg0, RECOBeaconRegion arg1) {
        // TODO Auto-generated method stub
        RangedBeacons = new ArrayList<RECOBeacon>(arg0);
		if (arg1.getMinor() == 14850) {
			if (arg0.size() == 0) {
                edit1.setText("비콘1 탈주!");
			} else {
                edit1.setText(String.format("%.2f", RangedBeacons.get(0).getAccuracy()) + "m");
                rows.put("notebook",RangedBeacons.get(0).getAccuracy());
                if (distanceok1 && RangedBeacons.get(0).getAccuracy() > 10) {
                    if (check1.isChecked()) {
                        Toast.makeText(this, arg1.getUniqueIdentifier() + "가 10m를 벗어났습니다.", Toast.LENGTH_SHORT).show();
                        date1.setText(getDate());
                    }
                    distanceok1=false;
                }
                else if(!distanceok1 && RangedBeacons.get(0).getAccuracy()>10)
                {
                    if(check1.isChecked())
                    {
                        if(k==0) {
                            Toast.makeText(this, arg1.getUniqueIdentifier() + "가 10m를 벗어났습니다.", Toast.LENGTH_SHORT).show();
                        }
                    }
                    distanceok1=false;
                }
                else if(!distanceok1 && RangedBeacons.get(0).getAccuracy()<10)
                {
                    distanceok1=true;
                }
                else
                {
                    distanceok1=true;
                }
            }
		} else if (arg1.getMinor() == 14851) {
			if (arg0.size() == 0) {
                edit2.setText("비콘2 탈주!");
			} else {
                edit2.setText(String.format("%.2f", RangedBeacons.get(0).getAccuracy()) + "m");
                rows.put("clock",RangedBeacons.get(0).getAccuracy());
                if (distanceok2 && RangedBeacons.get(0).getAccuracy() > 10) {
                    if (check2.isChecked()) {
                        Toast.makeText(this, arg1.getUniqueIdentifier() + "가 10m를 벗어났습니다.", Toast.LENGTH_SHORT).show();
                        date2.setText(getDate());
                    }
                    distanceok2=false;
                }
                else if(!distanceok2 && RangedBeacons.get(0).getAccuracy()>10)
                {
                    if(check2.isChecked())
                    {
                        if(k==0) {
                            Toast.makeText(this, arg1.getUniqueIdentifier() + "가 10m를 벗어났습니다.", Toast.LENGTH_SHORT).show();
                        }
                    }
                    distanceok2=false;
                }
                else if(!distanceok2 && RangedBeacons.get(0).getAccuracy()<10)
                {
                    distanceok2=true;
                }
                else
                {
                    distanceok2=true;
                }
			}
		} else if (arg1.getMinor() == 14852) {
			if (arg0.size() == 0) {
                edit3.setText("비콘3 탈주!");
			} else {
                edit3.setText(String.format("%.2f", RangedBeacons.get(0).getAccuracy()) + "m");
                rows.put("wallet", RangedBeacons.get(0).getAccuracy());
                if (distanceok3 && RangedBeacons.get(0).getAccuracy() > 10) {
                    if (check3.isChecked()) {
                        Toast.makeText(this, arg1.getUniqueIdentifier() + "가 10m를 벗어났습니다.", Toast.LENGTH_LONG).show();
                        date3.setText(getDate());
                    }
                    distanceok3=false;
                }
                else if(!distanceok3 && RangedBeacons.get(0).getAccuracy()>10)
                {
                    if(check3.isChecked())
                    {
                        if(k==0) {
                            Toast.makeText(this, arg1.getUniqueIdentifier() + "가 10m를 벗어났습니다.", Toast.LENGTH_SHORT).show();
                        }
                    }
                    distanceok3=false;
                }
                else if(!distanceok3 && RangedBeacons.get(0).getAccuracy()<10)
                {
                    distanceok3=true;
                }
                else
                {
                    distanceok3=true;
                }
            }
		}
        k++;
        if(k==5)
        {
            k=0;
            startDeviceTask();
        }
    }
    @Override
    public void rangingBeaconsDidFailForRegion(RECOBeaconRegion arg0, RECOErrorCode arg1) {
        // TODO Auto-generated method stub
    }
    private void unbind() {
        try {
            recoManager.unbind();
        } catch (RemoteException e) {
            Log.i("RECORangingActivity", "Remote Exception");
            e.printStackTrace();
        }
    }
    protected void stop(ArrayList<RECOBeaconRegion> regions) {
        for(RECOBeaconRegion region : regions) {
            try {
                recoManager.stopRangingBeaconsInRegion(region);
            } catch (RemoteException e) {
                Log.i("RECORangingActivity", "Remote Exception");
                e.printStackTrace();
            } catch (NullPointerException e) {
                Log.i("RECORangingActivity", "Null Pointer Exception");
                e.printStackTrace();
            }
        }
    }
    public static String getDate(){

        SimpleDateFormat dateFormat = new  SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault());

        Date date = new Date();

        String strDate = dateFormat.format(date);

        return strDate;

    }
    private void startDeviceTask() {
        // 이벤트 수행 시 실행 될 메소드
        DeviceTask deviceTask = new DeviceTask(this, rows);
        deviceTask.execute();
    }
}
