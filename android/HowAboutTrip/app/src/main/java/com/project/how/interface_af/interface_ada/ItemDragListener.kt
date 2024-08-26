package com.project.how.interface_af.interface_ada

interface ItemDragListener<T> {
    fun onDropActivity(changeList: List<T>, fromPosition: Long, toPosition: Long)
}