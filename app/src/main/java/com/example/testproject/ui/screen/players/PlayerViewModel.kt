package com.example.testproject.ui.screen.players

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.schabi.newpipe.extractor.NewPipe
import org.schabi.newpipe.extractor.stream.StreamInfo

// Giữ nguyên sealed class PlayerUiState
sealed class PlayerUiState {
    object Loading : PlayerUiState()
    data class Success(val videoUrl: String, val videoTitle: String) : PlayerUiState()
    data class Error(val message: String) : PlayerUiState()
}

class PlayerViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<PlayerUiState>(PlayerUiState.Loading)
    val uiState = _uiState.asStateFlow()

    fun extractVideoUrl(videoId: String) {
        if (videoId.isBlank()) {
            _uiState.value = PlayerUiState.Error("Video ID không hợp lệ.")
            return
        }
        _uiState.value = PlayerUiState.Loading

        viewModelScope.launch {
            try {
                // Thực hiện công việc mạng trên Coroutine Dispatcher.IO
                val streamInfo = withContext(Dispatchers.IO) {
                    val youtubeLink = "https://www.youtube.com/watch?v=$videoId"
                    StreamInfo.getInfo(NewPipe.getService(0), youtubeLink)
                }

                // Chọn luồng video tốt nhất (có cả hình và tiếng)
                // Ưu tiên 720p, nếu không có thì lấy luồng tốt nhất có thể
                val videoUrl = streamInfo.videoStreams
                    .firstOrNull { it.format?.id == 22 }?.url // itag 22 = 720p MP4
                    ?: streamInfo.videoStreams.firstOrNull { it.format?.id == 18 }?.url // itag 18 = 360p MP4
                    ?: streamInfo.videoStreams.maxByOrNull { it.resolution.split("p")[0].toIntOrNull() ?: 0 }?.url


                if (videoUrl != null) {
                    _uiState.value = PlayerUiState.Success(videoUrl, streamInfo.name)
                } else {
                    _uiState.value = PlayerUiState.Error("Không tìm thấy luồng video phù hợp.")
                }

            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.value = PlayerUiState.Error("Lỗi khi trích xuất video: ${e.message}")
            }
        }
    }
}