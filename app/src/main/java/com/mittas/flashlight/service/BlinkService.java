package com.mittas.flashlight.service;


import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

import com.mittas.flashlight.MainActivity;
import com.mittas.flashlight.R;
import com.mittas.flashlight.CameraUtility;

public class BlinkService extends Service {
    private static final int ONGOING_NOTIFICATION_ID = 1;
    public static final String BLINKING_FREQUENCY = "BL_FREQUENCY";
    private final IBinder binder = new LocalBinder();
    private final String channel_ID = "my_channel_01";
    private NotificationChannel channel;
    private boolean blinking = false;
    private int blinkingFrequency;
    private long sleepTimeMillis;


    public class LocalBinder extends Binder {
        public BlinkService getService() {
            return BlinkService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }


    @Override
    public void onCreate() {

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Notification notification = createNotification();
        startForeground(ONGOING_NOTIFICATION_ID, notification);

        // Start blinking
        if (null != intent) {
            blinkingFrequency = intent.getIntExtra(BLINKING_FREQUENCY, 0);
        }
        computeSleepTime();
        blinking = true;
        new Thread(new BlinkRunnable()).start();

        return START_REDELIVER_INTENT;
    }


    @Override
    public void onDestroy() {
        // stop blinking, stop runnable
        blinking = false;
    }

    private Notification createNotification() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent =
                PendingIntent.getActivity(this, 0, notificationIntent, 0);

        doCreateNotificationChannel();

        return new NotificationCompat.Builder(this, channel_ID)
                .setContentTitle(getText(R.string.notification_title))
                .setContentText(getText(R.string.notification_message))
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pendingIntent)
                .build();
    }

    private void doCreateNotificationChannel() {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.O) {
            return;
        }

        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // The user-visible name of the channel.
        CharSequence name = getString(R.string.channel_name);

        int importance = NotificationManager.IMPORTANCE_HIGH;

        channel = new NotificationChannel(channel_ID, name, importance);

        // The user-visible description of the channel.
        String description = getString(R.string.channel_description);
        channel.setDescription(description);


        // Sets the notification light color for notifications posted to this
        // channel, if the device supports this feature.
        // mChannel.setLightColor(Color.RED);
        // mChannel.enableVibration(true);
        // mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});

        mNotificationManager.createNotificationChannel(channel);
    }


    public void onFrequencyChanged(int frequency) {
        this.blinkingFrequency = frequency;
        computeSleepTime();
    }

    private void computeSleepTime() {
        // Method 1
            /*long startTime = 2500;
            long interval = 250;
            sleepTimeMillis = startTime - ((blinkingFrequency - 1) * interval);*/

        // Method 2
        switch (blinkingFrequency) {
            case 1:
                sleepTimeMillis = 2000;
                break;
            case 2:
                sleepTimeMillis = 1500;
                break;
            case 3:
                sleepTimeMillis = 1000;
                break;
            case 4:
                sleepTimeMillis = 750;
                break;
            case 5:
                sleepTimeMillis = 500;
                break;
            case 6:
                sleepTimeMillis = 250;
                break;
            case 7:
                sleepTimeMillis = 200;
                break;
            case 8:
                sleepTimeMillis = 150;
                break;
            case 9:
                sleepTimeMillis = 100;
                break;
            case 10:
                sleepTimeMillis = 50;
                break;
            default:
                // do nothing;
        }

        if (sleepTimeMillis <= 0) {
            throw new IllegalArgumentException();
        }
    }

    private class BlinkRunnable implements Runnable {
        @Override
        public void run() {
            while (blinking) {
                CameraUtility.turnOnOff(BlinkService.this, true);
                try {
                    Thread.sleep(sleepTimeMillis);
                } catch (InterruptedException e) {
                    // Restore interrupt status.
                    Thread.currentThread().interrupt();
                }

                if (!blinking) {
                    break;
                }

                CameraUtility.turnOnOff(BlinkService.this, false);
                try {
                    Thread.sleep(sleepTimeMillis);
                } catch (InterruptedException e) {
                    // Restore interrupt status.
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

}
