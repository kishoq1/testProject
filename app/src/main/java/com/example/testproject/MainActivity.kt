package com.example.testproject


import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.media3.common.util.NotificationUtil.createNotificationChannel
import com.example.testproject.navigation.AppNavigation
import com.example.testproject.ui.theme.TestProjectTheme
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat




class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createNotificationChannel()
        askForNotificationPermission()
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
    private fun createNotificationChannel() {
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