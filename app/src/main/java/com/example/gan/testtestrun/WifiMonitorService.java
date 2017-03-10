package com.example.gan.testtestrun;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.support.v4.content.LocalBroadcastManager;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class WifiMonitorService extends IntentService {
    public static final String ACTION_TIMER = "com.example.gan.testtestrun.action.timer";
    public static final String ACTION_TIMER2 = "com.example.gan.testtestrun.action.timer2";
    public static final String ACTION_WIFI = "com.example.gan.testtestrun.action.wifi";
    // TODO: Rename parameters
    public static final String PARAM_TIMER = "com.example.gan.testtestrun.extra.PARAM1";
    public static final String PARAM_WIFI = "com.example.gan.testtestrun.extra.PARAM2";

    public WifiMonitorService() {
        super("WifiMonitorService");
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_TIMER.equals(action)) {
                Integer timerCount = intent.getIntExtra(PARAM_TIMER, 0);
                handleActionTimer(timerCount);
            } else if (ACTION_WIFI.equals(action)) {
                String homeWifiName = intent.getStringExtra(PARAM_WIFI);
                handleActionWifi(homeWifiName);
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionTimer(Integer timerCount) {
        // TODO: Handle action Foo
        for(int i = 0; i<timerCount; i++){
            try {
                Thread.sleep(1000);
                Intent intent = new Intent(ACTION_TIMER2);
                intent.putExtra(PARAM_TIMER, i);
                //intent.setClass(this, WifiMonitorService.class);
                intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Handle action Baz in the provided background thread with the provided
     * parameters.
     */
    private void handleActionWifi(String homeWifiName) {
        // TODO: Handle action Baz
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
