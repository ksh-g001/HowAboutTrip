package com.project.how.interface_af

interface OnYesOrNoListener {
    fun onDeleteListener()
    fun onScheduleDeleteListener(position: Int)
    fun onKeepCheckListener()
    fun onCameraListener(answer : Boolean)
    fun onOcrListener(answer : Boolean)
}