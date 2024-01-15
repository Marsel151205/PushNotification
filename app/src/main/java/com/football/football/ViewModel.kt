package com.football.football

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ViewModel @Inject constructor(
    private val repository: Repository
) : ViewModel() {

    fun getData(url: String, code: String, utm: String, id: String, fcmToken: String) =
        repository.fetchData(url, code, utm, id, fcmToken)
}
