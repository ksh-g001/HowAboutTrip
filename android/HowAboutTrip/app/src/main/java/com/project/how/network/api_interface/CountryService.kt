package com.project.how.network.api_interface

import com.project.how.data_class.dto.country.GetCountryInfoRequest
import com.project.how.data_class.dto.country.GetCountryLocationResponse
import com.project.how.data_class.dto.country.exchangerate.GetAllExchangeRatesResponse
import com.project.how.data_class.dto.country.weather.GetCurrentWeatherResponse
import com.project.how.data_class.dto.country.weather.GetWeeklyWeathersResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST

interface CountryService {

    @Headers("No-Authorization: true")
    @POST("countries/locations")
    fun getCountryLocation(
        @Body country : GetCountryInfoRequest
    ) : Call<GetCountryLocationResponse>

    @Headers("No-Authorization: true")
    @POST("countries/weather/current")
    fun getCurrentWeather(
        @Body country: GetCountryInfoRequest
    ) : Call<GetCurrentWeatherResponse>

    @Headers("No-Authorization: true")
    @POST("countries/weather/weekly")
    fun getWeeklyWeather(
        @Body country: GetCountryInfoRequest
    ) : Call<GetWeeklyWeathersResponse>

    @Headers("No-Authorization: true")
    @GET("countries/exchange-rates")
    fun getAllExchangeRates() : Call<GetAllExchangeRatesResponse>
}