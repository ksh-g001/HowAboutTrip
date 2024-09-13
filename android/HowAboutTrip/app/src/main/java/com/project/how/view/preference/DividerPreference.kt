package com.project.how.view.preference

import android.content.Context
import android.util.AttributeSet
import androidx.preference.Preference
import com.project.how.R


class DividerPreference(context : Context, attr: AttributeSet?) : Preference(context, attr) {
    init {
        layoutResource = R.layout.preference_divider
    }
}