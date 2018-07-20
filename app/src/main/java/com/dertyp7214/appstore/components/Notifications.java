/*
 * Copyright (c) 2018.
 * Created by Josua Lengwenath
 */

package com.dertyp7214.appstore.components;

import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

import com.dertyp7214.appstore.R;
import com.dertyp7214.appstore.dev.Logs;
import com.dertyp7214.appstore.screens.AppScreen;

import java.util.ArrayList;
import java.util.List;

public class Notifications {

    private int id;
    private int max;
    private boolean progress;
    private Logs logs;
    private Thread thread;
    private Activity context;
    private NotificationCompat.Builder builder;

    private static NotificationManager notificationManager;
    private static List<Integer> ids = new ArrayList<>();

    public Notifications(Activity context, int id, String title, String subTitle, String content, Bitmap icon, boolean progress) {
        this.context = context;
        this.progress = progress;
        this.max = 100;
        this.id = id;
        this.logs = Logs.getInstance(context);

        if (notificationManager == null)
            notificationManager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        builder = null;
        builder = new NotificationCompat.Builder(context)
                .setSmallIcon(android.R.drawable.stat_sys_download)
                .setLargeIcon(icon)
                .setContentTitle(title)
                .setContentText(content)
                .setPriority(
                        progress ? NotificationCompat.PRIORITY_MIN : NotificationCompat.PRIORITY_LOW)
                .setSubText(subTitle);
        if (progress) {
            builder.setSubText("0%");
            builder.setOngoing(true);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String groupId = context.getString(R.string.app_name) + "_id";
            CharSequence groupName = context.getString(R.string.app_name);
            NotificationChannel channel = new NotificationChannel(groupId, groupName,
                    progress ? NotificationManager.IMPORTANCE_NONE : NotificationManager.IMPORTANCE_LOW);
            notificationManager.createNotificationChannel(channel);
            builder.setChannelId(groupId);
        }
        logs.debug("NOTI", "BUILDER: " + builder);
    }

    private void callAction() {
        if (thread != null)
            if (thread.isAlive())
                thread.interrupt();
        thread = new Thread(() -> {
            try {
                Thread.sleep(300000);
                context.runOnUiThread(this::removeNotification);
            } catch (InterruptedException ignored) {
            }
        });
        thread.start();
    }

    public void showNotification() {
        ids.add(id);
        notificationManager.notify(id, builder.build());
        callAction();
    }

    public void addButton(int icon, String text, Intent intent) {
        PendingIntent pendingIntent =
                PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.addAction(icon, text, pendingIntent);
    }

    public void setCancelButton() {
        Intent closeButton = new Intent("Download_Cancelled");
        closeButton.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        closeButton.putExtra("id", id);
        builder.addAction(R.drawable.ic_close, context.getString(R.string.notification_cancel),
                PendingIntent.getBroadcast(context, 0, closeButton, 0));
    }

    public void setProgress(int progress) {
        if (this.progress) {
            builder.setSmallIcon(android.R.drawable.stat_sys_download);
            builder.setSubText(progress + "%");
            builder.setProgress(this.max, progress, false);
            notificationManager.notify(id, builder.build());
            callAction();
        }
    }

    private void clearActions() {
        builder.mActions.clear();
    }

    private void removeProgress() {
        this.progress = false;
        builder.setProgress(0, 0, false);
        builder.setSubText(null);
        builder.setOngoing(false);
    }

    public void setFinished() {
        removeProgress();
        clearActions();
        builder.setSubText(context.getString(R.string.notification_finished));
        builder.setOngoing(false);
        builder.setSmallIcon(android.R.drawable.stat_sys_download_done);
        notificationManager.notify(id, builder.build());
        callAction();
    }

    public void setCanceled() {
        setCanceled(context.getString(R.string.notification_canceled));
    }

    public void setCanceled(String message) {
        removeProgress();
        clearActions();
        builder.setSubText(message);
        builder.setSmallIcon(android.R.drawable.ic_menu_close_clear_cancel);
        logs.debug("CANCEL", id + "");
        notificationManager.notify(id, builder.build());
        callAction();
    }

    public void removeNotification() {
        notificationManager.cancel(id);
    }

    public static class DownloadCancelReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                if (intent.getExtras() != null) {
                    AppScreen.downloadHashMap.get(intent.getExtras().getInt("id")).cancel();
                    notificationManager.cancel(intent.getExtras().getInt("id"));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
