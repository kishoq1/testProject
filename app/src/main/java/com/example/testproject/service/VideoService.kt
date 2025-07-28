package com.example.testproject.service

import android.app.Notification
import android.app.PendingIntent
import android.content.Intent
import android.graphics.Bitmap
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.ui.PlayerNotificationManager
import com.example.testproject.R // Quan trọng: Hãy chắc chắn bạn đã import đúng R của project
import com.example.testproject.MainActivity // Thay thế bằng Activity chính của bạn

@UnstableApi
class PlaybackService : MediaSessionService() {

    private var mediaSession: MediaSession? = null
    private lateinit var player: ExoPlayer
    private lateinit var notificationManager: PlayerNotificationManager

    // Định danh cho kênh thông báo và ID thông báo
    private val NOTIFICATION_CHANNEL_ID = "playback_channel"
    private val NOTIFICATION_ID = 123

    // Hàm này được gọi khi Service được tạo
    override fun onCreate() {
        super.onCreate()
        // 1. Khởi tạo ExoPlayer
        player = ExoPlayer.Builder(this).build()
        // 2. Khởi tạo MediaSession và liên kết nó với ExoPlayer
        mediaSession = MediaSession.Builder(this, player).build()

        // 3. Khởi tạo PlayerNotificationManager để quản lý Foreground Service
        notificationManager = PlayerNotificationManager.Builder(
            this,
            NOTIFICATION_ID,
            NOTIFICATION_CHANNEL_ID
        )
            .setMediaDescriptionAdapter(object : PlayerNotificationManager.MediaDescriptionAdapter {
                // Cung cấp thông tin hiển thị trên thông báo (tên bài hát, ca sĩ,...)
                override fun getCurrentContentTitle(player: Player): CharSequence {
                    return player.mediaMetadata.title ?: "Đang phát"
                }

                override fun createCurrentContentIntent(player: Player): PendingIntent? {
                    // Intent để mở lại ứng dụng khi người dùng nhấn vào thông báo
                    val intent = Intent(this@PlaybackService, MainActivity::class.java)
                    return PendingIntent.getActivity(
                        this@PlaybackService, 0, intent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                }

                override fun getCurrentContentText(player: Player): CharSequence? {
                    return player.mediaMetadata.artist ?: "Không rõ nghệ sĩ"
                }

                override fun getCurrentLargeIcon(
                    player: Player,
                    callback: PlayerNotificationManager.BitmapCallback
                ): Bitmap? {
                    // Bạn có thể tải ảnh bìa album ở đây
                    // Ví dụ: return BitmapFactory.decodeResource(resources, R.drawable.album_art)
                    return null // Trả về null để dùng icon mặc định
                }
            })
            .setNotificationListener(object : PlayerNotificationManager.NotificationListener {
                // Lắng nghe sự kiện của thông báo
                override fun onNotificationPosted(
                    notificationId: Int,
                    notification: Notification,
                    ongoing: Boolean
                ) {
                    // SỬA LỖI: Logic để bắt đầu foreground service.
                    // Điều kiện `ongoing` là đủ. Nó sẽ đúng khi nhạc đang phát, đang buffer, v.v.
                    // Việc kiểm tra thêm `player.isPlaying` có thể gây lỗi vì khi buffer, `isPlaying` là false.
                    if (ongoing) {
                        // Nếu thông báo đang hiển thị (nhạc đang chạy), đưa service lên foreground.
                        startForeground(notificationId, notification)
                    } else {
                        // Nếu thông báo không còn (nhạc đã dừng), gỡ service khỏi foreground.
                        // Tham số `false` sẽ xóa thông báo đi.
                        stopForeground(false)
                    }
                }

                override fun onNotificationCancelled(notificationId: Int, dismissedByUser: Boolean) {
                    // Khi người dùng vuốt bỏ thông báo
                    stopSelf() // Dừng service
                }
            })
            .setChannelNameResourceId(R.string.playback_channel_name) // Tên kênh thông báo từ strings.xml
            .setChannelDescriptionResourceId(R.string.playback_channel_description) // Mô tả kênh từ strings.xml
            .build()

        // 4. Gắn Player và MediaSession.Token vào NotificationManager
        notificationManager.setPlayer(player)
        notificationManager.setMediaSessionToken(mediaSession!!.sessionCompatToken)
    }

    // Hàm này được gọi khi một client (như Activity) muốn kết nối đến MediaSession
    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    // Xử lý các lệnh từ client
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // QUAN TRỌNG: Trên Android 13 (API 33) trở lên, bạn PHẢI yêu cầu quyền POST_NOTIFICATIONS
        // trong Activity của mình trước khi bắt đầu service này. Nếu không, thông báo sẽ không hiển thị.

        val urlToPlay = intent?.getStringExtra("URL_TO_PLAY")
        val title = intent?.getStringExtra("TITLE") ?: "Không rõ tiêu đề"
        val artist = intent?.getStringExtra("ARTIST") ?: "Không rõ nghệ sĩ"

        urlToPlay?.let {
            // Tạo MediaItem với metadata để hiển thị trên thông báo
            val mediaItem = MediaItem.Builder()
                .setUri(it)
                .setMediaMetadata(
                    androidx.media3.common.MediaMetadata.Builder()
                        .setTitle(title)
                        .setArtist(artist)
                        .build()
                )
                .build()

            player.setMediaItem(mediaItem)
            player.prepare()
            player.play() // Bắt đầu phát nhạc
        }
        // Gọi super để MediaSessionService xử lý các lệnh media
        return super.onStartCommand(intent, flags, startId)
    }

    // SỬA LỖI: Xử lý khi người dùng vuốt ứng dụng khỏi màn hình ứng dụng gần đây.
    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        // Khi người dùng xóa tác vụ, hãy dừng phát nhạc và dừng service.
        if (!player.playWhenReady) {
            player.stop()
            stopSelf()
        }
    }

    // Dọn dẹp tài nguyên khi Service bị hủy
    override fun onDestroy() {
        mediaSession?.run {
            // Giải phóng NotificationManager trước
            notificationManager.setPlayer(null)
            // Giải phóng Player và MediaSession
            player.release()
            release()
            mediaSession = null
        }
        super.onDestroy()
    }
}
