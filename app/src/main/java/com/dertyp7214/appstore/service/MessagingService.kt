/*
 *
 *  * Copyright (c) 2018.
 *  * Created by Josua Lengwenath
 *
 */

package com.dertyp7214.appstore.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.preference.PreferenceManager
import androidx.core.app.NotificationCompat
import com.dertyp7214.appstore.Config
import com.dertyp7214.appstore.R
import com.dertyp7214.appstore.Utils
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import java.io.IOException
import java.net.URL
import java.util.*

@Suppress("DEPRECATION")
class MessagingService : FirebaseMessagingService() {

    private val tokenPreferenceKey = "fcm_token"

    override fun onNewToken(token: String?) {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        sharedPreferences.edit().putString(tokenPreferenceKey, token).apply()

        FirebaseMessaging.getInstance().subscribeToTopic(update)
    }

    private fun getImage(id: String): Bitmap? {
        return try {
            val url = URL(Config.API_URL + "/apps/" + id + "/icon.png")
            BitmapFactory.decodeStream(url.openConnection().getInputStream())
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage?) {
        super.onMessageReceived(remoteMessage)

        if (remoteMessage!!.from == "/topics/$update") {
            sendNotification(remoteMessage.data["title"],
                    remoteMessage.data["content-text"],
                    remoteMessage.data["package"])
        }
    }

    private fun sendNotification(title: String?, message: String?, packageName: String?) {
        val notification = NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_file_download_white_48dp)
                .setLargeIcon(Utils.drawableToBitmap(resources.getDrawable(
                        R.drawable.ic_file_download_white_48dp)))
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setSubText(title)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val groupId = getString(R.string.app_name) + "_id"
            val groupName = getString(R.string.app_name) + " Updates"
            val channel = NotificationChannel(groupId, groupName,
                    NotificationManager.IMPORTANCE_DEFAULT)
            (Objects
                    .requireNonNull(getSystemService(Context.NOTIFICATION_SERVICE)) as NotificationManager)
                    .createNotificationChannel(channel)
            notification.setChannelId(groupId)
        }
        notification.build()
    }

    companion object {
        internal const val update = "update"
    }
}
