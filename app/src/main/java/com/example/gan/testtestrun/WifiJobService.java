
        package com.example.gan.testtestrun;

        import android.app.AlarmManager;
        import android.app.IntentService;
        import android.app.NotificationManager;
        import android.app.PendingIntent;
        import android.app.Service;
        import android.app.job.JobParameters;
        import android.app.job.JobService;
        import android.content.BroadcastReceiver;
        import android.content.Context;
        import android.content.Intent;
        import android.content.IntentFilter;
        import android.content.SharedPreferences;
        import android.net.NetworkInfo;
        import android.net.wifi.WifiInfo;
        import android.net.wifi.WifiManager;
        import android.os.Handler;
        import android.os.IBinder;
        import android.os.PowerManager;
        import android.os.SystemClock;
        import android.speech.tts.TextToSpeech;
        import android.support.v4.app.NotificationCompat;
        import android.support.v4.content.LocalBroadcastManager;
        import android.text.TextUtils;
        import android.util.Log;
        import android.view.WindowManager;

        import java.util.Locale;

public class WifiJobService extends JobService {
    public static final String ACTION_TIMER = "com.example.gan.testtestrun.action.timer";
    public static final String ACTION_WIFI = "com.example.gan.testtestrun.action.wifi";
    public static final Integer NOTIFY_ID = 1234;
    public static final Integer JOB_ID = 4321;
    // TODO: Rename parameters
    public static final String PARAM_TIMER = "com.example.gan.testtestrun.extra.PARAM1";
    public static final String PARAM_WIFI = "com.example.gan.testtestrun.extra.PARAM2";

    static final String TAG = WifiJobService.class.getName();
    private Integer timerCount;
    private boolean isStopTimer;
    WifiManager wifiManager;
    WifiReceiver receiver;
    String selectedWifi = "";
    boolean reminderTriggered;
    TextToSpeech speech;
    JobParameters jobParameters;
    private SleepReceiver sleepReceiver;
    PowerManager.WakeLock wakeLock;

    @Override
    public boolean onStartJob(final JobParameters params) {
        isStopTimer = false;
        jobParameters = params;
//        Intent intent = new Intent(getBaseContext(), WifiJobService.class);
//        intent.setAction(WifiJobService.ACTION_WIFI);
//        startService(intent);
        //startForeground(Process.myPid(), new Notification());

//        Intent notificationIntent = new Intent(this, WifiJobService.class);
//
//        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
//                notificationIntent, 0);
//
//        Notification notification = new NotificationCompat.Builder(this)
//                //.setSmallIcon(R.mipmap.app_icon)
//                .setContentTitle("My Awesome App")
//                .setContentText("Doing some work...")
//                .setContentIntent(pendingIntent).build();
//
//        startForeground(1337, notification);

        new Thread(new Runnable() {
            @Override
            public void run() {
                receiver.registerState();
                while(!isStopTimer){
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();

        //receiver.registerState();
        //wakeLock.acquire();

        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        receiver.unregisterState();
        //releaseLock(wakeLock);
        isStopTimer = true;
        return true;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        isStopTimer = false;
        receiver = new WifiReceiver();
        sleepReceiver = new SleepReceiver();
        reminderTriggered = false;

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
                }
                else
                    Log.e("error", "Initilization Failed!");
            }
        });

        wifiManager = (WifiManager)getSystemService(WIFI_SERVICE);
        SharedPreferences sharedPreferences = getSharedPreferences(WifiSettingActivity.WIFI_PREFERENCE, Context.MODE_PRIVATE);
        if(sharedPreferences != null)
            selectedWifi = sharedPreferences.getString(WifiSettingActivity.SELECTED_WIFI, "");
        receiver.registerState();

        PowerManager pm = (PowerManager)getSystemService(Context.POWER_SERVICE);
        //wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "TAG");
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_SCREEN_ON);
        registerReceiver(sleepReceiver, filter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //isStopTimer = true;
        receiver.unregisterState();
        speech.shutdown();
        //releaseLock(wakeLock);
        jobFinished(jobParameters, false);
    }

    public void releaseLock(PowerManager.WakeLock wakelock)
    {
        if(wakelock.isHeld())
            wakelock.release();
    }

    public class RepeatOnSleep extends IntentService{
        public final static int REQUEST_CODE = 12345;
        public RepeatOnSleep(){
            super("RepeatOnSleep");
        }

        @Override
        public int onStartCommand(Intent intent, int flags, int startId) {
            receiver.unregisterState();
            receiver.registerState();
            return super.onStartCommand(intent, flags, startId);
        }

        @Override
        protected void onHandleIntent(Intent intent) {
            receiver.unregisterState();
            receiver.registerState();
        }
    }

    private class SleepReceiver extends BroadcastReceiver{

        private static final long WAIT_FOR_SYS_CLEAN_UP_DELAY = 1000;

        @Override
        public void onReceive(Context context, Intent intent) {
            if(!intent.getAction().equals(Intent.ACTION_SCREEN_OFF) && !intent.getAction().equals(Intent.ACTION_SCREEN_ON))
                return;
            if(intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
//                    receiver.unregisterState();
//                    receiver.registerState();
                        scheduleAlarm();
                    }
                };
                new Handler().postDelayed(runnable, WAIT_FOR_SYS_CLEAN_UP_DELAY);
            }
            if(intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
//                    receiver.unregisterState();
//                    receiver.registerState();
                        cancelAlarm();
                    }
                };
                new Handler().post(runnable);
            }
        }
    }

    private void scheduleAlarm() {
        Intent intent = new Intent(getApplicationContext(), RepeatOnSleep.class);
        // Create a PendingIntent to be triggered when the alarm goes off
        final PendingIntent pIntent = PendingIntent.getService(this, RepeatOnSleep.REQUEST_CODE,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        long firstMillis = System.currentTimeMillis(); // alarm is set right away
        AlarmManager alarm = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);

//        alarm.setInexactRepeating(AlarmManager.RTC_WAKEUP, firstMillis,
//                10000, pIntent);
        alarm.setRepeating(AlarmManager.RTC, firstMillis,
                10000, pIntent);
    }

    private void cancelAlarm() {
        Intent intent = new Intent(getApplicationContext(), RepeatOnSleep.class);
        final PendingIntent pIntent = PendingIntent.getService(this, RepeatOnSleep.REQUEST_CODE,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarm = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        alarm.cancel(pIntent);
    }

    private class WifiReceiver extends BroadcastReceiver {
        //private BroadcastReceiver receiver = new BroadcastReceiver(){
        boolean mIsSigReceiverRegistered;
        boolean mIsStateReceiverRegistered;

        //public void registerState()
        public  Intent registerState()
        {
            if(mIsStateReceiverRegistered)
                //    return;
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
                //   return;
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
                case WifiManager.RSSI_CHANGED_ACTION:
                    String name = getWifiNetworkName(context);
                    float percent = getSignalStrength(intent);
                    Intent intentSignalChange = new Intent(ACTION_WIFI);
                    intentSignalChange.putExtra(PARAM_WIFI, name + ": " + percent);
                    intentSignalChange.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                    LocalBroadcastManager.getInstance(getBaseContext()).sendBroadcast(intentSignalChange);

                    // ToDo: rework logic
                    if(percent<0.4 && !reminderTriggered) {
                        speech.speak("主人，别忘了带钥匙", TextToSpeech.QUEUE_FLUSH, null);

                        PendingIntent mainIntent = PendingIntent.getActivity(context, 0, new Intent(context, MainActivity.class), 0);
                        NotificationCompat.Builder notifyBuilder = new NotificationCompat.Builder(context)
                                .setContentTitle("Friendly Reminder")
                                .setContentText("主人，别忘了带钥匙")
                                .setTicker("ticker")
                                .setSmallIcon(R.drawable.ic_stat_event)
                                .setAutoCancel(true);
                        notifyBuilder.setContentIntent(mainIntent);
                        notifyBuilder.setDefaults(NotificationCompat.DEFAULT_VIBRATE);
                        NotificationManager notificationManager = (NotificationManager)
                                getSystemService(Context.NOTIFICATION_SERVICE);
                        try {
                            notificationManager.notify(NOTIFY_ID, notifyBuilder.build());
                        }
                        catch (Exception ex)
                        {
                            Log.e(TAG, "notify error", ex);
                        }

                        reminderTriggered = true;
                    }
                    if(reminderTriggered && percent > 0.6)
                        reminderTriggered = false;
                    break;
                case WifiManager.NETWORK_STATE_CHANGED_ACTION:
                    boolean connected = getWifiConnected(intent);
                    String connText = connected ? "Wifi connected" : "Wifi disconnected";
                    Intent intentConnChange = new Intent(ACTION_WIFI);
                    intentConnChange.putExtra(PARAM_WIFI, connText);
                    intentConnChange.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                    LocalBroadcastManager.getInstance(getBaseContext()).sendBroadcast(intentConnChange);
                    //sendBroadcast(intentConnChange);
                    if(connected) {
                        String connectedWifiName = getWifiNetworkName(context);
                        if (!TextUtils.isEmpty(selectedWifi) && !selectedWifi.equals(connectedWifiName)) {
                            unregisterSignal();
                            break;
                        }
                        else if(!TextUtils.isEmpty(selectedWifi) && selectedWifi.equals(connectedWifiName)) {
                            unregisterSignal();
                            registerSignal();
                            break;
                        }
                    }
                    unregisterSignal();
                    break;
                default:
                    Log.d(TAG, "Invalid action type");
                    break;
            }
        }
    }

//    private void acquireWakelock()
//    {
//        KeyguardManager lock = ((KeyguardManager) getSystemService(Activity.KEYGUARD_SERVICE)).newKeyguardLock(KEYGUARD_SERVICE);
//        PowerManager powerManager = ((PowerManager) getSystemService(Context.POWER_SERVICE));
//        PowerManager.WakeLock wake = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "TAG");
//
//        //lock.disableKeyguard();
//        wake.acquire();
//        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
//                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
//                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
//                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
//                | WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON);
//    }

    private float getSignalStrength(Intent intent){
        float level = intent.getIntExtra(WifiManager.EXTRA_NEW_RSSI, -1);
        level = WifiManager.calculateSignalLevel((int)level, 100);
        level /= 100.0;
        return level;
    }

    private boolean getWifiConnected(Intent intent) {
        NetworkInfo info = (NetworkInfo) intent.getExtras().get(WifiManager.EXTRA_NETWORK_INFO);
        return info != null && info.getState().equals(NetworkInfo.State.CONNECTED);
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


