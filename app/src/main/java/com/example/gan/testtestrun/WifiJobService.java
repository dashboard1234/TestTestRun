
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
        import android.hardware.Sensor;
        import android.hardware.SensorEvent;
        import android.hardware.SensorEventListener;
        import android.hardware.SensorManager;
        import android.hardware.TriggerEvent;
        import android.hardware.TriggerEventListener;
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

public class WifiJobService extends Service {
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
    private SensorManager sensorManager;
    // significant motion sensor is wake up sensor, it does not do batch sensor, waitless
    private Sensor sensor;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        isStopTimer = false;
        //jobParameters = params;

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
        //return super.onStartCommand(intent, flags, startId);
        return START_STICKY;
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
        receiver = new WifiReceiver();
        sleepReceiver = new SleepReceiver();
        reminderTriggered = false;

        sensorManager =
                (SensorManager) getSystemService(SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_SIGNIFICANT_MOTION);

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
        //jobFinished(jobParameters, false);
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
        private final SensorManager sensorManager =
                (SensorManager) getSystemService(SENSOR_SERVICE);
        // significant motion sensor is wake up sensor, it does not do batch sensor, waitless
        private final Sensor sensor = sensorManager.getDefaultSensor(Sensor.TYPE_SIGNIFICANT_MOTION);
        private final TriggerEventListener mListener = new TriggerEventListener() {
            @Override
            public void onTrigger(TriggerEvent event) {
                WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                if(wifiInfo == null)
                {
                    sensorManager.requestTriggerSensor(mListener, sensor);
                    return;
                }

                //int rssi = wifiManager.getConnectionInfo().getRssi();
                int rssi = wifiInfo.getRssi();
                int level = WifiManager.calculateSignalLevel(rssi, 100);
                level /= 100.0;

                if(level<0.4 && !reminderTriggered) {
                    speech.speak("主人，别忘了带钥匙", TextToSpeech.QUEUE_FLUSH, null);

                    PendingIntent mainIntent = PendingIntent.getActivity(getBaseContext(), 0, new Intent(getBaseContext(), MainActivity.class), 0);
                    NotificationCompat.Builder notifyBuilder = new NotificationCompat.Builder(getBaseContext())
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
                if(reminderTriggered && level > 0.6)
                    reminderTriggered = false;
                boolean success = sensorManager.requestTriggerSensor(mListener, sensor);
            }
        };

        @Override
        public void onReceive(Context context, Intent intent) {
            if(!intent.getAction().equals(Intent.ACTION_SCREEN_OFF) && !intent.getAction().equals(Intent.ACTION_SCREEN_ON))
                return;
            if(intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
                Runnable runnable = new Runnable() {
                        @Override
                        public void run() {
                            receiver.unregisterState();
                            receiver.unregisterSignal();
//                    receiver.registerState();
                    boolean success = sensorManager.requestTriggerSensor(mListener, sensor);
                        }
                };
                new Handler().postDelayed(runnable, WAIT_FOR_SYS_CLEAN_UP_DELAY);
            }
            if(intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                      receiver.unregisterState();
                      receiver.registerState();
//                        cancelAlarm();
                        sensorManager.cancelTriggerSensor(mListener, sensor);
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
                            //break;
                        }
                        else if(!TextUtils.isEmpty(selectedWifi) && selectedWifi.equals(connectedWifiName)) {
                            unregisterSignal();
                            registerSignal();
                            //break;
                        }
                    }
                    else {
                        unregisterSignal();
                    }
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


