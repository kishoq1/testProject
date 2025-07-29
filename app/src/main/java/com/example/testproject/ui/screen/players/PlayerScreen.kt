package com.example.testproject.ui.screen.players

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import androidx.media3.ui.PlayerView
import com.example.testproject.service.PlaybackService
import com.google.common.util.concurrent.MoreExecutors

@Composable
fun YouTubePlayerScreen(
    videoId: String,
    viewModel: PlayerViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    // Lấy URL video một lần, truyền context vào
    LaunchedEffect(key1 = videoId) {
        viewModel.extractVideoUrl(context, videoId)
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        when (val state = uiState) {
            is PlayerUiState.Loading -> {
                CircularProgressIndicator()
            }
            is PlayerUiState.Success -> {
                // Khi có URL, khởi động service và hiển thị trình phát video
                VideoPlayer(context = context, videoUrl = state.videoUrl, title = state.videoTitle)
            }
            is PlayerUiState.Error -> {
                Text(state.message)
            }
        }
    }
}

@Composable
private fun VideoPlayer(context: Context, videoUrl: String, title: String) {
    var mediaController by remember { mutableStateOf<MediaController?>(null) }

    // Khởi động service với URL duy nhất
    LaunchedEffect(videoUrl) {
        val serviceIntent = Intent(context, PlaybackService::class.java).apply {
            putExtra("URL_TO_PLAY", videoUrl)
            putExtra("TITLE", title)
        }
        context.startService(serviceIntent)
    }

    DisposableEffect(Unit) {
        val sessionToken = SessionToken(context, ComponentName(context, PlaybackService::class.java))
        val controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
        controllerFuture.addListener(
            { mediaController = controllerFuture.get() },
            MoreExecutors.directExecutor()
        )
        onDispose {
            mediaController?.release()
        }
    }

    if (mediaController != null) {
        AndroidView(
            factory = {
                PlayerView(it).apply {
                    player = mediaController
                    useController = true // Bật thanh điều khiển
                }
            },
            modifier = Modifier.fillMaxSize()
        )
    } else {
        CircularProgressIndicator()
    }
}