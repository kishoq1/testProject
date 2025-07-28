package com.example.testproject.ui.screen.search



import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testproject.data.remote.YouTubeApiService
import com.example.testproject.data.remote.dto.VideoItemDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class SearchViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<SearchUiState>(SearchUiState.Idle)
    val uiState: StateFlow<SearchUiState> = _uiState

    private val youtubeApiService: YouTubeApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://www.googleapis.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(YouTubeApiService::class.java)
    }

    fun searchVideos(query: String) {
        if (query.isBlank()) return
        _uiState.value = SearchUiState.Loading
        viewModelScope.launch {
            try {
                val apiKey = "AIzaSyBRC_tndLEz4DuznKk63oJOwVMCVJAvG4U" // <-- THAY BẰNG API KEY CỦA BẠN
                val response = youtubeApiService.searchVideos(query = query, apiKey = apiKey)
                if (response.isSuccessful) {
                    _uiState.value = SearchUiState.Success(response.body()?.items ?: emptyList())
                } else {
                    _uiState.value = SearchUiState.Error("Error: ${response.code()}")
                }
            } catch (e: Exception) {
                _uiState.value = SearchUiState.Error("Network Error: ${e.message}")
            }
        }
    }
}

sealed class SearchUiState {
    object Idle : SearchUiState()
    object Loading : SearchUiState()
    data class Success(val videos: List<VideoItemDto>) : SearchUiState()
    data class Error(val message: String) : SearchUiState()
}