package com.project.how.adapter.item_touch_helper

import android.graphics.Canvas
import android.util.Log
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.project.how.interface_af.interface_ada.ItemMoveListener

class RecyclerViewItemTouchHelperCallback(
    private val moveListener: ItemMoveListener,
    private val nestedScrollView: NestedScrollView) : ItemTouchHelper.Callback() {
    private var fromPosition: Int = -1
    private var toPosition: Int = -1

    override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
        val dragFlags = ItemTouchHelper.UP or ItemTouchHelper.DOWN
        return makeMovementFlags(dragFlags, 0)
    }

    override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
        if(fromPosition == -1) fromPosition = viewHolder.adapterPosition
        toPosition = target.adapterPosition
        return true
    }

    override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
        super.onSelectedChanged(viewHolder, actionState)
        when(actionState) {
            ItemTouchHelper.ACTION_STATE_IDLE -> {
                if(fromPosition != -1 && toPosition != -1 && fromPosition != toPosition) {
                    moveListener.onItemMove(fromPosition, toPosition)
                }
                resetPositions()
            }
        }
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) { }

    override fun onChildDraw(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)

        val location = IntArray(2)
        val itemView = viewHolder.itemView
        itemView.getLocationOnScreen(location)
        val itemY = location[1]

        val displayMetrics = recyclerView.context.resources.displayMetrics
        val screenHeight = displayMetrics.heightPixels




        // NestedScrollView의 스크롤 처리
        if (itemY < 300) {
            // 상단에 도달했을 때 위로 스크롤
            nestedScrollView.smoothScrollBy(0, -20)
        } else if (itemY > screenHeight - 300) {
            // 하단에 도달했을 때 아래로 스크롤
            nestedScrollView.smoothScrollBy(0, 20)
        }
    }

    private fun resetPositions() {
        fromPosition = -1
        toPosition = -1
    }
}
