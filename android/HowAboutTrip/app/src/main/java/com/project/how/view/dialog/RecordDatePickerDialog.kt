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
import com.project.how.databinding.DialogRecordDatepickerBinding

class RecordDatePickerDialog(
    private val receiptId : Long,
    private val currentDate : String,
    private val dates : List<String>,
    private val onDateSetListener: OnDateSetListener
) : DialogFragment() {
    private var _binding : DialogRecordDatepickerBinding? = null
    private val binding : DialogRecordDatepickerBinding
        get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DataBindingUtil.inflate(inflater, R.layout.dialog_schedule_datepicker, container,false)
        binding.date = this
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun cancel(){
        dismiss()
    }

    fun save(){
        val date = getCheckedRadioButtonDate()
        onDateSetListener.onDateSetListener(receiptId, date)
        dismiss()
    }

    private fun setRadioButton(){
        for(d in dates){
            val rdbtn = RadioButton(requireContext()).apply {
                text = d
                id = View.generateViewId()
                if (d == currentDate)
                    isChecked = true
            }
            binding.radio.addView(rdbtn)
        }
    }

    private fun getCheckedRadioButtonDate() : String{
        val checked = binding.radio.checkedRadioButtonId
        if(checked == -1){
            return currentDate
        }

        val radioBtn = binding.radio.findViewById<RadioButton>(checked)
        return radioBtn.text.toString()
    }



    interface OnDateSetListener{
        fun onDateSetListener(receiptId: Long, newDate : String)
    }
}