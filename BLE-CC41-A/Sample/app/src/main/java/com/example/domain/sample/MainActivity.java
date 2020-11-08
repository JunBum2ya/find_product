package com.example.domain.sample;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.util.HashMap;
import java.util.Map;


public class MainActivity extends ActionBarActivity implements LocationListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    private Map<String, Double> rows = new HashMap<String, Double>();

    private LocationManager manager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startDeviceTask();
            }
        });

        manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        // 정확도
        criteria.setAccuracy(Criteria.NO_REQUIREMENT);
        // 전원 소비량
        criteria.setPowerRequirement(Criteria.NO_REQUIREMENT);
        // 고도, 높이 값을 얻어 올지를 결정
        criteria.setAltitudeRequired(false);
        // provider 기본 정보(방위, 방향)
        criteria.setBearingRequired(false);
        // 속도
        criteria.setSpeedRequired(false);
        // 위치 정보를 얻어 오는데 들어가는 금전적 비용
        criteria.setCostAllowed(true);

        String provider = manager.getBestProvider(criteria, true);

        if (manager.isProviderEnabled(manager.NETWORK_PROVIDER) == true) {
            provider = LocationManager.NETWORK_PROVIDER;
        } else {
            provider = LocationManager.GPS_PROVIDER;
        }

        Log.d(TAG, "provider : " + provider);

        Location location = manager.getLastKnownLocation(provider);

        if (location != null) {
            rows.put("latitude", location.getLatitude());
            rows.put("longitude", location.getLongitude());
        }

        manager.requestLocationUpdates(provider, 0, 0, this);
    }

    private void startDeviceTask() {
        // 이벤트 수행 시 실행 될 메소드
        DeviceTask deviceTask = new DeviceTask(this, rows);
        deviceTask.execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onLocationChanged(Location location) {
        rows.put("latitude", location.getLatitude());
        rows.put("longitude", location.getLongitude());
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }
}
