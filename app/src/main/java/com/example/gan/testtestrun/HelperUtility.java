package com.example.gan.testtestrun;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.util.Log;

import java.util.List;

/**
 * Created by gan on 3/8/17.
 */

public class HelperUtility {
    private static String LOG_TAG = HelperUtility.class.getName();
    public static String INTENT_ACTION_MONITOR_WIFI = "com.example.gan.testtestrun.action.monitorwifi";

    public static boolean isServiceRunning(Context context, Class<?> serviceClass){

        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}
