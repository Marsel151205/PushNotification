package com.football.football

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {

    @GET("/index.php")
    fun fetchData(
        @Query("GAID") url: String,
        @Query("code") code: String,
        @Query("IR") utm: String,
        @Query("id") analyticsId: String,
        @Query("messangingToken") fcmToken: String
    ): Call<String>
}
