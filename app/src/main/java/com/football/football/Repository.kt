package com.football.football

import android.util.Log
import androidx.lifecycle.MutableLiveData
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject

class Repository @Inject constructor(
    private val service: ApiService,
    private val sharedPreferences: Preferences
) {

    fun fetchData(url: String, code: String, utm: String, id: String, fcmToken: String): MutableLiveData<String> {
        Log.e("kirill", "repository: ${service.fetchData(url, code, utm, id, fcmToken).request().url}")
        sharedPreferences.url = service.fetchData(url, code, utm, id, fcmToken).request().url.toString()
        val model: MutableLiveData<String> = MutableLiveData()
        service.fetchData(url, code, utm, id, fcmToken).enqueue(object : Callback<String> {

            override fun onResponse(call: Call<String>, response: Response<String>) {
                if (response.isSuccessful)
                    model.postValue(response.body())
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                Log.e("wool", "onFailure: $t")
            }
        })
        return model
    }
}
