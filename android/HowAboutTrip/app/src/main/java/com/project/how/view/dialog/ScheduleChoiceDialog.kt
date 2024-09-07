package com.project.how.view.dialog

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.RadioButton
import androidx.core.view.isEmpty
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import com.project.how.R
import com.project.how.data_class.dto.schedule.GetScheduleListResponse
import com.project.how.databinding.DialogScheduleChoiceBinding

class ScheduleChoiceDialog(
    private val schedules : GetScheduleListResponse,
    private val onDialogListener: OnDialogListener
) : DialogFragment() {
    private var _binding : DialogScheduleChoiceBinding? = null
    private val binding : DialogScheduleChoiceBinding
        get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = DataBindingUtil.inflate(inflater, R.layout.dialog_schedule_choice, container,false)
        binding.dialog = this
        binding.lifecycleOwner = viewLifecycleOwner
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog?.window?.requestFeature(Window.FEATURE_NO_TITLE)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if(!binding.radio.isEmpty()){
            binding.radio.removeAllViews()
        }
        setRadioButton()
    }

    private fun setRadioButton(){
        for(d in schedules){
            val rdbtn = RadioButton(requireContext()).apply {
                text = "${d.scheduleName}\n(${getString(R.string.date_text, d.startDate, d.endDate)})"
                id = d.id.toInt()
                if (d.id == schedules[0].id)
                    isChecked = true
            }
            binding.radio.addView(rdbtn)
        }
    }

    private fun getCheckedRadioButtonId() : Long{
        val checked = binding.radio.checkedRadioButtonId
        if(checked == -1){
            return schedules[0].id
        }
        return checked.toLong()
    }

    fun cancel(){
        dismiss()
    }

    fun select(){
        onDialogListener.onSelectListener(getCheckedRadioButtonId())
    }

    interface OnDialogListener{
        fun onSelectListener(id : Long)
    }
}