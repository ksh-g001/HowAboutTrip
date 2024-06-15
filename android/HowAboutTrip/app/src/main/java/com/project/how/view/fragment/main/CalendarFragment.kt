package com.project.how.view.fragment.main

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.tabs.TabLayoutMediator
import com.project.how.BuildConfig
import com.project.how.R
import com.project.how.adapter.recyclerview.viewpager.EventViewPagerAdapter
import com.project.how.adapter.recyclerview.RecentAddedCalendarsAdapter
import com.project.how.data_class.recyclerview.EventViewPager
import com.project.how.data_class.recyclerview.RecentAddedCalendar
import com.project.how.data_class.recyclerview.Schedule
import com.project.how.data_class.dto.GetCountryLocationResponse
import com.project.how.data_class.dto.GetFastestSchedulesResponse
import com.project.how.databinding.FragmentCalendarBinding
import com.project.how.interface_af.OnDesListener
import com.project.how.view.activity.ai.AddAICalendarActivity
import com.project.how.view.activity.calendar.CalendarActivity
import com.project.how.view.activity.calendar.CalendarEditActivity
import com.project.how.view.activity.calendar.CalendarListActivity
import com.project.how.view.dialog.bottom_sheet_dialog.DesBottomSheetDialog
import com.project.how.view_model.MemberViewModel
import com.project.how.view_model.ScheduleViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.TimeZone

class CalendarFragment : Fragment(), OnDesListener {
    private var _binding : FragmentCalendarBinding? = null
    private val binding : FragmentCalendarBinding
        get() = _binding!!
    private val scheduleViewModel : ScheduleViewModel by viewModels()
    private var nearSchedule : GetFastestSchedulesResponse? = null
    private val event = mutableListOf<EventViewPager>()
    private val recentAddedCalendar = mutableListOf<RecentAddedCalendar>()
    private lateinit var eventAdapter : EventViewPagerAdapter
    private lateinit var recentAddedCalendarAdapter: RecentAddedCalendarsAdapter
    private var destination : String? = null
    private var departureDate : String? = null
    private var entranceDate : String? = null
    private var latLng : GetCountryLocationResponse? = null
    private lateinit var autoScrollJob : Job
    private var viewPagerPosition = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        recentAddedCalendarAdapter = RecentAddedCalendarsAdapter(recentAddedCalendar)
        for(i in 0..5){
            event.add(EventViewPager("test", "일정 생성을\n해보세요$i", null))
        }
        eventAdapter = EventViewPagerAdapter(event)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = DataBindingUtil.inflate(inflater, R.layout.fragment_calendar, container, false)
        binding.calendar = this
        binding.lifecycleOwner = viewLifecycleOwner
        scheduleViewModel.nearScheduleDayLiveData.observe(viewLifecycleOwner){near->
            nearSchedule = near
            val dDay = resources.getString(R.string.d_day) + if (nearSchedule!!.dday.toInt() == 0) getString(R.string.d_day_zero) else nearSchedule!!.dday.toString()
            Glide.with(binding.root)
                .load(nearSchedule!!.imageUrl)
                .error(BuildConfig.ERROR_IMAGE_URl)
                .into(binding.scheduleImage)
            binding.dDay.text = dDay
            binding.scheduleTitle.text = nearSchedule!!.scheduleName
        }
        autoScrollJobCreate()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        scheduleViewModel.getFastestSchedules(requireContext(), MemberViewModel.tokensLiveData.value!!.accessToken)
        binding.viewPager2.overScrollMode = RecyclerView.OVER_SCROLL_NEVER

        binding.viewPager2.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback(){
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                viewPagerPosition = position
            }

            override fun onPageScrollStateChanged(state: Int) {
                super.onPageScrollStateChanged(state)
                when (state){
                    ViewPager2.SCROLL_STATE_IDLE -> {if (!autoScrollJob.isActive) autoScrollJobCreate()}
                    ViewPager2.SCROLL_STATE_DRAGGING -> {
                        autoScrollJob.cancel()
                    }
                }
            }
        })

    }

    override fun onStart() {
        super.onStart()
        binding.recentAddedCalendar.adapter = recentAddedCalendarAdapter
        binding.viewPager2.adapter = eventAdapter
        TabLayoutMediator(binding.indicator, binding.viewPager2) { _, _ -> }.attach()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun autoScrollJobCreate() {
        autoScrollJob = lifecycleScope.launch{

        }
    }

    fun moveNearScheduleCalendar(){
        if (nearSchedule != null){
            val intent = Intent(requireContext(), CalendarActivity::class.java)
            intent.putExtra(getString(R.string.server_calendar_id), nearSchedule!!.id)
            startActivity(intent)
        }
    }

    fun add() {
        showDesInput()
    }

    private fun showDesInput(){
        val des = DesBottomSheetDialog(this)
        des.show(childFragmentManager, "DesBottomSheetDialog")
    }

    fun moveAddAICalendar(){
        startActivity(Intent(activity, AddAICalendarActivity::class.java))
    }

    fun moveCalendarList(){
        startActivity(Intent(activity, CalendarListActivity::class.java))
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
        calendar.show(childFragmentManager, "MaterialDatePicker")

        calendar.addOnPositiveButtonClickListener {
            val utc = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
            utc.timeInMillis = it.first
            val format = SimpleDateFormat("yyyy-MM-dd")
            val formatted = format.format(utc.time)
            utc.timeInMillis = it.second
            val formattedSecond = format.format(utc.time)
            entranceDate = formattedSecond
            departureDate = formatted
            val intent = Intent(activity, CalendarEditActivity::class.java)
            val schedule = Schedule(
                destination!!,
                destination!!,
                departureDate!!,
                entranceDate!!,
                0,
                scheduleViewModel.getEmptyDaysSchedule(departureDate!!, entranceDate!!)
            )
            intent.putExtra(getString(R.string.type), CalendarEditActivity.NEW)
            intent.putExtra(getString(R.string.schedule), schedule)
            intent.putExtra(getString(R.string.server_calendar_latitude), latLng!!.lat)
            intent.putExtra(getString(R.string.server_calendar_longitude), latLng!!.lng)
            startActivity(intent)
        }
    }

    override fun onDesListener(des: String) {
        lifecycleScope.launch {
            scheduleViewModel.getCountryLocation(des).collect{ location ->
                location?.let {
                    destination = des
                    latLng = location
                    showCalendar()
                } ?: run {
                    scheduleViewModel.getCountryLocation(des).collect { newLocation ->
                        newLocation?.let {
                            destination = des
                            latLng = newLocation
                            showCalendar()
                        } ?: run {
                            Toast.makeText(activity, getString(R.string.server_network_error), Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }
}