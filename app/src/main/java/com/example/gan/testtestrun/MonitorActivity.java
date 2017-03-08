package com.example.gan.testtestrun;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.speech.tts.TextToSpeech;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.TextView;

import java.util.Locale;

public class MonitorActivity extends BaseActivity {
    private final static String TAG = MonitorActivity.class.getName();
    private TextView tvSignal;
    WifiManager wifiManager;
    WifiReceiver receiver;
    WifiReceiver wifiStateReceiver;
    WifiReceiver wifiSignalReceiver;
    TextToSpeech speech;
    boolean reminderTriggered = false;
    String selectedWifi = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monitor);
        tvSignal = (TextView)findViewById(R.id.tvSignal);
        wifiManager = (WifiManager)getSystemService(WIFI_SERVICE);
        receiver = new WifiReceiver();
        wifiSignalReceiver = new WifiReceiver();
        wifiStateReceiver = new WifiReceiver();

        SharedPreferences sharedPreferences = getSharedPreferences(WifiSettingActivity.WIFI_PREFERENCE, Context.MODE_PRIVATE);
        if(sharedPreferences != null)
            selectedWifi = sharedPreferences.getString(WifiSettingActivity.SELECTED_WIFI, "");

        speech=new TextToSpeech(this, new TextToSpeech.OnInitListener() {

            @Override
            public void onInit(int status) {
                // TODO Auto-generated method stub
                if(status == TextToSpeech.SUCCESS){
                    int result=speech.setLanguage(Locale.CHINA);
                    if(result==TextToSpeech.LANG_MISSING_DATA ||
                            result==TextToSpeech.LANG_NOT_SUPPORTED){
                        Log.e("error", "This Language is not supported");
                    }
                    else{
                        //speech.speak("Master, please don't forget your keys", TextToSpeech.QUEUE_FLUSH, null);
                        //speech.speak("主人，别忘了带钥匙", TextToSpeech.QUEUE_FLUSH, null);
                    }
                }
                else
                    Log.e("error", "Initilization Failed!");
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
//        IntentFilter filter = new IntentFilter(WifiManager.NETWORK_STATE_CHANGED_ACTION);
//        filter.addAction(WifiManager.RSSI_CHANGED_ACTION);
//        registerReceiver(receiver, filter);
        wifiStateReceiver.registerState();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //unregisterReceiver(receiver);
        wifiStateReceiver.unregisterState();
    }

    private void displayAlert(){
        new AlertDialog.Builder(this)
                .setTitle("Please select home wifi")
                .setMessage("Go to settings to select your home wifi or connect to your home wifi")
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    private class WifiReceiver extends BroadcastReceiver{
        boolean mIsSigReceiverRegistered;
        boolean mIsStateReceiverRegistered;

        public Intent registerState()
        {
            if(mIsStateReceiverRegistered)
                return null;
            IntentFilter intentFilter = new IntentFilter(WifiManager.NETWORK_STATE_CHANGED_ACTION);
            mIsStateReceiverRegistered = true;
            return registerReceiver(this, intentFilter);
        }
        public Intent registerSignal()
        {
            if(mIsSigReceiverRegistered)
                return null;
            IntentFilter intentFilter = new IntentFilter(WifiManager.RSSI_CHANGED_ACTION);
            mIsSigReceiverRegistered = true;
            return registerReceiver(this, intentFilter);
        }

        public void unregisterState()
        {
            if(mIsStateReceiverRegistered)
                unregisterReceiver(this);
            mIsStateReceiverRegistered = false;
        }

        public void unregisterSignal()
        {
            if(mIsSigReceiverRegistered)
                unregisterReceiver(this);
            mIsSigReceiverRegistered = false;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction())
            {
                case WifiManager.RSSI_CHANGED_ACTION:
                    String name = getWifiNetworkName(context);
                    float percent = getSignalStrength(intent);
                    tvSignal.setText(name + ": " + percent);
                    // ToDo: rework logic
                    if(percent<0.4 && !reminderTriggered) {
                        speech.speak("主人，别忘了带钥匙", TextToSpeech.QUEUE_FLUSH, null);
                        reminderTriggered = true;
                    }
                    if(reminderTriggered && percent > 0.6)
                        reminderTriggered = false;
                    break;
                case WifiManager.NETWORK_STATE_CHANGED_ACTION:
                    boolean connected = getWifiConnected(intent);
                    tvSignal.setText(connected ? "Wifi connected" : "Wifi disconnected");
                    if(connected) {
                        String connectedWifiName = getWifiNetworkName(context);
                        if (!TextUtils.isEmpty(selectedWifi) && !selectedWifi.equals(connectedWifiName)) {
                            unregisterSignal();
                            tvSignal.setText("Stop listening wifi signal");
                            break;
                        }
                        else if(!TextUtils.isEmpty(selectedWifi) && selectedWifi.equals(connectedWifiName)) {
                            registerSignal();
                            break;
                        }
                    }
                    unregisterSignal();
                    tvSignal.setText("Stop listening wifi signal");
                    break;
                default:
                    Log.d(TAG, "Invalid action type");
                    break;
            }
        }
    }

    private float getSignalStrength(Intent intent){
        float level = intent.getIntExtra(WifiManager.EXTRA_NEW_RSSI, -1);
        level = WifiManager.calculateSignalLevel((int)level, 100);
        level /= 100.0;
        return level;
    }

    private boolean getWifiConnected(Intent intent){
        NetworkInfo info = (NetworkInfo)intent.getExtras().get(WifiManager.EXTRA_NETWORK_INFO);
        if(info == null)
            return false;
        return info.getState().equals(NetworkInfo.State.CONNECTED);
    }

    public String getWifiNetworkName(Context context){
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        String ssid = wifiInfo.getSSID();
        //for some reason SSID comes wrapped in double-quotes
        if( ssid == null ){
            ssid = "";
        }
        return ssid.replace("\"", "");
    }
}
