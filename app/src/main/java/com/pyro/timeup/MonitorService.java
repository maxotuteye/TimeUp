package com.pyro.timeup;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.SortedMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;

public class MonitorService extends Service {


    public static final int INTERVAL = 5000;
    private Handler handler = new Handler();
    public Runnable runnable;
    private Timer timer = null;
    public String app;
    public UsageStatsManager usageStatsManager;
    long start, end;
    List<UsageStats> usageStatsList;
    Float timeInForeground;
    String packageName;
    Map<String, Float> timeMap;
    Map<String, ?> apps;
    ApkInfoExtractor extractor;
    SharedPreferences preferences;

    public MonitorService() {
        super();
    }

    @Override
    public void onCreate() {
/*
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channel_id = "timeup_channel";
            NotificationChannel channel = new NotificationChannel(channel_id,
                    "Timer Service",
                    NotificationManager.IMPORTANCE_LOW);

            ((NotificationManager) getSystemService(NOTIFICATION_SERVICE)).createNotificationChannel(channel);
            Notification notification = new NotificationCompat.Builder(this, channel_id)
                    .setContentTitle(getString(R.string.timeup_monitor))
                    .setSmallIcon(R.mipmap.ic_launcher_foreground)
                    .setContentText(getString(R.string.timeup_m_serv)).build();

            startForeground(19777, notification);
        }
        preferences = null;
        extractor = new ApkInfoExtractor(getApplicationContext());
        start = 0;
        if (timer != null) {
            timer.cancel();
        } else timer = new Timer();
        timer.scheduleAtFixedRate(new TimeDisplay(), 0, INTERVAL);*/

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (Objects.equals(intent.getStringExtra("stop"), "true")) {
            Log.i("KILL", "Monitor is being stopped");
            onDestroy();
        }
        Log.i("KILL", "Monitor allowed to start");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channel_id = "timeup_channel";
            NotificationChannel channel = new NotificationChannel(channel_id, "Timer Service", NotificationManager.IMPORTANCE_LOW);

            ((NotificationManager) getSystemService(NOTIFICATION_SERVICE)).createNotificationChannel(channel);
            Notification notification = new NotificationCompat.Builder(this, channel_id)
                    .setContentTitle(getString(R.string.timeup_monitor))
                    .setSmallIcon(R.mipmap.ic_launcher_foreground)
                    .setContentText(getString(R.string.timeup_m_serv)).build();

            startForeground(19777, notification);
        }
        preferences = null;
        extractor = new ApkInfoExtractor(getApplicationContext());
        start = 0;
        if (timer != null) {
            timer.cancel();
        } else timer = new Timer();
        timer.scheduleAtFixedRate(new TimeDisplay(), 0, INTERVAL);

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        Log.i("KILL", "Destroying Service");
        preferences = null;
        timer.cancel();
        timer = null;
        handler.removeCallbacks(runnable);
        handler = null;
        stopForeground(true);
        stopSelf();
        super.onDestroy();
        Log.i("KILL", "Destroyed Service");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public class TimeDisplay extends TimerTask {

        @Override
        public void run() {
            runnable = () -> {
                preferences = null;
                apps = getTimedApps();
                timeMap = new HashMap<>();
                end = System.currentTimeMillis();
                usageStatsManager = (UsageStatsManager) getApplicationContext().getSystemService(USAGE_STATS_SERVICE);
                assert usageStatsManager != null;
                usageStatsList = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, start, end);
                if (usageStatsList != null) {
                    for (UsageStats usageStats : usageStatsList) {
                        timeInForeground = (float) usageStats.getTotalTimeInForeground();
                        packageName = usageStats.getPackageName();
                        timeMap.put(packageName, timeInForeground);

                        if (getForegroundTask().equals(packageName)) {
                            if (timeMap.get(packageName) != null) {
                                float c = timeMap.get(packageName);
                                timeMap.put(packageName, c + Time(usageStats.getLastTimeUsed()));
                            } else timeMap.put(packageName, timeInForeground);
                        } else {
                            timeMap.remove(packageName);
                            timeMap.put(packageName, timeInForeground);
                        }
                    }
                    apps = getTimedApps();
                    Control(apps, timeMap);
                }
            };

//            handler.post(() -> {
//
//            });
            handler.post(runnable);
        }
    }

    boolean isTimeUp(Float maxTime, Float usedTime) {
        return usedTime >= maxTime;
    }

    Map<String, ?> getTimedApps() {
        preferences = getSharedPreferences("hours", MODE_MULTI_PROCESS);
        Log.i("KILL", "getTimedApps: " + preferences.getAll().toString());
        if (preferences.getAll().isEmpty())
            onDestroy();
        return preferences.getAll();
    }

    private void kill(String time) {
        time = extractor.getAppName(time);
        Log.i("KILL", "Running kill on " + time);
        Toast notice = Toast.makeText(getApplicationContext(), time + "'s time is up\nPlease exit the app", Toast.LENGTH_SHORT);
        notice.setGravity(Gravity.CENTER, 0, 0);
        notice.show();
        //beep();   // removed due to noise complaints, will be added as a setting
        vibrate();
        // 2/2/2020 beep and vibration added
    }

    private void beep() {
        Uri sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        if (sound == null) {
            sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        }
        MediaPlayer mediaPlayer = MediaPlayer.create(getApplicationContext(), sound);
        mediaPlayer.start();
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        mediaPlayer.stop();
    }

    private void vibrate() {
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        int millis = 2000;
        // Vibrate for milliseconds
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            assert v != null;
            v.vibrate(VibrationEffect.createOneShot(millis, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            assert v != null;
            v.vibrate(millis);
        }
    }

    private float Time(long lastTimeUsed) {
        Date now = new Date(System.currentTimeMillis());
        Date then = new Date(lastTimeUsed);
        return (float) (now.getTime() - then.getTime());
    }

    void Control(Map<String, ?> maxTimes, Map<String, Float> currentTimes) {
        for (String Time :
                maxTimes.keySet()) {
            if (currentTimes.keySet().contains(Time) &&
                    isTimeUp(Float.valueOf(String.valueOf(maxTimes.get(Time))), currentTimes.get(Time))
                    && getForegroundTask().equals(Time)) {
                Log.i("KILL", "App is " + Time + "overtime:" +
                        (currentTimes.get(Time) - Float.valueOf(String.valueOf(maxTimes.get(Time)))));
                kill(Time);
            }
        }
    }

    private void printForegroundTask() {
        String currentApp = "NULL";
        UsageStatsManager usm = (UsageStatsManager) this.getSystemService(Context.USAGE_STATS_SERVICE);
        long time = System.currentTimeMillis();
        List<UsageStats> appList = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, time - 1000 * 500, time);
        if (appList != null && appList.size() > 0) {
            SortedMap<Long, UsageStats> mySortedMap = new TreeMap<>();
            for (UsageStats usageStats : appList) {
                mySortedMap.put(usageStats.getLastTimeUsed(), usageStats);
            }
            if (!mySortedMap.isEmpty()) {
                currentApp = mySortedMap.get(mySortedMap.lastKey()).getPackageName();
            }
        }
        Toast.makeText(getApplicationContext(), "Foreground: " + currentApp, Toast.LENGTH_SHORT).show();
    }

    private String getForegroundTask() {
        String currentApp = null;
        UsageStatsManager usm = (UsageStatsManager) this.getSystemService(Context.USAGE_STATS_SERVICE);
        long time = System.currentTimeMillis();
        List<UsageStats> appList = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, time - 1000 * 500, time);
        if (appList != null && appList.size() > 0) {
            SortedMap<Long, UsageStats> mySortedMap = new TreeMap<>();
            for (UsageStats usageStats : appList) {
                mySortedMap.put(usageStats.getLastTimeUsed(), usageStats);
            }
            if (!mySortedMap.isEmpty()) {
                currentApp = mySortedMap.get(mySortedMap.lastKey()).getPackageName();
            }
        }
        return currentApp;
    }
}
