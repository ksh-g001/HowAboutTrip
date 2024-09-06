package com.project.how.view.fragment.record

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
import androidx.navigation.fragment.navArgs
import com.project.how.R
import com.project.how.adapter.recyclerview.record.receipt.BillDetailsAdapter
import com.project.how.data_class.dto.recode.receipt.ReceiptDetail
import com.project.how.data_class.dto.recode.receipt.ReceiptDetailListItem
import com.project.how.data_class.dto.recode.receipt.StoreType
import com.project.how.databinding.FragmentBillNonOcrBinding
import com.project.how.view.activity.record.bill.BillInputActivity
import com.project.how.view_model.RecordViewModel
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.roundToLong


class BillNonOcrFragment : Fragment(), BillDetailsAdapter.OnPriceListener {
    private var _binding : FragmentBillNonOcrBinding? = null
    private val binding : FragmentBillNonOcrBinding
        get() = _binding!!
    private val recordViewModel : RecordViewModel by viewModels()
    private val args: BillNonOcrFragmentArgs by navArgs()
    private lateinit var adapter : BillDetailsAdapter
    private lateinit var data : ReceiptDetail
    private lateinit var currentDate : String
    private lateinit var currency : String
    private var id = -1L
    private var totalPrice = 0.0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        currentDate = args.date
        currency = args.currency
        id = args.id
        adapter = BillDetailsAdapter(mutableListOf<ReceiptDetailListItem>(), currency, this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = DataBindingUtil.inflate(inflater, R.layout.fragment_bill_non_ocr, container, false)
        binding.non = this
        binding.lifecycleOwner = viewLifecycleOwner
        binding.details.adapter = adapter
        binding.date.text = currentDate

        recordViewModel.saveCheckLiveData.observe(viewLifecycleOwner){
            if (it){
                close()
            }else{
                Toast.makeText(requireContext(), getString(R.string.server_network_error), Toast.LENGTH_SHORT).show()
            }
        }
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun add(){
        adapter.add()
    }

    fun confirm() {
        save()
    }

    private fun close(){
        val activity = requireActivity() as? BillInputActivity
        activity?.closeAllFragmentsAndFinishActivity()
    }

    private fun save(){
        lifecycleScope.launch {
            try {
                var storeName = binding.title.text.toString()
                if (storeName.isNullOrBlank()) storeName = getString(R.string.empty_title)
                data = ReceiptDetail(
                    id,
                    storeName,
                    StoreType.PLACE,
                    currentDate,
                    totalPrice,
                    adapter.getAllData())
                recordViewModel.saveReceiptNonImage(data)
            }catch (e : Exception){
                Log.e("BillNonOcrFragment", "save error: ${e.message}")
                Toast.makeText(requireContext(), getString(R.string.server_network_error), Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onTotalPriceListener(total: Double) {
        val formatted = NumberFormat.getNumberInstance(Locale.getDefault()).format(total)
        binding.total.text = getString(R.string.bill_total_price, formatted, currency)
        totalPrice = ((total*100).roundToLong().toDouble()/100)
    }

}