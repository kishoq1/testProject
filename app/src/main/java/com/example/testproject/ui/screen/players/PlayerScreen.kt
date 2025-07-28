package com.example.testproject.ui.screen.players



import android.annotation.SuppressLint
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun YouTubePlayerScreen(videoId: String) {
    // Giữ một tham chiếu đến WebView để có thể điều khiển
    val webView = remember { mutableStateOf<WebView?>(null) }

    // Sử dụng LocalLifecycleOwner để truy cập vào vòng đời của Activity/Fragment chứa nó
    val lifecycleOwner = LocalLifecycleOwner.current

    // DisposableEffect được dùng để thêm và xóa listener một cách an toàn
    // Nó sẽ tự động dọn dẹp (gỡ bỏ listener) khi màn hình này bị hủy
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            // Lắng nghe các sự kiện của vòng đời
            when (event) {
                Lifecycle.Event.ON_PAUSE -> {
                    // Khi app bị tạm dừng (tắt màn hình, chuyển app khác) -> DỪNG video
                    webView.value?.evaluateJavascript("if(player) { player.pauseVideo(); }", null)
                }
                Lifecycle.Event.ON_RESUME -> {
                    // Khi app quay trở lại -> TIẾP TỤC phát video
                    webView.value?.evaluateJavascript("if(player) { player.playVideo(); }", null)
                }
                else -> {
                    // Không làm gì với các sự kiện khác
                }
            }
        }

        // Thêm observer vào vòng đời
        lifecycleOwner.lifecycle.addObserver(observer)

        // Khối onDispose sẽ được gọi khi Composable bị hủy
        onDispose {
            // Gỡ bỏ observer để tránh rò rỉ bộ nhớ
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Giao diện WebView không thay đổi
    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { context ->
            WebView(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                webViewClient = WebViewClient()
                webChromeClient = WebChromeClient()
                // Gán tham chiếu WebView khi nó được tạo
                webView.value = this
            }
        },
        update = {
            it.loadDataWithBaseURL(
                "https://www.youtube.com",
                getYouTubeHTML(videoId), "text/html", "utf-8", null
            )
        }
    )
}



private fun getYouTubeHTML(videoId: String): String {
    return """
        <!DOCTYPE html>
        <html>
        <head>
            <style>
                body, html { margin: 0; padding: 0; height: 100%; overflow: hidden; background-color: #000; }
                #player { height: 100%; width: 100%; }
            </style>
        </head>
        <body>
            <div id="player"></div>
            <script>
                var tag = document.createElement('script');
                tag.src = "https://www.youtube.com/iframe_api";
                var firstScriptTag = document.getElementsByTagName('script')[0];
                firstScriptTag.parentNode.insertBefore(tag, firstScriptTag);
                var player;
                function onYouTubeIframeAPIReady() {
                    player = new YT.Player('player', {
                        height: '100%',
                        width: '100%',
                        videoId: '$videoId',
                        playerVars: {
                            'autoplay': 1,
                            'controls': 1,
                            'playsinline': 1,
                            'modestbranding': 1
                        },
                        events: { 'onReady': onPlayerReady }
                    });
                }
                function onPlayerReady(event) {
                    event.target.playVideo();
                }
            </script>
        </body>
        </html>
    """.trimIndent()
}