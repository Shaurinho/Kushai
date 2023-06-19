package com.kushai.api.requests

import com.kushai.api.responses.GetCategoriesResponse
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request

class GetCategories {
    var getCategoriesResponse: GetCategoriesResponse? = null
    var successful: Boolean = false

    fun send() {
        try {
            val client = OkHttpClient()
            val request = Request.Builder()
                .url("https://run.mocky.io/v3/058729bd-1402-4578-88de-265481fd7d54")
                .build()

            val responseBodyString: String? = client.newCall(request).execute().body?.string()
            getCategoriesResponse = Json.decodeFromString(GetCategoriesResponse.serializer(), responseBodyString!!)
            successful = true
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}