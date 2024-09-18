package com.project.how.view.fragment.mypage.setting

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import androidx.work.WorkManager
import com.project.how.R
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

@AndroidEntryPoint
class SettingsMainFragment : PreferenceFragmentCompat(), OnDialogListener, OnYesOrNoListener {
    private val settingViewModel: SettingViewModel by viewModels()
    private val bookingViewModel: BookingViewModel by viewModels()
    private lateinit var alarmPreference: SwitchPreferenceCompat
    private lateinit var locationPreference: SwitchPreferenceCompat
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        settingViewModel.settingLiveData.observe(viewLifecycleOwner){
            if(initNeed){
                Log.d("SettingMainFragment", "settingLiveData : $it")
                alarmPreference.isChecked = it.alarmSettingStatus
                locationPreference.isChecked = it.locationSettingStatus
                initNeed = false
            }
        }
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
                settingViewModel.setAlarmOn(requireContext())
            } else {
                Log.d("SettingMainFragment", "alarm on")
                settingViewModel.setAlarmOff(requireContext())
            }
            true
        }

        locationPreference.setOnPreferenceChangeListener{ preference, newValue ->
            if (locationPreference.isChecked) {
                Log.d("SettingMainFragment", "location off")
                settingViewModel.setLocationOn(requireContext())
            }else{
                Log.d("SettingMainFragment", "location on")
                settingViewModel.setLocationOff(requireContext())
            }
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