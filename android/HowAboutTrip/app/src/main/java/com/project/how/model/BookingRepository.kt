package com.project.how.model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.project.how.data_class.dto.GetFlightOffersResponse
import com.project.how.data_class.dto.GetOneWayFlightOffersResponse
import com.project.how.data_class.dto.LikeFlightList

class BookingRepository {
    private val _flightOffersLiveData = MutableLiveData<GetFlightOffersResponse>()
    private val _oneWayFlightLiveData = MutableLiveData<GetOneWayFlightOffersResponse>()
    private val _skyscannerUrlLiveData = MutableLiveData<String>()
    val flightOffersLiveData : LiveData<GetFlightOffersResponse>
        get() = _flightOffersLiveData
    val oneWayFlightOffersLiveData : LiveData<GetOneWayFlightOffersResponse>
        get() = _oneWayFlightLiveData
    val skyscannerUrlLiveData : LiveData<String>
        get() = _skyscannerUrlLiveData

    fun getFlightOffers(getFlightOffersResponse: GetFlightOffersResponse){
        _flightOffersLiveData.postValue(getFlightOffersResponse)
    }

    fun getOneWayFlightOffers(getOneWayFlightOffersResponse: GetOneWayFlightOffersResponse){
        _oneWayFlightLiveData.postValue(getOneWayFlightOffersResponse)
    }

    fun getSkyscannerUrl(url : String){
        _skyscannerUrlLiveData.postValue(url)
    }
}