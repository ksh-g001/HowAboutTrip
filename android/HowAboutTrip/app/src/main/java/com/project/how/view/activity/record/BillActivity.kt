package com.project.how.view.activity.record

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import com.google.android.material.tabs.TabLayout
import com.project.how.R
import com.project.how.adapter.item_touch_helper.RecyclerViewItemTouchHelperCallback
import com.project.how.adapter.recyclerview.record.BillDaysAdapter
import com.project.how.data_class.dto.recode.receipt.ChangeOrderReceiptRequest
import com.project.how.data_class.dto.recode.receipt.GetReceiptListResponse
import com.project.how.data_class.dto.recode.receipt.ReceiptList
import com.project.how.databinding.ActivityBillBinding
import com.project.how.interface_af.OnOrderChangeListener
import com.project.how.interface_af.interface_ada.ItemDragListener
import com.project.how.view.dialog.AiScheduleDialog
import com.project.how.view.dialog.OrderChangeDialog
import com.project.how.view.dialog.RecordDatePickerDialog
import com.project.how.view.dp.DpPxChanger
import com.project.how.view_model.RecordViewModel
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale


class BillActivity :
    AppCompatActivity(),
    BillDaysAdapter.OnItemClickListener,
    RecordDatePickerDialog.OnDateSetListener,
    OnOrderChangeListener{
    private lateinit var binding: ActivityBillBinding
    private val recordViewModel: RecordViewModel by viewModels()
    private lateinit var adapter: BillDaysAdapter
    private var id = 0L
    private var currentTab = 0
    private lateinit var currentDate: String
    private lateinit var currency: String
    private var totalPrice = 0.0
    private lateinit var startDate: String
    private lateinit var endDate: String
    private lateinit var receiptList: MutableList<ReceiptList>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_bill)
        binding.bill = this
        binding.lifecycleOwner = this
        adapter = BillDaysAdapter(mutableListOf<ReceiptList>(), this, "원", this)
        binding.dayBills.adapter = adapter

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                lifecycleScope.launch {
                    changeOrder(true)
                }
            }
        })

        val mCallback = RecyclerViewItemTouchHelperCallback(adapter, binding.scrollView)
        val mItemTouchHelper = ItemTouchHelper(mCallback)
        mItemTouchHelper.attachToRecyclerView(binding.dayBills)

        adapter.addItemDragListener(object : ItemDragListener<ChangeOrderReceiptRequest> {
            override fun onDropActivity(
                changeList: List<ChangeOrderReceiptRequest>,
                fromPosition: Long,
                toPosition: Long
            ) {
                swapOrderNum(fromPosition, toPosition)
            }
        })

        binding.swipeRefreshLayout.setOnRefreshListener { // 새로 고침을 위한 작업 수행
            refresh()
        }

        recordViewModel.currentReceiptListLiveData.observe(this) { data ->
            lifecycleScope.launch {
                Log.d("BillActivity", "currentTab : $currentTab")
                receiptList = data.receiptList.toMutableList()
                currentDate = recordViewModel.getCurrentDate(data.startDate, currentTab)
                currency = data.currencyName
                totalPrice = data.totalReceiptsPrice
                startDate = data.startDate
                endDate = data.endDate
                Log.d(
                    "BillActivity",
                    "size : ${
                        data.receiptList.filter { it.purchaseDate == currentDate }
                            .toMutableList().size
                    }\ncurrentDate : $currentDate \n currentTab : $currentTab"
                )
                adapter.update(receiptList.filter { it.purchaseDate == currentDate }
                    .toMutableList(), currency)
                binding.daysTab.getTabAt(currentTab)?.select()
                setTabSeletedListener()
                setUI(data)
                binding.swipeRefreshLayout.isRefreshing = false

            }
        }

        init()
    }

    fun add() {
        val intent = Intent(this, BillInputActivity::class.java)
        intent.putExtra(getString(R.string.server_calendar_id), id)
        intent.putExtra(getString(R.string.current_tab), currentTab)
        intent.putExtra(getString(R.string.current_date), currentDate)
        intent.putExtra(getString(R.string.currency), currency)
        startActivity(intent)
        finish()
    }

    private fun refresh() {
        lifecycleScope.launch {
            Log.d("BillActivity", "refresh start")
            changeOrder()
        }
    }

    private suspend fun changeOrder(backPressed : Boolean = false) {
        recordViewModel.changeOrderReceipt(id, getChangeOrderReceipt()).collect { result ->
            handleOrderChangeResult(result, backPressed)
        }
        Log.d("BillActivity", "changeOrder is finish")
    }

    private fun handleOrderChangeResult(result: Int, backPressed: Boolean) {
        val messageResId = when (result) {
            RecordViewModel.SUCCESS -> null
            RecordViewModel.NETWORK_FAILED -> R.string.server_network_error
            RecordViewModel.NOT_ALL -> R.string.app_error
            RecordViewModel.DUPLICATE_ORDER_NUM -> R.string.app_error
            RecordViewModel.UNKNOWN -> R.string.unknown_error
            else -> R.string.unknown_error
        }
        Log.d("BillActivity", "handleOrderChangeResult : $result")

        messageResId?.let {
            Toast.makeText(this, getString(it, result.toString()), Toast.LENGTH_SHORT).show()
            binding.swipeRefreshLayout.isRefreshing = false
        } ?: if (backPressed) finish() else recordViewModel.getReceiptList(id)
    }

    private fun init() {
        lifecycleScope.launch {
            Log.d("BillActivity", "init start")
            val id = intent.getLongExtra(getString(R.string.server_calendar_id), -1)
            currentTab = intent.getIntExtra(getString(R.string.current_tab), 0)
            this@BillActivity.id = id.toLong()
            recordViewModel.getReceiptList(this@BillActivity.id)
        }
    }

    private fun setUI(data: GetReceiptListResponse) {
        binding.title.text = data.scheduleName
        binding.date.text = "${data.startDate} - ${data.endDate}"
        binding.cost.text =
            NumberFormat.getNumberInstance(Locale.getDefault()).format(data.totalReceiptsPrice)
        binding.costUnit.text = " ${data.currencyName}"
        binding.daysTitle.text = getString(
            R.string.days_title,
            (currentTab + 1).toString(),
            recordViewModel.getDaysTitle(data.startDate, currentTab)
        )

        if (binding.daysTab.tabCount == 0) {
            setDaysTab(data.startDate, data.endDate)
            setDaysTabItemMargin()
        }
    }

    private fun setTabSeletedListener() {
        if (binding.daysTab.tabCount == 0) {
            binding.daysTab.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab?) {
                    lifecycleScope.launch {
                        Log.d(
                            "BillActivity",
                            "onTabSelected : binding.daysTab.selectedTabPosition : ${binding.daysTab.selectedTabPosition}"
                        )
                        currentTab = binding.daysTab.selectedTabPosition
                        binding.daysTitle.text = getString(
                            R.string.days_title,
                            (currentTab + 1).toString(),
                            recordViewModel.getDaysTitle(startDate, currentTab)
                        )
                        currentDate = recordViewModel.getCurrentDate(startDate, currentTab)
                        adapter.update(receiptList.filter { it.purchaseDate == currentDate }
                            .toMutableList())
                    }
                }

                override fun onTabUnselected(tab: TabLayout.Tab?) {}

                override fun onTabReselected(tab: TabLayout.Tab?) {}
            })
        }
    }

    private fun setDaysTab(startDate: String, endDate: String) {
        val sd = LocalDate.parse(startDate, DateTimeFormatter.ISO_DATE)
        val ed = LocalDate.parse(endDate, DateTimeFormatter.ISO_DATE)
        val daysBetween = ChronoUnit.DAYS.between(sd, ed)

        for (i in 0..daysBetween) {
            val tab = binding.daysTab.newTab().setText("${i + 1}일차")
            binding.daysTab.addTab(tab)
        }
    }

    private fun setDaysTabItemMargin() {
        val tabs = binding.daysTab.getChildAt(0) as ViewGroup
        for (i in 0 until tabs.childCount) {
            val tab = tabs.getChildAt(i)
            val lp = tab.layoutParams as LinearLayout.LayoutParams
            val dpPxChanger = DpPxChanger()
            lp.marginEnd = dpPxChanger.dpToPx(this, AiScheduleDialog.TAB_ITEM_MARGIN)
            lp.width = dpPxChanger.dpToPx(this, AiScheduleDialog.TAB_ITEM_WIDTH)
            lp.height = dpPxChanger.dpToPx(this, AiScheduleDialog.TAB_ITEM_HEIGHT)
            tab.layoutParams = lp
        }
        binding.daysTab.requestLayout()
    }

    private fun swapOrderNum(fromPosition: Long, toPosition: Long) {
        //실제로 이 함수에서는 position이 아닌 orderNum으로 처리(receiptList와 어댑터 안의 리스트는 다른 리스트이기 때문)
        var fromDataOrderNum: Long? = null
        var toDataOrderNum: Long? = null

        for (item in receiptList) {
            when (item.orderNum) {
                fromPosition -> fromDataOrderNum = item.orderNum
                toPosition -> toDataOrderNum = item.orderNum
            }

            if (fromDataOrderNum != null && toDataOrderNum != null) {
                break
            }
        }
        if (fromDataOrderNum != null && toDataOrderNum != null) {
            receiptList.find { it.orderNum == fromPosition }?.orderNum = toPosition
            receiptList.find { it.orderNum == toPosition }?.orderNum = fromPosition
        }
    }
    private fun getChangeOrderReceipt() : List<ChangeOrderReceiptRequest> = receiptList.map { ChangeOrderReceiptRequest(it.receiptId, it.purchaseDate, it.orderNum) }

    override fun onItemClickListener(data: ReceiptList, position: Int) {
        val intent = Intent(this, BillInputActivity::class.java)
        intent.putExtra(getString(R.string.server_calendar_id), id)
        intent.putExtra(getString(R.string.receipt_id), data.receiptId)
        intent.putExtra(getString(R.string.store_name), data.storeName)
        intent.putExtra(getString(R.string.current_tab), currentTab)
        intent.putExtra(getString(R.string.current_date), currentDate)
        intent.putExtra(getString(R.string.currency), currency)
        startActivity(intent)
        finish()
    }

    override fun onMoreMenuDateChangeClickListener(data: ReceiptList, position: Int) {
        RecordDatePickerDialog(
            data.receiptId,
            data.purchaseDate,
            recordViewModel.getDatesList(startDate, endDate),
            this
        ).show(supportFragmentManager, "RecordDatePickerDialog")
    }

    override fun onMoreMenuOrderChangeClickListener(position: Int) {
        OrderChangeDialog(
            adapter.getDataAllName().toMutableList(),
            position,
            this
        ).show(supportFragmentManager, "OrderChangeDialog")
    }

    override fun onMoreMenuDeleteClickListener(id: Long, position: Int) {
        lifecycleScope.launch {
            recordViewModel.deleteReceipt(id).collect { check ->
                if (check) {
                    totalPrice -= adapter.remove(position)
                    binding.cost.text =
                        NumberFormat.getNumberInstance(Locale.getDefault()).format(totalPrice)
                }
            }
        }
    }

    override fun onDateSetListener(receiptId: Long, newDate: String) {
        adapter.updateDate(receiptId, newDate)
        receiptList.first{ it.receiptId == receiptId }.purchaseDate = newDate
    }

    override fun onOrderChangeListener(changedPosition: Int, previousPosition: Int) {
        val previous = adapter.getData(previousPosition)
        val changed = adapter.getData(changedPosition)

        swapOrderNum(previous.orderNum, changed.orderNum)

        Log.d("BillActivity", "${previous.storeName}(${previous.receiptId}) : ${previous.orderNum}\t${changed.storeName}(${changed.receiptId}) : ${changed.orderNum}" +
                "\n${receiptList.first{ it.receiptId == previous.receiptId }.orderNum}\t${receiptList.first{ it.receiptId == changed.receiptId }.orderNum}")
        adapter.swap(changedPosition, previousPosition)
    }
}