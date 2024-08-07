package com.project.how.view.activity.calendar

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.MaterialDatePicker
import com.project.how.R
import com.project.how.adapter.recyclerview.schedule.CalendarListAdapter
import com.project.how.data_class.dto.country.GetCountryLocationResponse
import com.project.how.data_class.recyclerview.Schedule
import com.project.how.data_class.dto.schedule.GetScheduleListResponseElement
import com.project.how.databinding.ActivityCalendarListBinding
import com.project.how.interface_af.OnDesListener
import com.project.how.interface_af.OnYesOrNoListener
import com.project.how.view.dialog.YesOrNoDialog
import com.project.how.view.dialog.bottom_sheet_dialog.DesBottomSheetDialog
import com.project.how.view_model.CountryViewModel
import com.project.how.view_model.MemberViewModel
import com.project.how.view_model.ScheduleViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.TimeZone

class CalendarListActivity
    : AppCompatActivity(), CalendarListAdapter.OnCalendarListButtonClickListener, OnYesOrNoListener, OnDesListener {
    private lateinit var binding : ActivityCalendarListBinding
    private lateinit var adapter : CalendarListAdapter
    private val viewModel : ScheduleViewModel by viewModels()
    private val countryViewModel : CountryViewModel by viewModels()
    private var destination : String? = null
    private var departureDate : String? = null
    private var entranceDate : String? = null
    private var latLng : GetCountryLocationResponse? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_calendar_list)
        binding.list = this
        binding.lifecycleOwner = this

        viewModel.scheduleListLiveData.observe(this@CalendarListActivity){
            adapter = CalendarListAdapter(this@CalendarListActivity, it, this@CalendarListActivity)
            binding.calendarList.adapter = adapter
        }
    }

    override fun onStart() {
        super.onStart()

        lifecycleScope.launch {
            viewModel.getScheduleList(this@CalendarListActivity, MemberViewModel.tokensLiveData.value!!.accessToken)
        }
    }

    fun add() {
        showDesInput()
    }

    private fun showDesInput(){
        val des = DesBottomSheetDialog(this)
        des.show(supportFragmentManager, "DesBottomSheetDialog")
    }

    private fun showCalendar(){
        val constraints = CalendarConstraints.Builder()
            .setStart(Calendar.getInstance().timeInMillis)
            .build()

        val calendar = MaterialDatePicker.Builder.dateRangePicker()
            .setTheme(R.style.ThemeOverlay_App_DatePicker)
            .setCalendarConstraints(constraints)
            .setInputMode(MaterialDatePicker.INPUT_MODE_CALENDAR)
            .build()
        calendar.show(supportFragmentManager, "MaterialDatePicker")

        calendar.addOnPositiveButtonClickListener {
            val utc = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
            utc.timeInMillis = it.first
            val format = SimpleDateFormat("yyyy-MM-dd")
            val formatted = format.format(utc.time)
            utc.timeInMillis = it.second
            val formattedSecond = format.format(utc.time)
            entranceDate = formattedSecond
            departureDate = formatted
            moveCalendarEdit()
        }
    }

    private fun moveCalendarEdit(){
        val intent = Intent(this, CalendarEditActivity::class.java)
        val schedule = Schedule(
            destination!!,
            destination!!,
            departureDate!!,
            entranceDate!!,
            0,
            viewModel.getEmptyDaysSchedule(departureDate!!, entranceDate!!)
        )
        intent.putExtra(getString(R.string.type), CalendarEditActivity.NEW)
        intent.putExtra(getString(R.string.schedule), schedule)
        intent.putExtra(getString(R.string.server_calendar_latitude), latLng?.lat ?: 0.0)
        intent.putExtra(getString(R.string.server_calendar_longitude), latLng?.lng ?: 0.0)
        startActivity(intent)
        finish()
    }

    override fun onDeleteButtonClickListener(data : GetScheduleListResponseElement, position : Int) {
        val yesOrNoDialog = YesOrNoDialog(data.scheduleName, YesOrNoDialog.SCHEDULE_DELETE, position, this)
        Log.d("onDelete", "position : ${position}")
        yesOrNoDialog.show(supportFragmentManager, "YesOrNoDialog")
    }
    override fun onItemClickListener(id: Long, latitude : Double, longitude : Double) {
        Log.d("onCreate", "onItemClickerListener\nid : ${id}\nlatitude : ${latitude}\tlongitude : ${longitude}")
        val intent = Intent(this, CalendarActivity::class.java)
        intent.putExtra(getString(R.string.server_calendar_id), id)
        intent.putExtra(getString(R.string.server_calendar_latitude), latitude)
        intent.putExtra(getString(R.string.server_calendar_longitude), longitude)
        startActivity(intent)
        finish()
    }

    override fun onCheckListButtonClickListener(id: Long) {

    }

    override fun onShareButtonClickListener(id: Long) {

    }

    override fun onScheduleDeleteListener(position: Int) {
       lifecycleScope.launch {
           val data = adapter.getData(position)
           Log.d("onDelete", "onScheduleDeleteListener\nposition: $position\tid : ${data.id}\ttitle : ${data.scheduleName}")
           viewModel.deleteSchedule(this@CalendarListActivity, MemberViewModel.tokensLiveData.value!!.accessToken, data.id).collect{
                when(it){
                    ScheduleViewModel.SUCCESS -> { adapter.remove(position) }
                    ScheduleViewModel.OTHER_USER_SCHEDULE -> { Toast.makeText(this@CalendarListActivity, getString(R.string.other_user_schedule_delete_error), Toast.LENGTH_SHORT).show()}
                    ScheduleViewModel.NETWORK_FAILED -> { Toast.makeText(this@CalendarListActivity, getString(R.string.server_network_error), Toast.LENGTH_SHORT).show() }
                    ScheduleViewModel.NOT_EXIST_SCHEDULE -> {Toast.makeText(this@CalendarListActivity, getString(R.string.not_exist_schedule_delete_error), Toast.LENGTH_SHORT).show() }
                }
           }
       }
    }

    override fun onAddressCheckListener() {
        TODO("Not yet implemented")
    }

    override fun onKeepCheckListener() {
        TODO("Not yet implemented")
    }

    override fun onDesListener(des: String) {
        lifecycleScope.launch {
            countryViewModel.getCountryLocation(des).collect{ location ->
                location?.let {
                    destination = des
                    latLng = location
                    showCalendar()
                } ?: run {
                    countryViewModel.getCountryLocation(des).collect { newLocation ->
                        newLocation?.let {
                            destination = des
                            latLng = newLocation
                            showCalendar()
                        } ?: run {
                            Toast.makeText(this@CalendarListActivity, getString(R.string.server_network_error), Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }
}