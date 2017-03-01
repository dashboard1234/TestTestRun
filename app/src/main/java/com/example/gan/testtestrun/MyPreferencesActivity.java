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
import android.widget.Toast;

import java.util.List;
import java.util.logging.Handler;

/**
 * Created by gan on 2/28/17.
 */

public class MyPreferencesActivity extends PreferenceActivity {
    private static final int PERMISSIONS_REQUEST_CODE_ACCESS_COARSE_LOCATION = 100;
    private WifiManager wifiManager;
    private List<ScanResult> wifiList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction().replace(android.R.id.content, new MyPreferencesFragment()).commit();
        wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        if (!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
        }
        WifiConnectionReceiver receiver = new WifiConnectionReceiver(new android.os.Handler());
        registerReceiver(receiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        wifiManager.startScan();
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
        if (requestCode == PERMISSIONS_REQUEST_CODE_ACCESS_COARSE_LOCATION
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // Do something with granted permission
            wifiList = wifiManager.getScanResults();
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

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        PERMISSIONS_REQUEST_CODE_ACCESS_COARSE_LOCATION);
                //After this point you wait for callback in onRequestPermissionsResult(int, String[], int[]) overriden method

            }else{
                wifiList = wifiManager.getScanResults();
                //do something, permission was previously granted; or legacy device
            }




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
}
