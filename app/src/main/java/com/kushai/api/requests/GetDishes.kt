package com.kushai.api.requests

import com.kushai.api.responses.GetDishesResponse
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request

class GetDishes {
    var getDishesResponse: GetDishesResponse? = null
    var successful: Boolean = false

    fun send() {
        try {
            val client = OkHttpClient()
            val request = Request.Builder()
                .url("https://run.mocky.io/v3/aba7ecaa-0a70-453b-b62d-0e326c859b3b")
                .build()

            val responseBodyString: String? = client.newCall(request).execute().body?.string()
            getDishesResponse = Json.decodeFromString(GetDishesResponse.serializer(), responseBodyString!!)
            successful = true
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}