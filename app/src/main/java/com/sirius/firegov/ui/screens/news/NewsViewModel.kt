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

    init {
        fetchNews()
    }

    fun fetchNews() {
        viewModelScope.launch {
            _isLoading.value = true
            _indiaNews.value = newsRepository.getIndiaNews()
            _worldNews.value = newsRepository.getWorldNews()
            _isLoading.value = false
        }
    }
}
