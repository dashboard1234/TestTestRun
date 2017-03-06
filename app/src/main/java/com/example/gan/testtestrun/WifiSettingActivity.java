package com.example.gan.testtestrun;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.preference.ListPreference;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.LayoutInflaterCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CheckedTextView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

public class WifiSettingActivity extends AppCompatActivity {
    private static final int PERMISSIONS_REQUEST_CODE_ACCESS_COARSE_LOCATION = 100;
    private static final int PERMISSIONS_REQUEST_CODE_ACCESS_FINE_LOCATION = 200;
    public static final String WIFI_PREFERENCE = "wifiPref";
    public static final String SELECTED_WIFI = "selectedwifi";
    private WifiManager wifiManager;
    private List<ScanResult> wifiList;
    private View rootView;
    private static ProgressDialog waitDlg;
    private static boolean scanComplete;
    private WifiSettingActivity.WifiConnectionReceiver receiver;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi_setting);

        rootView = findViewById(android.R.id.content);
        sharedPreferences = getSharedPreferences(WIFI_PREFERENCE, Context.MODE_PRIVATE);

        waitDlg = new ProgressDialog(this);
        waitDlg.setTitle("Wait...");
        waitDlg.setMessage("Wait for wifi info");
        waitDlg.setCancelable(false);
        waitDlg.show();
        scanComplete = false;

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(7000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(!scanComplete)
                        {
                            TextView txtSig = (TextView)findViewById(R.id.txtNoSignal);
                            txtSig.setText(R.string.no_wifi_signal);
                            waitDlg.dismiss();
                            //unregisterReceiver(receiver);
                        }
                    }
                });
            }
        }).start();

        wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        if (!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
        }

        Button btnDone = (Button)findViewById(R.id.btnDone);
        btnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(WifiSettingActivity.this, MainActivity.class));
            }
        });

        scanWifi();
    }

    @Override
    protected void onResume() {
        super.onResume();
        receiver = new WifiSettingActivity.WifiConnectionReceiver(new android.os.Handler());
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

            handler.post(new Runnable() {
                @Override
                public void run() {
                    final List<String> wifiInfo = new ArrayList<String>();
                    for (int i = 0; i < wifiList.size(); i++) {
                        String ssid = wifiList.get(i).SSID;
                        ssid.replace("\"", "");
                        if(!TextUtils.isEmpty(ssid) && !wifiInfo.contains(ssid))
                            wifiInfo.add(ssid);
                    }

                    WifiListViewAdapter adapter = new WifiListViewAdapter(getBaseContext(), R.layout.wifi_info, wifiInfo);
                    ListView listView = (ListView)rootView.findViewById(R.id.lvWifi);
                    listView.setAdapter(adapter);
                    scanComplete = true;
                    waitDlg.dismiss();
                    //unregisterReceiver(receiver);
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
                            ActivityCompat.requestPermissions(WifiSettingActivity.this,
                                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_CODE_ACCESS_FINE_LOCATION);
                        }
                    }).show();
        }
        else
        {
            ActivityCompat.requestPermissions(WifiSettingActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_CODE_ACCESS_FINE_LOCATION);
        }
    }

    private class WifiListViewAdapter extends ArrayAdapter<String> {
        List<String> wifiList;
        int selectedPosition;
        String selectedWifi;
        final ViewHolder viewHolder;

        public WifiListViewAdapter(Context context, int resource, List<String> wifis) {
            super(context, resource, wifis);
            wifiList = new ArrayList<String>();
            wifiList.addAll(wifis);
            selectedWifi = sharedPreferences.getString(SELECTED_WIFI, "");
            viewHolder = new ViewHolder();
        }

        private class ViewHolder{
            RadioButton radioButton;
            //TextView textView;
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if(convertView == null)
            {
                LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.wifi_info, null);
            }
            viewHolder.radioButton = (RadioButton)convertView.findViewById(R.id.rbWifi);

            selectedWifi = sharedPreferences.getString(SELECTED_WIFI, "");

            String ssidName = wifiList.get(position);
            viewHolder.radioButton.setText(ssidName);
            viewHolder.radioButton.setTag(position);

            if (selectedWifi.equals(viewHolder.radioButton.getText()))
                viewHolder.radioButton.setChecked(true);
            else
                viewHolder.radioButton.setChecked(false);

            viewHolder.radioButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    selectedPosition = (int)viewHolder.radioButton.getTag();
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString(SELECTED_WIFI, ((RadioButton)v).getText().toString());
                    editor.commit();

                    notifyDataSetChanged();
                }
            });
            return convertView;
        }
    }
}
