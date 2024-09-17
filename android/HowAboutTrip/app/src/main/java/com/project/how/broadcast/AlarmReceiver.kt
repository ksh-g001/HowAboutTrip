package com.project.how.broadcast

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.project.how.R
import com.project.how.background.workmanager.AlarmWorkManager.Companion.REQ_DDAY_ALARM
import java.util.Calendar

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val dday = intent?.getLongExtra("dday", -1) ?: -1L
        val scheduleName = intent?.getStringExtra("scheduleName") ?: ""
        if (dday == -1L){
            Log.d("AlarmReceiver", "failed(dday is -1)")
            return
        }
        showNotify(context, dday, scheduleName)
    }

    private fun showNotify(context: Context, dday: Long, scheduleName: String){
        val title = if (dday == 0L) "D-Day" else "D-$dday"
        val text = if (dday == 0L) "오늘은 여행 예정 당일날 이네요. 즐거운 여행 다녀오세요!" else "${scheduleName} 여행 일정이 시작되기까지 ${dday}일 남았어요."

        Log.d("AlarmReceiver", "notify start $scheduleName : $dday\n$title\n$text")

        createAlarmChannel(context)

        val notification = NotificationCompat.Builder(context, "dday_channel")
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            NotificationManagerCompat.from(context).notify(REQ_DDAY_ALARM, notification)
        }
    }

    private fun createAlarmChannel(context: Context) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(REQ_DDAY_ALARM)
        val existingChannel = notificationManager.getNotificationChannel("dday_channel")
        if (existingChannel == null) {
            val channel = NotificationChannel(
                "dday_channel",
                "D-Day 알람",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "D-Day 알람을 위한 채널"
            }
            notificationManager.createNotificationChannel(channel)
        }
    }
}