package com.example.gan.testtestrun;

import android.Manifest;
import android.animation.Animator;
import android.app.Activity;
import android.app.ProgressDialog;
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
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
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
    private static ListPreference wifiConnections;
    private static ProgressDialog waitDlg;
    private static boolean scanComplete;
    private WifiConnectionReceiver receiver;

    public MyPreferencesActivity() {
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        rootView = findViewById(android.R.id.content);
        getFragmentManager().beginTransaction().replace(android.R.id.content, new MyPreferencesFragment()).commit();

        waitDlg = new ProgressDialog(this);
        waitDlg.setTitle("Wait...");
        waitDlg.setMessage("Wait for wifi info");
        waitDlg.setCancelable(false);
        scanComplete = false;

        wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        if (!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
        }
//        receiver = new WifiConnectionReceiver(new android.os.Handler());
//        registerReceiver(receiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        scanWifi();
    }

    @Override
    protected void onResume() {
        super.onResume();
        receiver = new WifiConnectionReceiver(new android.os.Handler());
        registerReceiver(receiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
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

    //public static class MyPreferencesFragment extends PreferenceFragment implements OnTaskCompleted {
    public static class MyPreferencesFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);
            wifiConnections = (ListPreference)findPreference("wifiConnections");
            if(wifiConnections != null)
            {
                wifiConnections.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        scanComplete = false;
                        waitDlg.show();

                        new Thread(){
                            public void run(){
                                Activity parentActivity = getActivity();
                                for(int i=0; i<=16; i++) {
                                    if(scanComplete)
                                    {
                                        waitDlg.dismiss();
                                        if(parentActivity != null) {
                                            parentActivity.runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    wifiConnections.setSummary("Select your home wifi");
                                                }
                                            });
                                        }
                                        return;
                                    }
                                    try {
                                        Thread.sleep(500);
                                        i++;
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                }
                                waitDlg.dismiss();
                                if(parentActivity != null) {
                                    parentActivity.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            wifiConnections.setSummary("No wifi network found");
                                        }
                                    });
                                }

                            }
                        }.start();
                        return false;
                    }
                });
            }
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
                Snackbar.make(rootView, "Fine location permission denied", Snackbar.LENGTH_SHORT).show();

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

        @SuppressWarnings("deprecation")
        @Override
        public void onReceive(final Context context, Intent intent) {
            final StringBuilder sb = new StringBuilder();

                //do something, permission was previously granted; or legacy device
                wifiList = wifiManager.getScanResults();
                final ListPreference wifiConnections2 = (ListPreference) findPreference("wifiConnections");

                handler.post(new Runnable() {
                    @Override
                    public void run() {

                        final List<CharSequence> entries = new ArrayList<CharSequence>();
                        final List<CharSequence> entryValues = new ArrayList<CharSequence>();
                        for (int i = 0; i < wifiList.size(); i++) {
                            //Toast.makeText(context, wifiList.get(i).toString(), Toast.LENGTH_SHORT).show();
                            String ssid = wifiList.get(i).SSID;
                            entries.add(ssid);
                            entryValues.add(ssid);
                        }

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                wifiConnections.setEntries((CharSequence[])entries.toArray(new String[0]));
                                wifiConnections.setEntryValues((CharSequence[])entryValues.toArray(new String[0]));
                                scanComplete = true;
                            }
                        });
//                        wifiConnections.setEntries((CharSequence[])entries.toArray(new String[0]));
//                        wifiConnections.setEntryValues((CharSequence[])entryValues.toArray(new String[0]));
//                        scanComplete = true;

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
