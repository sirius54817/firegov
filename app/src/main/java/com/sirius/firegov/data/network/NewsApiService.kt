package com.sirius.firegov.data.network

import com.sirius.firegov.data.model.NewsResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface NewsApiService {
    @GET("api/1/latest")
    suspend fun getLatestNews(
        @Query("apikey") apiKey: String,
        @Query("q") query: String,
        @Query("language") language: String = "en",
        @Query("country") country: String? = null,
        @Query("excludecountry") excludeCountry: String? = null,
        @Query("page") page: String? = null
    ): NewsResponse

    companion object {
        const val BASE_URL = "https://newsdata.io/"
    }
}
