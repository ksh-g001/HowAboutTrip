package com.project.how.adapter.recyclerview.record

import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.project.how.data_class.dto.recode.receipt.ReceiptDetailListItem
import com.project.how.databinding.BillDetailsItemBinding
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.roundToLong
import kotlin.math.truncate

class BillDetailsAdapter(
    private var details : MutableList<ReceiptDetailListItem>,
    private val currency : String,
    private val onPriceListener: OnPriceListener
) : RecyclerView.Adapter<BillDetailsAdapter.ViewHolder>() {
    private var editMode = true

    inner class ViewHolder(val binding : BillDetailsItemBinding) : RecyclerView.ViewHolder(binding.root){
        private var currentTextWatcher: TextWatcher? = null
        private var titleTextWatcher: TextWatcher? = null

        fun bind(data : ReceiptDetailListItem, position : Int){
            data.itemPrice = (data.itemPrice*100).roundToLong().toDouble()/100
            if (data.title.startsWith("Hint")){
                binding.title.setHint(data.title.replace("Hint", ""))
            }else{
                binding.title.setText(data.title)
            }

            if (editMode)
                ableEdit(data.count, binding)
            else
                unableEdit(binding)

            if (data.itemPrice == 0.0){
                binding.totalPrice.text = 0.toString()
            }else{
                val totalPrice = data.itemPrice * data.count.toDouble()
                val formatted = NumberFormat.getNumberInstance(Locale.getDefault()).format(totalPrice)
                Log.d("BillDetailsAdapter", "bind : ${getTotalPrice()}\ttotal price : $totalPrice		count : ${details[position].count}	item price : ${details[position].itemPrice}	title : ${details[position].title}")
                binding.totalPrice.text = formatted
            }
            binding.num.text = data.count.toString()
            val formatted = NumberFormat.getNumberInstance(Locale.getDefault()).format(data.itemPrice)
            binding.unitPrice.setText(formatted)
            binding.currency.text = currency
            binding.totalCurrency.text = currency

            onPriceListener.onTotalPriceListener(getTotalPrice())


            binding.minus.setOnClickListener {
                minus(position)
            }
            binding.plus.setOnClickListener {
                plus(position)
            }
            binding.delete.setOnClickListener {
                remove(position)
                onPriceListener.onTotalPriceListener(getTotalPrice())
            }

            currentTextWatcher = object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    if (!s.isNullOrBlank()) {
                        val cursorPosition = binding.unitPrice.selectionStart
                        val num = s.toString().replace(Regex("[^\\d.]"), "")
                        details[position].itemPrice = num.toDoubleOrNull() ?: 0.0
                        binding.unitPrice.setSelection(cursorPosition)
                        val totalPrice = details[position].itemPrice * details[position].count.toDouble()
                        val formatted = NumberFormat.getNumberInstance(Locale.getDefault()).format(totalPrice)
                        binding.totalPrice.text = formatted
                        Log.d("BillDetailsAdapter", "itemPriceEditText : ${getTotalPrice()}\ttotal price : $totalPrice\t\tcount : ${details[position].count}\titem price : ${details[position].itemPrice}\ttitle : ${details[position].title}")
                        onPriceListener.onTotalPriceListener(getTotalPrice())
                    }
                }
            }
            titleTextWatcher = object : TextWatcher{
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

                override fun afterTextChanged(s: Editable?) {
                    details[position].title = binding.title.text.toString()
                }

            }

            binding.unitPrice.addTextChangedListener(currentTextWatcher)
            binding.title.addTextChangedListener(titleTextWatcher)

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = ViewHolder(
        BillDetailsItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
    )

    override fun getItemCount(): Int = details.size

    override fun getItemViewType(position: Int): Int = position

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = details[position]
        holder.bind(data, position)
    }

    private fun plus(position: Int) {
        val data = details[position]
        val updatedData = data.copy(count = data.count + 1)
        details[position] = updatedData
        notifyItemChanged(position)
        Log.d("BillDetailsAdapter", "plus : ${getTotalPrice()}\tcount : ${details[position].count}\titem price : ${details[position].itemPrice}\ttitle : ${details[position].title}")
        onPriceListener.onTotalPriceListener(getTotalPrice())
    }

    private fun minus(position: Int) {
        val data = details[position]
        if (data.count > 1) {
            val updatedData = data.copy(count = data.count - 1)
            details[position] = updatedData
            notifyItemChanged(position)
            Log.d("BillDetailsAdapter", "minus : ${getTotalPrice()}\tcount : ${details[position].count}\titem price : ${details[position].itemPrice}\ttitle : ${details[position].title}")
            onPriceListener.onTotalPriceListener(getTotalPrice())
        }
    }

    private fun remove(position: Int){
        details.removeAt(position)
        notifyItemRemoved(position)
    }

    fun update(newData : List<ReceiptDetailListItem>){
        details = newData.toMutableList()
        notifyDataSetChanged()
    }

    fun add(){
        details.add(ReceiptDetailListItem(
            "Hint물품 이름",
            1L,
            1000.0
        ))
        notifyItemInserted(details.lastIndex)
    }

    fun startEdit(){
        editMode = true
        notifyDataSetChanged()
    }

    fun finishEdit(){
        editMode = false
        notifyDataSetChanged()
    }

    private fun unableEdit(binding : BillDetailsItemBinding) {
        binding.title.inputType = InputType.TYPE_NULL
        binding.unitPrice.inputType = InputType.TYPE_NULL
        binding.plus.visibility = View.INVISIBLE
        binding.minus.visibility = View.GONE
        binding.delete.visibility = View.GONE
    }

    private fun ableEdit(count : Long, binding: BillDetailsItemBinding){
        binding.title.inputType = InputType.TYPE_CLASS_TEXT
        binding.unitPrice.inputType = InputType.TYPE_NUMBER_FLAG_DECIMAL
        binding.plus.visibility = View.VISIBLE
        if (count == 1L){
            binding.minus.visibility = View.GONE
        }else{
            binding.minus.visibility = View.VISIBLE
        }
        binding.delete.visibility = View.VISIBLE
    }

    fun getTotalPrice() : Double {
        val totalPrice = details.sumOf { it.itemPrice * it.count.toDouble() }
        Log.d("BillDetailsAdapter", "getTotalPrice : ${totalPrice}")
        return totalPrice
    }

    fun getAllData() : List<ReceiptDetailListItem> = details.toList()

    fun getEditMode() : Boolean = editMode

    interface OnPriceListener{
        fun onTotalPriceListener(total : Double)
    }
}
