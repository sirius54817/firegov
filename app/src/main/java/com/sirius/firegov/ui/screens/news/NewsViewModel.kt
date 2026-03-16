package com.sirius.firegov.ui.screens.news

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sirius.firegov.data.model.NewsArticle
import com.sirius.firegov.data.repository.NewsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NewsViewModel @Inject constructor(
    private val newsRepository: NewsRepository
) : ViewModel() {

    private val _indiaNews = MutableStateFlow<List<NewsArticle>>(emptyList())
    val indiaNews = _indiaNews.asStateFlow()

    private val _worldNews = MutableStateFlow<List<NewsArticle>>(emptyList())
    val worldNews = _worldNews.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private var indiaNextPage: String? = null
    private var worldNextPage: String? = null

    init {
        fetchInitialNews()
    }

    private fun fetchInitialNews() {
        viewModelScope.launch {
            _isLoading.value = true
            
            val indiaResponse = newsRepository.getIndiaNews()
            if (indiaResponse != null) {
                _indiaNews.value = indiaResponse.results
                indiaNextPage = indiaResponse.nextPage
            }

            val worldResponse = newsRepository.getWorldNews()
            if (worldResponse != null) {
                _worldNews.value = worldResponse.results
                worldNextPage = worldResponse.nextPage
            }
            
            _isLoading.value = false
        }
    }

    fun loadMore(isIndia: Boolean) {
        val nextPage = if (isIndia) indiaNextPage else worldNextPage
        if (nextPage == null || _isLoading.value) return

        viewModelScope.launch {
            _isLoading.value = true
            val response = if (isIndia) {
                newsRepository.getIndiaNews(nextPage)
            } else {
                newsRepository.getWorldNews(nextPage)
            }

            if (response != null) {
                if (isIndia) {
                    _indiaNews.value += response.results
                    indiaNextPage = response.nextPage
                } else {
                    _worldNews.value += response.results
                    worldNextPage = response.nextPage
                }
            }
            _isLoading.value = false
        }
    }
}
