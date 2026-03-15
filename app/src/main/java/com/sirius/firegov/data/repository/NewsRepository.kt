package com.sirius.firegov.data.repository

import com.sirius.firegov.data.model.NewsArticle
import com.sirius.firegov.data.network.NewsApiService
import javax.inject.Inject
import javax.inject.Singleton

import com.sirius.firegov.BuildConfig

@Singleton
class NewsRepository @Inject constructor(
    private val newsApiService: NewsApiService
) {
    private val apiKey = BuildConfig.NEWSDATA_API_KEY

    suspend fun getIndiaNews(): List<NewsArticle> {
        return try {
            newsApiService.getLatestNews(
                apiKey = apiKey,
                query = "fire rescue",
                country = "in"
            ).results
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getWorldNews(): List<NewsArticle> {
        return try {
            newsApiService.getLatestNews(
                apiKey = apiKey,
                query = "fire rescue disaster",
                excludeCountry = "in"
            ).results
        } catch (e: Exception) {
            emptyList()
        }
    }
}
