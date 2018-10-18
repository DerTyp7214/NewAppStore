/*
 * Copyright (c) 2018.
 * Created by Josua Lengwenath
 */

package com.dertyp7214.appstore.components

import android.annotation.SuppressLint
import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import androidx.core.app.NotificationCompat
import com.dertyp7214.appstore.BuildConfig
import com.dertyp7214.appstore.R
import com.dertyp7214.appstore.dev.Logs
import java.util.*

@Suppress("DEPRECATION")
class Notifications(private val context: Context, private val id: Int, title: String, subTitle: String, content: String, icon: Bitmap?, private var progress: Boolean) {
    private val max: Int = 100
    private var logs: Logs? = null
    private var thread: Thread? = null
    private var activity: Activity? = null
    private var builder: NotificationCompat.Builder? = null

    constructor(activity: Activity, id: Int, title: String, subTitle: String, content: String, icon: Bitmap?, progress: Boolean) : this(activity as Context, id, title, subTitle, content, icon, progress) {
        this.logs = Logs.getInstance(activity)
        this.activity = activity
        logs!!.debug("NOTI", "BUILDER: " + builder!!)
    }

    init {
        if (notificationManager == null)
            notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        builder = null
        val groupId = context.getString(R.string.app_name) + "_id"
        val groupName = context.getString(R.string.app_name)
        builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            NotificationCompat.Builder(context)
        else
            NotificationCompat.Builder(context, groupId)
        builder!!.setSmallIcon(android.R.drawable.stat_sys_download)
                .setLargeIcon(icon)
                .setContentTitle(title)
                .setContentText(content)
                .setPriority(
                        if (progress) NotificationCompat.PRIORITY_MIN else NotificationCompat.PRIORITY_LOW)
                .setSubText(subTitle)
        if (progress) {
            builder!!.setSubText("0%")
            builder!!.setOngoing(true)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(groupId, groupName,
                    if (progress) NotificationManager.IMPORTANCE_MIN else NotificationManager.IMPORTANCE_LOW)
            notificationManager!!.createNotificationChannel(channel)
            builder!!.setChannelId(groupId)
            if (progress) builder!!.setGroup("${BuildConfig.APPLICATION_ID}.apk_download")
        }
    }

    private fun callAction() {
        if (thread != null)
            if (thread!!.isAlive)
                thread!!.interrupt()
        thread = Thread {
            try {
                Thread.sleep(300000)
                activity!!.runOnUiThread { this.removeNotification() }
            } catch (ignored: InterruptedException) {
            }
        }
        thread!!.start()
    }

    fun showNotification() {
        ids.add(id)
        notificationManager!!.notify(id, builder!!.build())
        callAction()
    }

    fun addButton(icon: Int, text: String, intent: Intent) {
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        builder!!.addAction(icon, text, pendingIntent)
    }

    fun setCancelButton() {
        val closeButton = Intent("Download_Cancelled")
        closeButton.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        closeButton.putExtra("id", id)
        builder!!.addAction(R.drawable.ic_close, context.getString(R.string.notification_cancel),
                PendingIntent.getBroadcast(context, 0, closeButton, 0))
    }

    fun setProgress(progress: Int, size: String) {
        if (this.progress) {
            builder!!.setSmallIcon(android.R.drawable.stat_sys_download)
            if (size.isEmpty()) builder!!.setSubText("$progress%")
            else builder!!.setSubText("$size ($progress%)")
            builder!!.setProgress(this.max, progress, false)
            notificationManager!!.notify(id, builder!!.build())
            callAction()
        }
    }

    fun setProgress(progress: Int) {
        setProgress(progress, "")
    }

    @SuppressLint("RestrictedApi")
    private fun clearActions() {
        builder!!.mActions.clear()
    }

    private fun removeProgress() {
        this.progress = false
        builder!!.setProgress(0, 0, false)
        builder!!.setSubText(null)
        builder!!.setOngoing(false)
    }

    fun setFinished() {
        removeProgress()
        clearActions()
        builder!!.setSubText(context.getString(R.string.notification_finished))
        builder!!.setOngoing(false)
        builder!!.setSmallIcon(android.R.drawable.stat_sys_download_done)
        notificationManager!!.notify(id, builder!!.build())
        callAction()
    }

    @JvmOverloads
    fun setCanceled(message: String = context.getString(R.string.notification_canceled)) {
        removeProgress()
        clearActions()
        builder!!.setSubText(message)
        builder!!.setSmallIcon(android.R.drawable.ic_menu_close_clear_cancel)
        if (activity != null)
            logs!!.debug("CANCEL", id.toString() + "")
        notificationManager!!.notify(id, builder!!.build())
        callAction()
    }

    fun removeNotification() {
        notificationManager!!.cancel(id)
    }

    fun setClickEvent(notiUpdate: PendingIntent) {
        builder!!.setContentIntent(notiUpdate)
    }

    companion object {
        private var notificationManager: NotificationManager? = null
        private val ids = ArrayList<Int>()
    }
}
