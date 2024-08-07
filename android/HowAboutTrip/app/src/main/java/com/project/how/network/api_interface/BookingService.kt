package com.project.how.network.api_interface

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
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.Call
import retrofit2.http.Header
import retrofit2.http.Path

interface BookingService {
    @POST("bookings/flights/search")
    fun getFlightOffers(
        @Header("Authorization") accessToken : String,
        @Body getFlightOffersRequest: GetFlightOffersRequest
    ) : Call<GetFlightOffersResponse>

    @POST("bookings/flights/search")
    fun getOneWayFligthOffers(
        @Header("Authorization") accessToken : String,
        @Body getOneWayFlightOffersRequest: GetOneWayFlightOffersRequest
    ) : Call<GetOneWayFlightOffersResponse>

    @POST("bookings/flights/connect")
    fun generateSkyscannerUrl(
        @Header("Authorization") accessToken : String,
        @Body generateSkyscannerUrlRequest: GenerateSkyscannerUrlRequest
    ) : Call<GenerateSkyscannerUrlResponse>

    @POST("bookings/flights/connect")
    fun generateOneWaySkyscannerUrl(
        @Header("Authorization") accessToken : String,
        @Body generateOneWaySkyscannerUrlRequest: GenerateOneWaySkyscannerUrlRequest
    ) : Call<GenerateSkyscannerUrlResponse>

    @POST("bookings/flights/like")
    fun addLikeFlight(
        @Header("Authorization") accessToken : String,
        @Body addLikeFlightRequest: LikeFlightElement
    ) : Call<String>

    @POST("bookings/flights/like")
    fun addLikeOneWayFlight(
        @Header("Authorization") accessToken : String,
        @Body addLikeFlightRequest: LikeOneWayFlightElement
    ) : Call<String>

    @GET("bookings/flights/likes")
    fun getLikeFlight(
        @Header("Authorization") accessToken : String
    ) : Call<GetLikeFlightResponse>

    @DELETE("bookings/flights/like/{id}")
    fun deleteLikeFlight(
        @Header("Authorization") accessToken : String,
        @Path("id", encoded = true) id : Long
    ) : Call<EmptyResponse>
}