package com.project.how.background.workmanager

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.project.how.broadcast.AlarmReceiver
import com.project.how.data_class.dto.schedule.GetFastestSchedulesResponse
import com.project.how.network.client.ScheduleRetrofit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.awaitResponse
import java.util.Calendar
import javax.inject.Inject
import kotlin.random.Random

@HiltWorker
class AlarmWorkManager @Inject constructor(appContext: Context, workerParams: WorkerParameters):
    CoroutineWorker(appContext, workerParams) {
        private var dday = -1L
        private var scheduleName = ""
        private var nCheck = false
    override suspend fun doWork(): Result = coroutineScope{
        if (isStopped) {
            Log.d("AlarmWorkManager", "Work was stopped before completion.")
            alarmCancel()
            return@coroutineScope Result.failure()
        }

        try {
            getDday()
        }catch (e : Exception){
            Log.e("AlarmWorkManager", "getDday() Error\nerror : ${e.message}")
            nCheck = false
        }

        Log.d("AlarmWorkManager", "D-Day: $dday, Schedule Name: $scheduleName, nCheck: $nCheck")
        if (nCheck) Result.success() else Result.failure()
    }

    private suspend fun getDday() {
        try {
            val response = withContext(Dispatchers.IO) {
                ScheduleRetrofit.getApiService()!!.getFastestSchedule().awaitResponse()
            }

            if (response.isSuccessful) {
                response.body()?.let {
                    dday = it.dday.toLong()
                    scheduleName = it.scheduleName
                    Log.d("AlarmWorkManager", "getDday() Success\nD-Day: $dday, Schedule Name: $scheduleName")
                    if (dday < 15) {
                        setDdayAlarm(dday, scheduleName)
                        nCheck = true
                    }
                } ?: run {
                    Log.e("AlarmWorkManager", "Response body is null(${response.code()})")
                    nCheck = false
                }
            }
        } catch (e: Exception) {
            Log.e("AlarmWorkManager", "getDday() Error: ${e.message}")
            nCheck = false
        }
    }

    @SuppressLint("ScheduleExactAlarm")
    private fun setDdayAlarm(dday : Long, scheduleName : String){
        val alarmManager = applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(applicationContext, AlarmReceiver::class.java)

        val pendingIntent = PendingIntent.getBroadcast(applicationContext,
            REQ_DDAY_ALARM,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        Log.d("AlarmWorkManager", "PendingIntent created: $pendingIntent")

        val random = Random.nextInt(45)

        val calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, 10)
            set(Calendar.MINUTE, random)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)

            // 오늘의 오전 10시가 이미 지났다면 내일 오전 10시로 설정
            if (timeInMillis < System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            pendingIntent)

        Log.d("AlarmWorkManager'", "Alarm set for: ${calendar.timeInMillis} with PendingIntent: $pendingIntent")
    }

    private fun alarmCancel(){
        val alarmManager = applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(applicationContext, AlarmReceiver::class.java).apply {
            putExtra("EXTRA_ALARM_COUNT", 1)
            putExtra("dday", dday)
            putExtra("scheduleName", scheduleName)
        }

        val pendingIntent = PendingIntent.getBroadcast(applicationContext,
            REQ_DDAY_ALARM,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        alarmManager.cancel(pendingIntent)
    }

    companion object{
        const val REQ_DDAY_ALARM = 1001
    }
}