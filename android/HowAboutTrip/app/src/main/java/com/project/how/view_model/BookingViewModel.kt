package com.project.how.view_model

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.how.data_class.dto.EmptyResponse
import com.project.how.data_class.dto.booking.airplane.GenerateOneWaySkyscannerUrlRequest
import com.project.how.data_class.dto.booking.airplane.GenerateSkyscannerUrlRequest
import com.project.how.data_class.dto.booking.airplane.GenerateSkyscannerUrlResponse
import com.project.how.data_class.dto.booking.airplane.GetFlightOffersRequest
import com.project.how.data_class.dto.booking.airplane.GetFlightOffersResponse
import com.project.how.data_class.dto.booking.airplane.GetLikeFlightResponse
import com.project.how.data_class.dto.booking.airplane.GetOneWayFlightOffersRequest
import com.project.how.data_class.dto.booking.airplane.GetOneWayFlightOffersResponse
import com.project.how.data_class.dto.booking.airplane.LikeFlightElement
import com.project.how.data_class.dto.booking.airplane.LikeOneWayFlightElement
import com.project.how.data_class.roomdb.RecentAirplane
import com.project.how.model.BookingRepository
import com.project.how.network.client.BookingRetrofit
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject

@HiltViewModel
class BookingViewModel @Inject constructor(
    private val bookingRepository: BookingRepository
) : ViewModel() {
    private val _flightOffersLiveData = bookingRepository.flightOffersLiveData
    private val _oneWayFlightOffersLiveData = bookingRepository.oneWayFlightOffersLiveData
    private val _skyscannerUrlLiveData = bookingRepository.skyscannerUrlLiveData
    private val _likeFlightLiveData = bookingRepository.likeFlightLiveData
    private val _likeFlightListLiveData = bookingRepository.likeFlightListLiveData
    private val _recentAirplaneLiveData = bookingRepository.recentAirplaneLiveData
    val flightOffersLiveData : LiveData<GetFlightOffersResponse>
        get() = _flightOffersLiveData
    val oneWayFlightOffersLiveData : LiveData<GetOneWayFlightOffersResponse>
        get() = _oneWayFlightOffersLiveData
    val skyscannerUrlLiveData : LiveData<String>
        get() = _skyscannerUrlLiveData
    val likeFlightLiveData : LiveData<GetLikeFlightResponse>
        get() = _likeFlightLiveData
    val likeFlightListLiveData : LiveData<MutableList<Long>>
        get() = _likeFlightListLiveData
    val recentAirplaneLiveData : LiveData<List<RecentAirplane>>
        get() = _recentAirplaneLiveData

    fun deleteAll(){
        viewModelScope.launch(Dispatchers.IO) {
            bookingRepository.deleteAll()
        }
    }

    fun fetchRecentAirplanes() {
        viewModelScope.launch(Dispatchers.IO) {
            val recentAirplanes = bookingRepository.fetchRecentAirplanes()
            Log.d("RoomDB", "fetchRecentAirplanes()\nrecentAirplanes.size : ${recentAirplanes.size}")
            bookingRepository.getRecentAirplane(recentAirplanes)
        }
    }

    fun addRecentAirplane(recentAirplane: RecentAirplane) {
        viewModelScope.launch(Dispatchers.IO) {
            bookingRepository.addRecentAirplane(recentAirplane)
        }
    }

    fun deleteAllRecentAirplanes(){
        viewModelScope.launch(Dispatchers.IO){
            bookingRepository.deleteAllRecentAirplane()
        }
    }

    fun getFlightOffers(getFlightOffersRequest: GetFlightOffersRequest) : Flow<Int> = callbackFlow{
        BookingRetrofit.getApiService()?.let {apiService ->
            apiService.getFlightOffers(getFlightOffersRequest)
                .enqueue(object : Callback<GetFlightOffersResponse>{
                    override fun onResponse(
                        call: Call<GetFlightOffersResponse>,
                        response: Response<GetFlightOffersResponse>
                    ) {
                        if (response.isSuccessful){
                            val result = response.body()
                            if (result != null){
                                Log.d("getFlightOffers is success", "result.size : ${result.size}")
                                if (result.isNotEmpty()){
                                    Log.d("getFlightOffers is success", "${result[0].departureIataCode} -> ${result[0].arrivalIataCode}")
                                    bookingRepository.getFlightOffers(result)
                                    trySend(SUCCESS)
                                }else{
                                    trySend(NOT_EXIST)
                                }
                            }else{
                                Log.d("getFlightOffers is success", "response.body is null\ncode : ${response.code()}")
                                trySend(NOT_EXIST)
                            }
                        }else{
                            Log.d("getFlightOffers is not success", "code : ${response.code()}")
                            trySend(NOT_EXIST)
                        }
                    }

                    override fun onFailure(call: Call<GetFlightOffersResponse>, t: Throwable) {
                        Log.d("getFlightOffers is failed", "${t.message}")
                        trySend(NETWORK_FAILED)
                    }

                })
        } ?: close()

        awaitClose()
    }.flowOn(Dispatchers.IO)

    fun getFlightOffers(getOneWayFlightOffersRequest: GetOneWayFlightOffersRequest) : Flow<Int> = callbackFlow<Int> {
        BookingRetrofit.getApiService()?.let {apiService ->
            apiService.getOneWayFligthOffers(getOneWayFlightOffersRequest)
                .enqueue(object : Callback<GetOneWayFlightOffersResponse>{
                    override fun onResponse(
                        call: Call<GetOneWayFlightOffersResponse>,
                        response: Response<GetOneWayFlightOffersResponse>
                    ) {
                        if (response.isSuccessful){
                            val result = response.body()
                            if (result != null){
                                Log.d("getFlightOffers is success", "result.size : ${result.size}")
                                if (result.isNotEmpty()){
                                    Log.d("getFlightOffers is success", "${result[0].departureIataCode} -> ${result[0].arrivalIataCode}")
                                    bookingRepository.getOneWayFlightOffers(result)
                                    trySend(SUCCESS)
                                }else{
                                    trySend(NOT_EXIST)
                                }
                            }else{
                                Log.d("getFlightOffers is success", "response.body is null\ncode : ${response.code()}")
                                trySend(NOT_EXIST)
                            }
                        }else{
                            Log.d("getFlightOffers is no6t success", "code : ${response.code()}")
                            trySend(NOT_EXIST)
                        }
                    }

                    override fun onFailure(call: Call<GetOneWayFlightOffersResponse>, t: Throwable) {
                        Log.d("getFlightOffers is failed", "${t.message}")
                        trySend(NETWORK_FAILED)
                    }

                })
        } ?: close()

        awaitClose()
    }.flowOn(Dispatchers.IO)

    fun generateSkyscannerUrl(generateSkyscannerUrlRequest: GenerateSkyscannerUrlRequest) : Flow<Int> = callbackFlow<Int> {
        BookingRetrofit.getApiService()?.let {apiService->
           apiService.generateSkyscannerUrl(generateSkyscannerUrlRequest)
               .enqueue(object : Callback<GenerateSkyscannerUrlResponse>{
                   override fun onResponse(
                       call: Call<GenerateSkyscannerUrlResponse>,
                       response: Response<GenerateSkyscannerUrlResponse>
                   ) {
                       if (response.isSuccessful){
                           val result = response.body()
                           if (result != null){
                               Log.d("generateSkyscannerUrl response is success", "url : ${result.url}")
                               bookingRepository.getSkyscannerUrl(result.url)
                               trySend(SUCCESS)
                           }else{
                               Log.d("generateSkyscannerUrl response is success", "response.body is null\ncode : ${response.code()}")
                               trySend(NOT_EXIST)
                           }
                       }else{
                           Log.d("generateSkyscannerUrl response is not success", "code : ${response.code()}")
                           trySend(NOT_EXIST)
                       }
                   }

                   override fun onFailure(call: Call<GenerateSkyscannerUrlResponse>, t: Throwable) {
                       Log.d("generateSkyscannerUrl is failed", "${t.message}")
                       trySend(NETWORK_FAILED)
                   }

               })
        } ?: close()

        awaitClose()
    }.flowOn(Dispatchers.IO)

    fun generateOneWaySkyscannerUrl(generateOneWaySkyscannerUrlRequest: GenerateOneWaySkyscannerUrlRequest) : Flow<Int> = callbackFlow<Int> {
        BookingRetrofit.getApiService()?.let {apiService->
            apiService.generateOneWaySkyscannerUrl(generateOneWaySkyscannerUrlRequest)
                .enqueue(object : Callback<GenerateSkyscannerUrlResponse>{
                    override fun onResponse(
                        call: Call<GenerateSkyscannerUrlResponse>,
                        response: Response<GenerateSkyscannerUrlResponse>
                    ) {
                        if (response.isSuccessful){
                            val result = response.body()
                            if (result != null){
                                Log.d("generateSkyscannerUrl response is success", "url : ${result.url}")
                                bookingRepository.getSkyscannerUrl(result.url)
                                trySend(SUCCESS)
                            }else{
                                Log.d("generateSkyscannerUrl response is success", "response.body is null\ncode : ${response.code()}")
                                trySend(NOT_EXIST)
                            }
                        }else{
                            Log.d("generateSkyscannerUrl response is not success", "code : ${response.code()}")
                            trySend(NOT_EXIST)
                        }
                    }

                    override fun onFailure(call: Call<GenerateSkyscannerUrlResponse>, t: Throwable) {
                        Log.d("generateSkyscannerUrl is failed", "${t.message}")
                        trySend(NETWORK_FAILED)
                    }

                })
        } ?: close()

        awaitClose()
    }.flowOn(Dispatchers.IO)

    fun like(likeFlightElement: LikeFlightElement, position: Int) : Flow<Int> = callbackFlow<Int> {
        BookingRetrofit.getApiService()?.let { apiService->
            apiService.addLikeFlight(likeFlightElement)
                .enqueue(object : Callback<String>{
                    override fun onResponse(p0: Call<String>, p1: Response<String>) {
                        if (p1.isSuccessful){
                            val result = p1.body()
                            if (result != null) {
                                bookingRepository.addLikeFlightList(result.toLong(), position)
                                Log.d("addLikeFlight", "success ${p1.code()}\nid : $result\n${likeFlightElement.totalPrice}\t${likeFlightElement.arrivalIataCode}")
                                trySend(SUCCESS)
                            }else{
                                Log.d("addLikeFlight", "response is failed\ncode : ${p1.code()}")
                                trySend(NOT_EXIST)
                            }
                        }else{
                            Log.d("addLikeFlight", "response is failed\ncode : ${p1.code()}")
                            trySend(NOT_EXIST)
                        }
                    }

                    override fun onFailure(p0: Call<String>, p1: Throwable) {
                        Log.d("addLikeFlight", "onFailure\ncode : ${p1.message}")
                        trySend(NETWORK_FAILED)
                    }

                })

        } ?: close()

        awaitClose()
    }.flowOn(Dispatchers.IO)

    fun like(likeOneWayFlightElement: LikeOneWayFlightElement, position: Int) : Flow<Int> = callbackFlow<Int> {
        BookingRetrofit.getApiService()?.let { apiService->
            apiService.addLikeOneWayFlight(likeOneWayFlightElement)
                .enqueue(object : Callback<String>{
                    override fun onResponse(p0: Call<String>, p1: Response<String>) {
                        if (p1.isSuccessful){
                            val result = p1.body()
                            if (result != null) {
                                bookingRepository.addLikeFlightList(result.toLong(), position)
                                Log.d("addLikeFlight", "success ${p1.code()}\nid : $result\n${likeOneWayFlightElement.totalPrice}\t${likeOneWayFlightElement.arrivalIataCode}")
                                trySend(SUCCESS)
                            }else{
                                Log.d("addLikeFlight", "response is failed\ncode : ${p1.code()}")
                                trySend(NOT_EXIST)
                            }
                        }else{
                            Log.d("addLikeFlight", "response is failed\ncode : ${p1.code()}")
                            trySend(NOT_EXIST)
                        }
                    }

                    override fun onFailure(p0: Call<String>, p1: Throwable) {
                        Log.d("addLikeFlight", "on Failure\n${p1.message}")
                        trySend(NETWORK_FAILED)
                    }

                })

        } ?: close()

        awaitClose()
    }.flowOn(Dispatchers.IO)

    fun getLikeFlight(): Flow<Int> = callbackFlow<Int> {
        BookingRetrofit.getApiService()?.let { apiService->
            apiService.getLikeFlight()
                .enqueue(object : Callback<GetLikeFlightResponse>{
                    override fun onResponse(
                        p0: Call<GetLikeFlightResponse>,
                        p1: Response<GetLikeFlightResponse>
                    ) {
                        if (p1.isSuccessful){
                            val result = p1.body()
                            if (result != null){
                                Log.d("getLikeFlight", "size : ${result.size}")
                                bookingRepository.getLikeFlight(result)
                                trySend(SUCCESS)
                            }else{
                                Log.d("getLikeFlight", "result is null\ncode : ${p1.code()}")
                                trySend(NOT_EXIST)
                            }
                        }else{
                            Log.d("getLikeFlight", "response is failed\ncode : ${p1.code()}")
                            trySend(NOT_EXIST)
                        }
                    }

                    override fun onFailure(p0: Call<GetLikeFlightResponse>, p1: Throwable) {
                        Log.d("getLikeFlight", "onFailure\ncode : ${p1.message}")
                        trySend(NETWORK_FAILED)
                    }

                })
        } ?: close()

        awaitClose()
    }.flowOn(Dispatchers.IO)

    fun unlike(id: Long, position: Int) : Flow<Int> = callbackFlow<Int> {
        BookingRetrofit.getApiService()?.let {apiService->
            apiService.deleteLikeFlight(id)
                .enqueue(object : Callback<EmptyResponse>{
                    override fun onResponse(p0: Call<EmptyResponse>, p1: Response<EmptyResponse>) {
                        if (p1.isSuccessful){
                            when(p1.code()){
                                SUCCESS->{
                                    bookingRepository.deleteLikeFlightList(position)
                                    trySend(SUCCESS)
                                }
                                NOT_EXIST->{trySend(NOT_EXIST)}
                                NOT_MINE->{trySend(NOT_MINE)}
                            }
                        }else{
                            Log.d("deleteLikeFlight", "response is failed\ncode : ${p1.code()}")
                            trySend(NETWORK_FAILED)
                        }
                    }

                    override fun onFailure(p0: Call<EmptyResponse>, p1: Throwable) {
                        Log.d("deleteLikeFlight", "onFailure\ncode : ${p1.message}")
                        trySend(NETWORK_FAILED)
                    }

                })

        } ?: close()

        awaitClose()
    }.flowOn(Dispatchers.IO)

    fun getLikeFlightList(data : MutableList<Long>){
        bookingRepository.getLikeFlightList(data)
    }

    companion object{
        const val NETWORK_FAILED = -1
        const val NOT_EXIST = 404
        const val NOT_MINE = 401
        const val SUCCESS = 200
    }
}