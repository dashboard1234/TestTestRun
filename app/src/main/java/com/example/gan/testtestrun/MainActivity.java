package com.example.gan.testtestrun;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
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
        if(HelperUtility.isServiceRunning(this, WifiJobService.class)){
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
        IntentFilter intentFilter = new IntentFilter(WifiJobService.ACTION_WIFI);
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
//                Intent intent = new Intent(getBaseContext(), WifiJobService.class);
//                intent.setAction(WifiJobService.ACTION_WIFI);
//                startService(intent);
                scheduleJobs();

                btnStart.setEnabled(false);
                btnStop.setEnabled(true);
                break;
            case R.id.btnStop:
                stopService(new Intent(getBaseContext(), WifiJobService.class));
                btnStart.setEnabled(true);
                btnStop.setEnabled(false);
                cancelJobs();
                break;
            default:
                break;
        }
    }

    private void scheduleJobs()
    {
        final long REFRESH_INTERVAL = 10000;
        ComponentName serviceComponent = new ComponentName(this, WifiJobService.class);
        JobInfo.Builder builder = new JobInfo.Builder(WifiJobService.JOB_ID, serviceComponent);
        builder.setMinimumLatency(1 * 1000); // wait at least
        builder.setOverrideDeadline(3 * 1000); // maximum delay
        builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED); // require unmetered network
        //builder.setRequiresDeviceIdle(true); // device should be idle
        builder.setRequiresCharging(false); // we don't care if the device is charging or not
        builder.setPersisted(true);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//            builder.setMinimumLatency(REFRESH_INTERVAL);
//        } else {
//            builder.setPeriodic(REFRESH_INTERVAL);
//        }
        JobScheduler jobScheduler = (JobScheduler)getSystemService(Context.JOB_SCHEDULER_SERVICE);
        jobScheduler.schedule(builder.build());
    }

    private void cancelJobs()
    {
        JobScheduler jobScheduler = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
        jobScheduler.cancelAll();
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String displayText = intent.getStringExtra(WifiJobService.PARAM_WIFI);
            tvSignal.setText(displayText);
        }
    };
}
