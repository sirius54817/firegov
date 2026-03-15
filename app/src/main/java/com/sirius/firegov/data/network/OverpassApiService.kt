package com.sirius.firegov.data.network

import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Query

interface OverpassApiService {
    @GET("api/interpreter")
    suspend fun getFireStations(
        @Query("data") query: String
    ): ResponseBody

    companion object {
        const val BASE_URL = "https://overpass-api.de/"
    }
}
