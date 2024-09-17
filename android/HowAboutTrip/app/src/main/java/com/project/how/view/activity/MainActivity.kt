package com.project.how.view.activity

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.project.how.R
import com.project.how.background.workmanager.AlarmWorkManager
import com.project.how.databinding.ActivityMainBinding
import com.project.how.view.activity.ai.AddAICalendarActivity
import com.project.how.view.fragment.main.CalendarFragment
import com.project.how.view.fragment.main.MypageFragment
import com.project.how.view.fragment.main.RecordFragment
import com.project.how.view.fragment.main.TicketFragment
import com.project.how.view_model.MemberViewModel
import com.project.how.view_model.SettingViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val settingViewModel : SettingViewModel by viewModels()

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.main = this
        binding.lifecycleOwner = this

        settingViewModel.init(this)

        scheduleWork()

        val menu = intent.getIntExtra(getString(R.string.menu_intent), 2)

        if (!allPermissionsGranted()){
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE)
        }

        supportFragmentManager.beginTransaction().add(R.id.fragment, CalendarFragment()).commitAllowingStateLoss()
        binding.menu.menu.getItem(menu).isEnabled = false
        binding.menu.selectedItemId = R.id.menu_calendar
        binding.menu.setOnItemSelectedListener {
            when(it.itemId){
                R.id.menu_ticket->{
                    supportFragmentManager.beginTransaction().replace(R.id.fragment, TicketFragment())
                        .commitAllowingStateLoss()
                }
                R.id.menu_calendar->{
                    supportFragmentManager.beginTransaction().replace(R.id.fragment, CalendarFragment())
                        .commitAllowingStateLoss()
                }
                R.id.menu_picture->{
                    supportFragmentManager.beginTransaction().replace(R.id.fragment, RecordFragment())
                        .commitAllowingStateLoss()
                }
                R.id.menu_mypage->{
                    supportFragmentManager.beginTransaction().replace(R.id.fragment, MypageFragment())
                        .commitAllowingStateLoss()
                }
            }
            true
        }
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            MemberViewModel.getInfo(applicationContext).collect{
            }



        }
    }

    fun moveAddAICalendar(){
        startActivity(Intent(this, AddAICalendarActivity::class.java))
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    private fun scheduleWork() {
        val workRequest = PeriodicWorkRequestBuilder<AlarmWorkManager>(12, TimeUnit.HOURS)
            .addTag("Alarm_work")
            .build()
        WorkManager.getInstance(this)
            .getWorkInfosByTagLiveData("Alarm_work")
            .observe(this) { workInfoList ->
                if (workInfoList.isNullOrEmpty()) {
                    WorkManager.getInstance(this).enqueue(workRequest)
                } else {
                    Log.d("WorkManager", "Work is already scheduled.")
                }
            }

    }

    companion object{
        const val TICKET = 1
        const val CALENDAR = 2
        const val PICTURE = 3
        const val MY_PAGE = 4

        const val REQUEST_CODE = 10
        @RequiresApi(Build.VERSION_CODES.TIRAMISU)
        val REQUIRED_PERMISSIONS = arrayOf(
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.READ_MEDIA_IMAGES,
            Manifest.permission.POST_NOTIFICATIONS,
            Manifest.permission.RECEIVE_BOOT_COMPLETED)
    }
}