package com.example.testproject

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.example.testproject.navigation.AppNavigation
import com.example.testproject.ui.theme.TestProjectTheme

class MainActivity : ComponentActivity() {

    // 1. Khai báo launcher để yêu cầu quyền
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        // Ở đây bạn có thể xử lý kết quả sau khi người dùng đồng ý/từ chối
        // Ví dụ: hiển thị thông báo nếu họ từ chối.
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createNotificationChannel()
        askForNotificationPermission() // Bây giờ hàm này đã tồn tại
        setContent {
            // Theme được tạo tự động bởi Android Studio
            TestProjectTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Gọi hệ thống điều hướng chính
                    AppNavigation()
                }
            }
        }
    }

    // 2. Định nghĩa hàm bị thiếu
    private fun askForNotificationPermission() {
        // Chỉ yêu cầu quyền trên Android 13 (API 33) trở lên
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Kiểm tra xem quyền đã được cấp chưa
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                // Nếu chưa, yêu cầu quyền
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun createNotificationChannel() {
        // Chỉ tạo kênh trên Android 8.0 (API 26) trở lên
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Playback Controls"
            val descriptionText = "Controls for media playback"
            val importance = NotificationManager.IMPORTANCE_LOW // Để không có âm thanh
            val channel = NotificationChannel("playback_channel", name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}