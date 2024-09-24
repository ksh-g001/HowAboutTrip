package com.project.how.view.fragment.mypage.setting

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.project.how.R
import com.project.how.background.workmanager.AlarmWorkManager
import com.project.how.background.workmanager.AlarmWorkManager.Companion.REQ_DDAY_ALARM
import com.project.how.broadcast.AlarmReceiver
import com.project.how.interface_af.OnDialogListener
import com.project.how.interface_af.OnYesOrNoListener
import com.project.how.view.activity.LoginActivity
import com.project.how.view.dialog.WarningDialog
import com.project.how.view.dialog.YesOrNoDialog
import com.project.how.view.preference.DividerPreference
import com.project.how.view_model.BookingViewModel
import com.project.how.view_model.MemberViewModel
import com.project.how.view_model.SettingViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.concurrent.TimeUnit
import kotlin.math.truncate

@AndroidEntryPoint
class SettingsMainFragment : PreferenceFragmentCompat(), OnDialogListener, OnYesOrNoListener {
    private val settingViewModel: SettingViewModel by viewModels()
    private val bookingViewModel: BookingViewModel by viewModels()
    private lateinit var alarmPreference: SwitchPreferenceCompat
    private lateinit var locationPreference: Preference
    private lateinit var privacyPreference: Preference
    private lateinit var openSourcePreference: Preference
    private lateinit var clearPreference: Preference
    private lateinit var logoutPreference: Preference
    private lateinit var deleteIdPreference: Preference
    private var initNeed = true

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)

        val divider = DividerPreference(requireContext(), null)
        preferenceScreen.addPreference(divider)

        lifecycleScope.launch {
            init()
        }


    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        settingViewModel.settingLiveData.observe(viewLifecycleOwner){
            Log.d("SettingMainFragment", "init : ${initNeed}\nsettingLiveData : $it")
            if(initNeed){
                alarmPreference.isChecked = it.alarmSettingStatus
                initNeed = false
            }
        }

        WorkManager.getInstance(requireContext()).getWorkInfosByTagLiveData(getString(R.string.alarm_workmanager)).observe(viewLifecycleOwner) { workInfos ->
            // 작업 상태를 확인
            if (workInfos.isNullOrEmpty()) {
                Log.d("WorkManager", "No work found with the specified tag.")
            } else {
                for (workInfo in workInfos) {
                    Log.d("WorkManager", "Work ID: ${workInfo.id}, State: ${workInfo.state}")
                }
            }
        }

        return super.onCreateView(inflater, container, savedInstanceState)
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    private fun init(){
        try {
            alarmPreference = findPreference(getString(R.string.preference_key_alarm))!!
            locationPreference = findPreference(getString(R.string.preference_key_location))!!
            privacyPreference = findPreference(getString(R.string.preference_key_privacy))!!
            openSourcePreference = findPreference(getString(R.string.preference_key_license))!!
            clearPreference = findPreference(getString(R.string.preference_key_data_delete))!!
            logoutPreference = findPreference(getString(R.string.preference_key_logout))!!
            deleteIdPreference = findPreference(getString(R.string.preference_key_delete_id))!!

            handlePreferenceClick()


        }catch (e : Exception){
            Log.e("SettingMainFragment", "error : ${e.message}")
            error()
        }
    }

    private fun handlePreferenceClick(){
        alarmPreference.setOnPreferenceChangeListener { preference, newValue ->
            if (alarmPreference.isChecked) {
                Log.d("SettingMainFragment", "alarm off")
                alarmCancel()
                settingViewModel.setAlarmOff(requireContext())
            } else {
                Log.d("SettingMainFragment", "alarm on")
                setAlarm()
                settingViewModel.setAlarmOn(requireContext())
            }
            true
        }

        locationPreference.setOnPreferenceClickListener {
            openAppSettings()
            true
        }

        clearPreference.setOnPreferenceClickListener {
            YesOrNoDialog(getString(R.string.delete_check_message),
                YesOrNoDialog.DELETE_CHECK,
                0,
                this@SettingsMainFragment)
                .show(childFragmentManager, "YesOrNoDialog")
            true
        }

        logoutPreference.setOnPreferenceClickListener {
            runBlocking {
                MemberViewModel.logout(requireContext())
                WorkManager.getInstance(requireContext()).cancelAllWorkByTag(getString(R.string.alarm_workmanager))
            }
            val intent = Intent(requireContext(), LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
            true
        }
    }

    private fun setAlarm(){
        val workRequest = PeriodicWorkRequestBuilder<AlarmWorkManager>(4, TimeUnit.HOURS)
            .addTag(getString(R.string.alarm_workmanager))
            .build()
        WorkManager.getInstance(requireContext()).enqueueUniquePeriodicWork(
            getString(R.string.alarm_workmanager),
            ExistingPeriodicWorkPolicy.UPDATE,
            workRequest
        )
    }

    private fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", requireContext().packageName, null)
        intent.data = uri
        startActivity(intent)
    }

    private fun alarmCancel(){
        WorkManager.getInstance(requireContext()).cancelAllWorkByTag(getString(R.string.alarm_workmanager))
        val intent = Intent(requireContext(), AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(requireContext(),
            REQ_DDAY_ALARM,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(pendingIntent)
    }

    private fun error(){
        WarningDialog(getString(R.string.setting_ui_error), this).show(childFragmentManager, "WarningDialog")
    }

    override fun onDismissListener() {
        activity?.onBackPressed()
    }

    override fun onDeleteListener() {
        bookingViewModel.deleteAll()
    }

    override fun onScheduleDeleteListener(position: Int) {
        TODO("Not yet implemented")
    }

    override fun onKeepCheckListener() {
        TODO("Not yet implemented")
    }

    override fun onCameraListener(answer: Boolean) {
        TODO("Not yet implemented")
    }

    override fun onOcrListener(answer: Boolean) {
        TODO("Not yet implemented")
    }
}