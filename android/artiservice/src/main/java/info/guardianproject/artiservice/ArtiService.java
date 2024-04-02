package info.guardianproject.artiservice;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;

import info.guardianproject.arti.Arti;

public class ArtiService extends Service {

    public static final String NOTIFICATION_CHANNEL_ID = "ArtiService";
    private volatile boolean started;

    private static Notification newNotification(Context context) {
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel channel = new NotificationChannel(
                    NOTIFICATION_CHANNEL_ID,
                    "890",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(channel);
            return new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID).setContentTitle("4567 title").setContentText("4567 text").build();
        } else {
            return new NotificationCompat.Builder(context).setContentTitle("4567 title").setContentText("4567 text").build();
        }
    }

    public static void startService(Context context) {
        Intent intent = new Intent(context, ArtiService.class);
        intent.setAction("gogogo");
        if (Build.VERSION.SDK_INT >= 26) {
            context.startForegroundService(intent);
        } else {
            context.startService(intent);
        }
    }

    public static void stopService(Context context) {
        Intent intent = new Intent(context, ArtiService.class);
        intent.setAction("halt");
        context.stopService(intent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!started) {
            started = true;
            startArtiProxy();
        }

        // If we get killed, after returning from here, restart
        return Service.START_STICKY;
    }

    private void startArtiProxy() {
        // start sticky foreground notification
        if (Build.VERSION.SDK_INT >= 34) {
            startForeground(1, newNotification(this), ServiceInfo.FOREGROUND_SERVICE_TYPE_SYSTEM_EXEMPTED);
        } else if (Build.VERSION.SDK_INT >= 29) {
            startForeground(1, newNotification(this), ServiceInfo.FOREGROUND_SERVICE_TYPE_NONE);
        } else {
            startForeground(1, newNotification(this));
        }

        Arti.startSocksProxy(ArtiService.this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // We don't provide binding, so return null
        return null;
    }

    @Override
    public void onDestroy() {
        stopForeground(true);
    }
}
