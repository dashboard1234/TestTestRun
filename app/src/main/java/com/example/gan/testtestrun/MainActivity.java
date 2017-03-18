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
        IntentFilter intentFilter = new IntentFilter(WifiService.ACTION_WIFI);
        // use LocalBroadcastManager is performance is much better than global register
        //registerReceiver(receiver, intentFilter);
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, intentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        //unregisterReceiver(receiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        speech.shutdown();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnStart:
                Intent intent = new Intent(getBaseContext(), WifiService.class);
                intent.setAction(WifiService.ACTION_WIFI);
                startService(intent);

                btnStart.setEnabled(false);
                btnStop.setEnabled(true);
                break;
            case R.id.btnStop:
                stopService(new Intent(getBaseContext(), WifiService.class));
                btnStart.setEnabled(true);
                btnStop.setEnabled(false);
                break;
            default:
                break;
        }
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String displayText = intent.getStringExtra(WifiService.PARAM_WIFI);
            tvSignal.setText(displayText);
        }
    };
}
