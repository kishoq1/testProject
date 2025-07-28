package com.example.testproject.ui.screen.search


import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.testproject.data.remote.dto.VideoItemDto
import com.example.testproject.ui.screen.search.SearchUiState
import com.example.testproject.ui.screen.search.SearchViewModel


/**
 * Composable chính cho toàn bộ màn hình tìm kiếm.
 *
 * @param onVideoClicked Một hàm callback để thông báo cho hệ thống điều hướng biết rằng
 * một video đã được chọn và truyền đi ID của video đó.
 * @param viewModel ViewModel chịu trách nhiệm xử lý logic và trạng thái của màn hình.
 */
@Composable
fun SearchScreen(
    onVideoClicked: (String) -> Unit,
    viewModel: SearchViewModel = viewModel()
) {
    // Lắng nghe và nhận các thay đổi trạng thái từ ViewModel
    val uiState by viewModel.uiState.collectAsState()

    // State cục bộ để lưu giữ nội dung của thanh tìm kiếm
    var query by remember { mutableStateOf("") }

    val context = LocalContext.current

    Column(modifier = Modifier.padding(16.dp)) {
        // Hàng chứa thanh tìm kiếm và nút bấm
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                label = { Text("Search YouTube...") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(onClick = { viewModel.searchVideos(query) }) {
                Icon(Icons.Default.Search, contentDescription = "Search")
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Vùng hiển thị kết quả dựa trên trạng thái từ ViewModel
        Box(modifier = Modifier.fillMaxSize()) {
            when (val state = uiState) {
                is SearchUiState.Idle -> {
                    Text("Enter a query to search for videos.", modifier = Modifier.align(Alignment.Center))
                }
                is SearchUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is SearchUiState.Success -> {
                    // Nếu thành công, hiển thị danh sách video và truyền hàm onVideoClicked xuống
                    VideoList(videos = state.videos, onVideoClicked = onVideoClicked)
                }
                is SearchUiState.Error -> {
                    Text(state.message, modifier = Modifier.align(Alignment.Center))
                    // Hiển thị một thông báo ngắn khi có lỗi
                    LaunchedEffect(state.message) {
                        Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }
}

/**
 * Composable để hiển thị danh sách các video.
 */
@Composable
private fun VideoList(videos: List<VideoItemDto>, onVideoClicked: (String) -> Unit) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        items(videos) { video ->
            VideoItemRow(video = video, onVideoClicked = onVideoClicked)
        }
    }
}

/**
 * Composable cho một hàng (item) trong danh sách video.
 */
@Composable
private fun VideoItemRow(video: VideoItemDto, onVideoClicked: (String) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onVideoClicked(video.id.videoId) }, // <-- Đây là nơi sự kiện click được kích hoạt
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = video.snippet.thumbnails.mediumQuality.url,
                contentDescription = "Video thumbnail",
                modifier = Modifier.size(120.dp, 90.dp),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = video.snippet.title,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 3
            )
        }
    }
}