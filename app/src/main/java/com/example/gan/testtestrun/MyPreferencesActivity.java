package com.example.gan.testtestrun;

import android.Manifest;
import android.animation.Animator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import java.util.List;
import java.util.logging.Handler;

/**
 * Created by gan on 2/28/17.
 */

public class MyPreferencesActivity extends PreferenceActivity implements ActivityCompat.OnRequestPermissionsResultCallback {
    private static final int PERMISSIONS_REQUEST_CODE_ACCESS_COARSE_LOCATION = 100;
    private static final int PERMISSIONS_REQUEST_CODE_ACCESS_FINE_LOCATION = 200;
    private WifiManager wifiManager;
    private List<ScanResult> wifiList;
    private View rootView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        rootView = findViewById(android.R.id.content);
        getFragmentManager().beginTransaction().replace(android.R.id.content, new MyPreferencesFragment()).commit();
        wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        if (!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
        }
        WifiConnectionReceiver receiver = new WifiConnectionReceiver(new android.os.Handler());
        registerReceiver(receiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        scanWifi();
    }

    private void scanWifi() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_CODE_ACCESS_FINE_LOCATION);
            //After this point you wait for callback in onRequestPermissionsResult(int, String[], int[]) overriden method
            requestLocationPermission();
        }
        else {
            wifiManager.startScan();
        }
    }

    public static class MyPreferencesFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_CODE_ACCESS_FINE_LOCATION)
        {
            if(grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                Snackbar.make(rootView, "Fine location permission granted", Snackbar.LENGTH_SHORT).show();
                wifiManager.startScan();
            }
            else
                Snackbar.make(rootView, "Fine location permission granted", Snackbar.LENGTH_SHORT).show();

        }
        else
        {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private class WifiConnectionReceiver extends BroadcastReceiver {
        private final android.os.Handler handler;

        public WifiConnectionReceiver(android.os.Handler handlerIn) {
            this.handler = handlerIn;
        }

        @Override
        public void onReceive(final Context context, Intent intent) {
            final StringBuilder sb = new StringBuilder();

                //do something, permission was previously granted; or legacy device
                wifiList = wifiManager.getScanResults();
                ListPreference wifiConnections = (ListPreference) findPreference("wifiConnections");

//            for(int i = 0; i < wifiList.size(); i++){
//
//                sb.append(new Integer(i+1).toString() + ". ");
//                sb.append((wifiList.get(i)).toString());
//                sb.append("\n\n");
//            }
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        for (int i = 0; i < wifiList.size(); i++) {
                            Toast.makeText(context, wifiList.get(i).toString(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }

    }

    private void requestLocationPermission() {
        if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION))
        {
            Snackbar.make(rootView, R.string.permission_fine_location_rationale, Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.ok, new View.OnClickListener(){

                        @Override
                        public void onClick(View v) {
                            ActivityCompat.requestPermissions(MyPreferencesActivity.this,
                                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_CODE_ACCESS_FINE_LOCATION);
                        }
                    }).show();
        }
        else
        {
            ActivityCompat.requestPermissions(MyPreferencesActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_CODE_ACCESS_FINE_LOCATION);
        }
    }
}
