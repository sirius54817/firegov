package com.sirius.firegov.data.repository

import com.sirius.firegov.data.model.NewsArticle
import com.sirius.firegov.data.network.NewsApiService
import javax.inject.Inject
import javax.inject.Singleton

import com.sirius.firegov.BuildConfig

import com.sirius.firegov.data.model.NewsResponse

@Singleton
class NewsRepository @Inject constructor(
    private val newsApiService: NewsApiService
) {
    private val apiKey = BuildConfig.NEWSDATA_API_KEY

    suspend fun getIndiaNews(page: String? = null): NewsResponse? {
        return try {
            newsApiService.getLatestNews(
                apiKey = apiKey,
                query = "fire rescue",
                country = "in",
                page = page
            )
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getWorldNews(page: String? = null): NewsResponse? {
        return try {
            newsApiService.getLatestNews(
                apiKey = apiKey,
                query = "fire rescue disaster",
                excludeCountry = "in",
                page = page
            )
        } catch (e: Exception) {
            null
        }
    }
}
