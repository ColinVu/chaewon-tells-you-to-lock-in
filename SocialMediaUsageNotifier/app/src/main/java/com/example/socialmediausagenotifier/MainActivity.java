package com.example.socialmediausagenotifier;

import android.app.Activity;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.Manifest;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.util.Log;

public class MainActivity extends Activity {
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));
            new Handler().postDelayed(() -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    Log.d("MainActivity", "Starting foreground service");
                    startForegroundService(new Intent(this, SocialTrackerService.class));
                } else {
                    startService(new Intent(this, SocialTrackerService.class));
                }

                finish();
            }, 500);
            finish();
        }
}