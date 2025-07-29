package com.example.testproject.ui.screen.players

import android.annotation.SuppressLint
import android.util.Log
import androidx.annotation.OptIn
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.offline.Downloader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.schabi.newpipe.extractor.NewPipe
import org.schabi.newpipe.extractor.downloader.Downloader
import org.schabi.newpipe.extractor.downloader.Request
import org.schabi.newpipe.extractor.downloader.Response
import org.schabi.newpipe.extractor.stream.StreamInfo

sealed class PlayerUiState {
    object Loading : PlayerUiState()
    data class Success(val videoUrl: String, val videoTitle: String) : PlayerUiState()
    data class Error(val message: String) : PlayerUiState()
}

@OptIn(UnstableApi::class)
@SuppressLint("StaticFieldLeak")
class PlayerViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<PlayerUiState>(PlayerUiState.Loading)
    val uiState = _uiState.asStateFlow()

    init {
        NewPipe.init(object : Downloader() {
            override fun execute(request: Request?): Response {
                return Response(200, null, emptyMap(), null, null)
            }
        })
    }

    fun extractVideoUrl(videoId: String) {
        viewModelScope.launch {
            _uiState.value = PlayerUiState.Loading
            withContext(Dispatchers.IO) {
                try {
                    val youtubeUrl = "https://www.youtube.com/watch?v=$videoId"
                    val info = StreamInfo.getInfo(youtubeUrl)
                    val videoStream = info.videoStreams
                        .filter { it.format?.name == "MPEG_4" && it.height < 720 }
                        .minByOrNull { it.height }

                    if (videoStream != null) {
                        withContext(Dispatchers.Main) {
                            _uiState.value = PlayerUiState.Success(videoStream.url, info.name)
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            _uiState.value = PlayerUiState.Error("Không tìm thấy luồng video phù hợp.")
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        _uiState.value = PlayerUiState.Error("Lỗi: ${e.message}")
                    }
                }
            }
        }
    }
}