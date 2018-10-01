/*
 *
 *  * Copyright (c) 2018.
 *  * Created by Josua Lengwenath
 *
 */

package com.dertyp7214.appstore.service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.preference.PreferenceManager;

import com.dertyp7214.appstore.Config;
import com.dertyp7214.appstore.R;
import com.dertyp7214.appstore.Utils;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;

import androidx.core.app.NotificationCompat;

public class MessagingService extends FirebaseMessagingService {

    final String tokenPreferenceKey = "fcm_token";
    final static String update = "update";

    @Override
    public void onNewToken(String token) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences.edit().putString(tokenPreferenceKey, token).apply();

        FirebaseMessaging.getInstance().subscribeToTopic(update);
    }

    private Bitmap getImage(String id) {
        final Bitmap bitmap = null;
        try {
            URL url = new URL(Config.API_URL + "/apps/" + id + "/icon.png");
            Bitmap pic = BitmapFactory.decodeStream(url.openConnection().getInputStream());
            return pic;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        if (Objects.equals(remoteMessage.getFrom(), "/topics/" + update)) {
            sendNotification(remoteMessage.getData().get("title"),
                    remoteMessage.getData().get("content-text"),
                    remoteMessage.getData().get("package"));
        }
    }

    private void sendNotification(String title, String message, String packageName) {
        NotificationCompat.Builder notification = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_file_download_white_48dp)
                .setLargeIcon(Utils.drawableToBitmap(getResources().getDrawable(
                        R.drawable.ic_file_download_white_48dp)))
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setSubText(title);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String groupId = getString(R.string.app_name) + "_id";
            CharSequence groupName = getString(R.string.app_name) + " Updates";
            NotificationChannel channel = new NotificationChannel(groupId, groupName,
                    NotificationManager.IMPORTANCE_DEFAULT);
            ((NotificationManager) Objects
                    .requireNonNull(getSystemService(Context.NOTIFICATION_SERVICE)))
                    .createNotificationChannel(channel);
            notification.setChannelId(groupId);
        }
        notification.build();
    }
}
