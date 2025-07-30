package com.example.testproject.ui.screen.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testproject.data.remote.dto.SearchItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.schabi.newpipe.extractor.NewPipe
import org.schabi.newpipe.extractor.ServiceList
import org.schabi.newpipe.extractor.search.SearchInfo
import org.schabi.newpipe.extractor.stream.StreamInfoItem

// Cập nhật sealed class SearchUiState
sealed class SearchUiState {
    object Idle : SearchUiState()
    object Loading : SearchUiState()
    data class Success(val videos: List<SearchItem>) : SearchUiState() // Dùng model mới
    data class Error(val message: String) : SearchUiState()
}

class SearchViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<SearchUiState>(SearchUiState.Idle)
    val uiState: StateFlow<SearchUiState> = _uiState

    fun searchVideos(query: String) {
        if (query.isBlank()) return
        _uiState.value = SearchUiState.Loading

        viewModelScope.launch {
            try {
                // Thực hiện tìm kiếm trên Dispatchers.IO
                val searchResult = withContext(Dispatchers.IO) {
                    val service = ServiceList.YouTube
                    val url = service.searchQHFactory.fromQuery(query)
                    SearchInfo.getInfo(service, url)
                }

                // Chuyển đổi kết quả từ NewPipeExtractor sang model của app
                val videoList = searchResult.relatedItems.mapNotNull { item ->
                    if (item is StreamInfoItem) {
                        SearchItem(
                            videoId = item.url.substringAfter("v="),
                            title = item.name,
                            thumbnailUrl = item.thumbnails.toString()
                        )
                    } else {
                        null
                    }
                }
                _uiState.value = SearchUiState.Success(videoList)

            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.value = SearchUiState.Error("Lỗi khi tìm kiếm: ${e.message}")
            }
        }
    }
}