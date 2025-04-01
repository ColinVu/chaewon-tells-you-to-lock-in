package com.example.socialmediausagenotifier;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import com.example.socialmediausagenotifier.VideoPopupActivity;

import java.util.List;

public class SocialTrackerService extends Service {
    private volatile boolean isRunning = false;

    private final String[] watchedApps = {
        "com.instagram.android",
        "com.reddit.frontpage",
        "com.twitter.android",
        "com.android.chrome"
    };

    private String currentApp = null;
    private long usageStartTime = 0;

    @Override
    public void onDestroy() {
        super.onDestroy();
        isRunning = false;
        Log.d("SocialTracker", "Service destroyed");
    }

    private Notification createForegroundNotification() {
        String channelId = "tracking_service";
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, "Tracking Service",
                    NotificationManager.IMPORTANCE_LOW);
            notificationManager.createNotificationChannel(channel);
        }

        return new NotificationCompat.Builder(this, channelId)
                .setContentTitle("Tracking App Usage")
                .setContentText("Monitoring social media activity...")
                .setSmallIcon(android.R.drawable.ic_media_play)
                .build();
    }

    @SuppressLint("ForegroundServiceType")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("SocialTracker", "Service started");
        startForeground(1, createForegroundNotification());
        isRunning = true;
        new Thread(() -> {
            while (isRunning) {
                try {
                    String topApp = getTopApp();
                    if (topApp == null) continue;

                    boolean isWatched = false;
                    for (String app : watchedApps) {
                        if (app.equals(topApp)) {
                            isWatched = true;
                            break;
                        }
                    }

                    if (isWatched) {
                        Log.d("SocialTracker", "IsWatched");
                        if (!topApp.equals(currentApp)) {
                            currentApp = topApp;
                            usageStartTime = System.currentTimeMillis();
                        } else {
                            long elapsed = System.currentTimeMillis() - usageStartTime;
                            Log.d("SocialTracker", "Elapsed: " + elapsed);
                            if (elapsed >= 60 * 15 * 1000) {
                                Intent popupIntent = new Intent(this, VideoPopupActivity.class);
                                popupIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                PendingIntent pendingIntent = PendingIntent.getActivity(
                                        this, 0, popupIntent, PendingIntent.FLAG_IMMUTABLE);

                                NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

// 🔧 Create channel if needed
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                    NotificationChannel channel = new NotificationChannel(
                                            "social_alerts", "Social Alerts", NotificationManager.IMPORTANCE_HIGH);
                                    notificationManager.createNotificationChannel(channel);
                                }

                                Notification notification = new NotificationCompat.Builder(this, "social_alerts")
                                        .setContentTitle("!!ALERT!!")
                                        .setContentText("Incoming Message from KIM CHAEWON")
                                        .setSmallIcon(android.R.drawable.ic_dialog_alert)
                                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                                        .setCategory(NotificationCompat.CATEGORY_REMINDER)
                                        .setContentIntent(pendingIntent)
                                        .setAutoCancel(true)
                                        .build();

                                notificationManager.notify(2, notification);
                                usageStartTime = System.currentTimeMillis();
                            }
                        }
                    } else {
                        currentApp = null;
                        usageStartTime = 0;
                    }

                    Thread.sleep(1000);
                } catch (Exception e) {
                    Log.e("SocialTracker", "Exception in loop: ", e);
                }
            }
        }).start();
        return START_STICKY;
    }

    private String getTopApp() {
        UsageStatsManager usm = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);
        long endTime = System.currentTimeMillis();
        long beginTime = endTime - 10000;
        List<UsageStats> usageStatsList = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, beginTime, endTime);
        UsageStats recentStats = null;
        for (UsageStats stats : usageStatsList) {
            if (recentStats == null || stats.getLastTimeUsed() > recentStats.getLastTimeUsed()) {
                recentStats = stats;
            }
        }
        return recentStats != null ? recentStats.getPackageName() : null;
    }

    private void showNotification(String title, String text) {
        String channelId = "social_alerts";
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, "Social Alerts", NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
        }

        Notification notification = new NotificationCompat.Builder(this, channelId)
                .setContentTitle(title)
                .setContentText(text)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .build();

        notificationManager.notify(1, notification);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}