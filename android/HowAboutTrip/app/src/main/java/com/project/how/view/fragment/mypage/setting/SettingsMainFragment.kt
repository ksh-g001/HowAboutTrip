package com.project.how.view.fragment.mypage.setting

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import com.project.how.R
import com.project.how.view.preference.DividerPreference



class SettingsMainFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)

        val divider = DividerPreference(requireContext(), null)
        preferenceScreen.addPreference(divider)

        

    }
}