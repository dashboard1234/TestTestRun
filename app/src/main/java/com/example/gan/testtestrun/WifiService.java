package com.example.gan.testtestrun;

import android.app.Service;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;

public class WifiService extends Service {
    public static final String ACTION_TIMER = "com.example.gan.testtestrun.action.timer";
    public static final String ACTION_WIFI = "com.example.gan.testtestrun.action.wifi";
    // TODO: Rename parameters
    public static final String PARAM_TIMER = "com.example.gan.testtestrun.extra.PARAM1";
    public static final String PARAM_WIFI = "com.example.gan.testtestrun.extra.PARAM2";

    private Integer timerCount;
    private boolean isStopTimer;
    WifiManager wifiManager;
    public WifiService() {

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //super.onStartCommand(intent, flags, startId);

        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_TIMER.equals(action)) {
                timerCount = intent.getIntExtra(PARAM_TIMER, 0);

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        for(int i = 0; i<timerCount; i++){
                            if(isStopTimer)
                                break;
                            try {
                                Thread.sleep(1000);
                                Intent intentBroadcast = new Intent(ACTION_TIMER);
                                intentBroadcast.putExtra(PARAM_TIMER, i);
                                intentBroadcast.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                                //LocalBroadcastManager.getInstance(getBaseContext()).sendBroadcast(intentBroadcast);
                                sendBroadcast(intentBroadcast);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        stopSelf();
                    }
                }).start();
            } else if (ACTION_WIFI.equals(action)) {
                String homeWifiName = intent.getStringExtra(PARAM_WIFI);
            }
        }

        //return START_STICKY;
        return START_REDELIVER_INTENT;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        isStopTimer = false;
        wifiManager = (WifiManager)getSystemService(WIFI_SERVICE);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isStopTimer = true;
    }

    public void runTimer(Integer timerCount)
    {
        for(int i = 0; i<timerCount; i++){
            if(isStopTimer)
                break;
            try {
                Thread.sleep(1000);
                Intent intent = new Intent(ACTION_TIMER);
                intent.putExtra(PARAM_TIMER, i);
                intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
                //sendBroadcast(intent);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        stopSelf();
    }
}
