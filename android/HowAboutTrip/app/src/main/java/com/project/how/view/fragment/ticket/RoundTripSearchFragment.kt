package com.project.how.view.fragment.ticket

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.MaterialDatePicker
import com.project.how.R
import com.project.how.data_class.dto.booking.airplane.GetFlightOffersRequest
import com.project.how.data_class.dto.booking.airplane.GetFlightOffersResponse
import com.project.how.data_class.dto.booking.airplane.RoundTripFlightOffers
import com.project.how.databinding.FragmentRoundTripSearchBinding
import com.project.how.interface_af.OnLoadListener
import com.project.how.interface_af.interface_ff.OnAirportListener
import com.project.how.view.activity.ticket.RoundTripAirplaneListActivity
import com.project.how.view.dialog.ConfirmDialog
import com.project.how.view.dialog.bottom_sheet_dialog.AirportBottomSheetDialog
import com.project.how.view_model.BookingViewModel
import com.project.how.view_model.MemberViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.TimeZone

@AndroidEntryPoint
class RoundTripSearchFragment(private val onLoadListener: OnLoadListener) : Fragment(), OnAirportListener {
    private var _binding : FragmentRoundTripSearchBinding? = null
    private val binding : FragmentRoundTripSearchBinding
        get() = _binding!!
    private val bookingViewModel : BookingViewModel by viewModels()
    private var departureDate : String? = null
    private var desnationDate : String? = null
    private var nonStop = true
    private var destination : String? = null
    private var departure : String? = null
    private lateinit var input : GetFlightOffersRequest

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = DataBindingUtil.inflate(inflater, R.layout.fragment_round_trip_search, container, false)
        binding.roundTrip = this
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bookingViewModel.flightOffersLiveData.observe(viewLifecycleOwner){
            moveAirportList(it)
        }
    }

    fun showAirportInput(type: Int){
        val airport = AirportBottomSheetDialog(type, this)
        airport.show(childFragmentManager, "AirportBottomSheetDialog")
    }

    fun showCalendar(){
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
            binding.dateOutput.text = getString(R.string.date_text, formatted, formattedSecond)
            departureDate = formatted
            desnationDate = formattedSecond
        }
    }

    fun search(){
        lifecycleScope.launch {
            val adults = binding.adultNumber.text.toString().toLongOrNull() ?: 0L
            val children = binding.childNumber.text.toString().toLongOrNull() ?: 0L
            if ((departure != null) && (destination != null) && (departureDate != null) && (adults != 0L || children != 0L)){
                setUnEnabled()
                onLoadListener.onLoadStartListener()
                binding.search.isEnabled = false
                nonStop = if (binding.radioGroup.checkedRadioButtonId == R.id.non_inclusive) true else false
                input = GetFlightOffersRequest(
                    departure!!,
                    destination!!,
                    departureDate!!,
                    desnationDate!!,
                    adults,
                    children,
                    50,
                    nonStop
                )
                bookingViewModel.getFlightOffers(requireContext(), MemberViewModel.tokensLiveData.value!!.accessToken, input).collect{check->
                    setEnabled()
                    onLoadListener.onLoadFinishListener()
                    when(check){
                        BookingViewModel.NETWORK_FAILED->{Toast.makeText(requireContext(), getString(R.string.server_network_error), Toast.LENGTH_SHORT).show()}
                        BookingViewModel.NOT_EXIST->{
                            Toast.makeText(requireContext(),
                                getString(R.string.not_exist_flight_offers), Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }else{
                val message = mutableListOf<String>()
                if (departure == null)
                    message.add(getString(R.string.departure))
                if (destination == null)
                    message.add(getString(R.string.destination))
                if (departureDate == null)
                    message.add(getString(R.string.date))
                if (adults == 0L && children == 0L)
                    message.add(getString(R.string.people_num))
                val confirm = ConfirmDialog(message)
                confirm.show(childFragmentManager, "ConfirmDialog")
            }
        }
    }

    fun swap(){
        if((destination == null) && (departure == null)){
            return
        }else{
            val temp = departure
            departure = destination
            destination = temp
            binding.departure.text = departure ?: getString(R.string.departure)
            binding.destination.text = destination ?: getString(R.string.destination)
        }
    }

    private fun setUnEnabled(){
        binding.search.isEnabled = false
        binding.dateInput.isEnabled = false
        binding.departure.isEnabled = false
        binding.destination.isEnabled = false
        binding.adultNumber.isEnabled = false
        binding.childNumber.isEnabled = false
    }

    private fun setEnabled(){
        binding.search.isEnabled = true
        binding.dateInput.isEnabled = true
        binding.departure.isEnabled = true
        binding.destination.isEnabled = true
        binding.adultNumber.isEnabled = true
        binding.childNumber.isEnabled = true
    }

    private fun moveAirportList(flightOffers : GetFlightOffersResponse){
        if (!::input.isInitialized) {
            Log.e("RoundTripSearchFragment", "input is not initialized")
            return
        }
        val fo = ArrayList(flightOffers)
        val f = RoundTripFlightOffers(fo)
        val intent = Intent(activity, RoundTripAirplaneListActivity::class.java)
        intent.putExtra(getString(R.string.round_trip_flight_offers), f)
        intent.putExtra(getString(R.string.get_flight_offers_request), input)
        startActivity(intent)
    }

    override fun onAirportListener(type: Int, airport: String) {
        if(type == AirportBottomSheetDialog.DEPARTURE){
            departure = airport
            binding.departure.text = airport
        }else{
            destination = airport
            binding.destination.text = airport
        }
    }
}