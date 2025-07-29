package com.example.testproject.ui.screen.players

import android.annotation.SuppressLint
import android.content.Context
import android.util.SparseArray
import androidx.lifecycle.ViewModel
import at.huber.youtubeExtractor.VideoMeta
import at.huber.youtubeExtractor.YouTubeExtractor
import at.huber.youtubeExtractor.YtFile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

sealed class PlayerUiState {
    object Loading : PlayerUiState()
    data class Success(val videoUrl: String, val videoTitle: String) : PlayerUiState()
    data class Error(val message: String) : PlayerUiState()
}

@SuppressLint("StaticFieldLeak")
class PlayerViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<PlayerUiState>(PlayerUiState.Loading)
    val uiState = _uiState.asStateFlow()

    fun extractVideoUrl(context: Context, videoId: String) {
        _uiState.value = PlayerUiState.Loading

        // === SỬA LỖI TẠI ĐÂY ===
        // Xây dựng URL YouTube chính xác từ videoId được truyền vào
        val youtubeLink = "http://youtube.com/watch?v=$videoId"

        object : YouTubeExtractor(context) {
            override fun onExtractionComplete(ytFiles: SparseArray<YtFile>?, videoMeta: VideoMeta?) {
                if (videoMeta == null || ytFiles == null) {
                    _uiState.value = PlayerUiState.Error("Không thể lấy thông tin video.")
                    return
                }

                // Tìm luồng video có cả hình và tiếng (muxed)
                // itag 22 là 720p, 18 là 360p (định dạng mp4)
                val itag = 22 // Ưu tiên 720p
                var downloadUrl = ytFiles[itag]?.url

                if (downloadUrl == null) {
                    // Nếu không có 720p, thử 360p
                    downloadUrl = ytFiles[18]?.url
                }

                if (downloadUrl != null) {
                    _uiState.value = PlayerUiState.Success(downloadUrl, videoMeta.title)
                } else {
                    _uiState.value = PlayerUiState.Error("Không tìm thấy luồng video MP4 phù hợp.")
                }
            }
        }.extract(youtubeLink, true, true)
    }
}