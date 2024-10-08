package com.project.how.adapter.recyclerview.record.receipt

import android.content.Context
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.project.how.R
import com.project.how.data_class.dto.recode.receipt.ChangeOrderReceiptRequest
import com.project.how.data_class.dto.recode.receipt.ReceiptList
import com.project.how.data_class.dto.recode.receipt.StoreType
import com.project.how.databinding.BillDaysDetailItemBinding
import com.project.how.interface_af.interface_ada.ItemDragListener
import com.project.how.interface_af.interface_ada.ItemMoveListener

class BillDaysAdapter(
    private var billDetails : MutableList<ReceiptList>,
    private val context : Context,
    private var currency : String,
    private val onItemClickListener: OnItemClickListener,
) : RecyclerView.Adapter<BillDaysAdapter.ViewHolder>(), PopupMenu.OnMenuItemClickListener,
    ItemMoveListener {
    private var currentPosition = -1
    private var currentData : ReceiptList? = null
    private var orderChange = false
    private var onItemDragListener: ItemDragListener<ChangeOrderReceiptRequest>? = null

    inner class ViewHolder(val binding : BillDaysDetailItemBinding) : RecyclerView.ViewHolder(binding.root){
        fun bind(data : ReceiptList, position: Int){
            binding.date.text = "${data.purchaseDate} 구매"
            binding.title.text = data.storeName
            binding.budget.text = "${data.price} $currency"

            if (position == 0)
                binding.topDottedLine.visibility = View.GONE
            else
                binding.topDottedLine.visibility = View.VISIBLE

            when(data.storeType){
                StoreType.AIRPLANE ->{
                    binding.number.visibility = View.GONE
                    binding.numberBackground.setImageResource(R.drawable.icon_ticket_bold)
                }
                StoreType.HOTEL->{
                    binding.number.text = "${position+1}"
                    binding.number.visibility = View.VISIBLE
                    binding.numberBackground.setImageResource(R.drawable.black_oval)

                }
                StoreType.PLACE ->{
                    binding.number.text = "${position+1}"
                    binding.number.visibility = View.VISIBLE
                    binding.numberBackground.setImageResource(R.drawable.black_oval)
                }
            }

            itemView.setOnClickListener {
                onItemClickListener.onItemClickListener(data, position)
            }

            binding.more.setOnClickListener {
                currentPosition = position
                currentData = data
                PopupMenu(context, binding.more).apply {
                    inflate(R.menu.bill_edit_more)
                    setOnMenuItemClickListener(this@BillDaysAdapter)
                    show()
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = ViewHolder(
        BillDaysDetailItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
    )

    override fun onMenuItemClick(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            R.id.menu_bill_order_change -> {
                onItemClickListener.onMoreMenuOrderChangeClickListener(currentPosition)
            }
            R.id.menu_bill_date_change -> {
                onItemClickListener.onMoreMenuDateChangeClickListener(currentData!!, currentPosition)
            }
            R.id.menu_bill_delete -> {
                onItemClickListener.onMoreMenuDeleteClickListener(currentData!!.receiptId, currentPosition)
            }
            else ->{
                Toast.makeText(context, context.getString(R.string.non_exist_menu_warning), Toast.LENGTH_SHORT).show()
            }
        }
        return false
    }

    override fun getItemCount(): Int = billDetails.size

    override fun getItemViewType(position: Int): Int = position

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = billDetails[position]

        holder.bind(data, position)
    }

    fun update(newData : MutableList<ReceiptList>, currency: String){
        billDetails = newData
        this.currency = currency
        notifyDataSetChanged()
    }

    fun update(newData : MutableList<ReceiptList>){
        billDetails = newData
        notifyDataSetChanged()
    }

    fun updateDate(receiptId: Long, newDate : String){
        orderChange = true
        billDetails.first { it.receiptId == receiptId }.purchaseDate = newDate
        billDetails.remove(billDetails.first { it.receiptId == receiptId })
        notifyDataSetChanged()
    }

    fun add(newData : ReceiptList){
        billDetails.add(newData)
        notifyItemInserted(billDetails.lastIndex)
    }

    fun remove(position: Int) : Double {
        val removedPrice = billDetails[position].price
        billDetails.removeAt(position)
        notifyDataSetChanged()
        return removedPrice
    }

    fun swap(fromPosition : Int, toPosition : Int){
        orderChange = true
        val temp = billDetails[fromPosition]
        billDetails[fromPosition] = billDetails[toPosition]
        billDetails[toPosition] = temp
        notifyItemMoved(fromPosition, toPosition)
    }

    fun getData(receiptId : Long) = billDetails.first { it.receiptId == receiptId }
    fun getData(position: Int) = billDetails[position]
    fun getDataAllName() = billDetails.map { it.storeName }
    fun getChangeOrderReceipt() : List<ChangeOrderReceiptRequest> = emptyList()

    fun addItemDragListener(listener : ItemDragListener<ChangeOrderReceiptRequest>){
        onItemDragListener = listener
    }

    interface OnItemClickListener{
        fun onItemClickListener(data : ReceiptList, position: Int)
        fun onMoreMenuDateChangeClickListener(data : ReceiptList, position: Int)
        fun onMoreMenuOrderChangeClickListener(position: Int)
        fun onMoreMenuDeleteClickListener(id: Long, position: Int)
    }

    override fun onItemMove(fromPosition: Int, toPosition: Int): Boolean {
        if (fromPosition < 0 || fromPosition >= billDetails.size ||
            toPosition < 0 || toPosition >= billDetails.size) {
            return false
        }

        swap(fromPosition, toPosition)
        onDropAdapter(billDetails[fromPosition].orderNum, billDetails[toPosition].orderNum)

        return true
    }

    override fun onDropAdapter(fromPosition: Long, toPosition: Long): Boolean {
        onItemDragListener?.onDropActivity(getChangeOrderReceipt(), fromPosition, toPosition)
        return true
    }
}