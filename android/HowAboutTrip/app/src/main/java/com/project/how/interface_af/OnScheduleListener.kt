package com.project.how.interface_af

import com.project.how.data_class.recyclerview.schedule.DaysSchedule

interface OnScheduleListener {
    fun onDaysScheduleListener(schedule : DaysSchedule, position : Int)
}