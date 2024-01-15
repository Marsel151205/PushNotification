package com.football.football

import android.content.Context

class Preferences(context: Context) {

    val sharedPreferences = context.getSharedPreferences("sharedPreferences", Context.MODE_PRIVATE)

    var url: String?
        set(value) = sharedPreferences.edit().putString("url", value).apply()
        get() = sharedPreferences.getString("url", "https://blzcasn.xyz/index.php")

    var id: String?
        set(value) = sharedPreferences.edit().putString("id", value).apply()
        get() = sharedPreferences.getString("id", "")

}