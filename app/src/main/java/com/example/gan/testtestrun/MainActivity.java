package com.example.gan.testtestrun;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

import static com.example.gan.testtestrun.R.id.btnStart;


public class MainActivity extends BaseActivity implements View.OnClickListener {
    private boolean isStop = true;
    private static final String TAG = MainActivity.class.getName();
    private TextView tvSignal;
    private TextView tvTimer;
    WifiManager wifiManager;
    WifiReceiver receiver;
    WifiReceiver timerReceiver;
//    WifiReceiver wifiStateReceiver;
//    WifiReceiver wifiSignalReceiver;
    TextToSpeech speech;
    boolean reminderTriggered = false;
    String selectedWifi = "";
    private Button btnStart;
    private Button btnStop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tvTimer = (TextView)findViewById(R.id.tvTimer);
        tvSignal = (TextView)findViewById(R.id.tvSignal);
        btnStart = (Button)findViewById(R.id.btnStart);
        btnStop = (Button)findViewById(R.id.btnStop);
        receiver = new WifiReceiver();
        timerReceiver = new WifiReceiver();

        wifiManager = (WifiManager)getSystemService(WIFI_SERVICE);
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

        btnStart.setOnClickListener(this);
        btnStop.setOnClickListener(this);
        //if(HelperUtility.isServiceRunning(this, WifiMonitorService.class)){
        if(HelperUtility.isServiceRunning(this, WifiService.class)){
            btnStart.setEnabled(false);
            btnStop.setEnabled(true);
        }
        else{
            btnStart.setEnabled(true);
            btnStop.setEnabled(false);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //IntentFilter intentFilter = new IntentFilter(WifiMonitorService.ACTION_TIMER2);
        IntentFilter intentFilter = new IntentFilter(WifiService.ACTION_TIMER);
        LocalBroadcastManager.getInstance(this).registerReceiver(timerReceiver, intentFilter);
        //receiver.registerState();
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(timerReceiver);
        //receiver.unregisterState();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnStart:
                //Intent intent = new Intent(getBaseContext(), WifiMonitorService.class);
                Intent intent = new Intent(getBaseContext(), WifiService.class);
                //intent.setAction(HelperUtility.INTENT_ACTION_MONITOR_WIFI);
                intent.setAction(WifiMonitorService.ACTION_TIMER);
                intent.putExtra(WifiMonitorService.PARAM_TIMER, 120);
                //intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);


                startService(intent);

                btnStart.setEnabled(false);
                btnStop.setEnabled(true);
                break;
            case R.id.btnStop:
                //stopService(new Intent(getBaseContext(), WifiMonitorService.class));
                stopService(new Intent(getBaseContext(), WifiService.class));
                btnStart.setEnabled(true);
                btnStop.setEnabled(false);
                break;
            default:
                break;
        }
    }

    private class WifiReceiver extends BroadcastReceiver {
    //private BroadcastReceiver receiver = new BroadcastReceiver(){
        boolean mIsSigReceiverRegistered;
        boolean mIsStateReceiverRegistered;

        //public void registerState()
        public  Intent registerState()
        {
            if(mIsStateReceiverRegistered)
                return null;
                IntentFilter intentFilter = new IntentFilter(WifiManager.NETWORK_STATE_CHANGED_ACTION);
                mIsStateReceiverRegistered = true;
                return registerReceiver(this, intentFilter);
                //LocalBroadcastManager.getInstance(getBaseContext()).registerReceiver(this, intentFilter);

        }
        //public void registerSignal()
        public Intent registerSignal()
        {
            if(mIsSigReceiverRegistered)
                return null;
                IntentFilter intentFilter = new IntentFilter(WifiManager.RSSI_CHANGED_ACTION);
                mIsSigReceiverRegistered = true;
                return registerReceiver(this, intentFilter);
                //LocalBroadcastManager.getInstance(getBaseContext()).registerReceiver(this, intentFilter);
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
                case WifiService.ACTION_TIMER:
                    //Integer timerCount = intent.getIntExtra(WifiMonitorService.PARAM_TIMER, 0);
                    Integer timerCount = intent.getIntExtra(WifiService.PARAM_TIMER, 0);
                    tvTimer.setText(timerCount + " seconds");
                    break;
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
                            tvSignal.setText(R.string.stop_listen_signal);
                            break;
                        }
                        else if(!TextUtils.isEmpty(selectedWifi) && selectedWifi.equals(connectedWifiName)) {
                            registerSignal();
                            break;
                        }
                    }
                    unregisterSignal();
                    tvSignal.setText(R.string.stop_listen_signal);
                    break;
                default:
                    Log.d(TAG, "Invalid action type");
                    break;
            }
        }
    };

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
