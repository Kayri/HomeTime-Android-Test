package com.kayri.hometime.utils

import com.kayri.hometime.models.*
import io.reactivex.Observable
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface TramApiService {

    //Get Token
    @GET("GetDeviceToken")
    fun getDeviceToken(@Query("aid") aid: String, @Query("devInfo") devInfo: String): Observable<DeviceToken>

    //Return all routes
    @GET("GetRouteSummaries")
    fun getRouteSummaries(@Query("aid") aid: String, @Query("tkn") tkn: String): Observable<RouteSummaries>

    //Return all Stop for one Route
    @GET("GetRouteStopsByRoute/{routeNo}")
    fun getRouteStopsByRoute(@Path("routeNo") routeNo: Int, @Query("aid") aid: String, @Query("tkn") tkn: String): Observable<RouteStopsByRoute>

    //Return all Route at one Stop //TODO USELESS ??
    @GET("GetMainRoutesForStop/{stopNo}")
    fun getMainRoutesForStop(@Path("stopNo") stopNo: Int, @Query("aid") aid: String, @Query("tkn") tkn: String): Observable<MainRoutesForStop>

    //Return prediction for Stop and Route
    @GET("GetNextPredictedRoutesCollection/{stopNo}/{routeNo}/{lowFloor}")
    fun getNextPredictedRoutesCollection(@Path("stopNo") stopNo: Int, @Path("routeNo") routeNo: Int, @Path("lowFloor") lowFloor: Boolean, @Query("aid") aid: String, @Query("cid") cid: Int, @Query("tkn") tkn: String): Observable<NextPredictedRoutesCollection>


    companion object {
        fun create(): TramApiService {

            val retrofit = Retrofit.Builder()
                    .addCallAdapterFactory(
                            RxJava2CallAdapterFactory.create())
                    .addConverterFactory(MoshiConverterFactory.create())
                    .baseUrl("http://ws3.tramtracker.com.au/TramTracker/RestService/")
                    .build()

            return retrofit.create(TramApiService::class.java)
        }
    }
}