package com.example.testproject.navigation

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.testproject.ui.screen.search.SearchScreen
import com.example.testproject.ui.screen.players.YouTubePlayerScreen


@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "search") {

        // --- MÀN HÌNH TÌM KIẾM ---
        composable(
            route = "search" // Tuyến đường cho màn hình tìm kiếm
        ) {
            SearchScreen(
                onVideoClicked = { videoId ->
                    //  Lệnh điều hướng. Chuỗi này phải khớp với route của màn hình player
                    navController.navigate("player/$videoId")
                }
            )
        }

        // --- MÀN HÌNH PHÁT VIDEO ---
        composable(
            // 3. ĐỊNH NGHĨA TUYẾN ĐƯỜNG:
            // Hãy chắc chắn chuỗi này "player/{videoId}" giống hệt 100% với
            // chuỗi trong lệnh navController.navigate ở trên.
            route = "player/{videoId}",

            arguments = listOf(navArgument("videoId") { type = NavType.StringType })
        ) { backStackEntry ->
            // Lấy videoId từ arguments của route
            val videoId = backStackEntry.arguments?.getString("videoId") ?: ""

            // 4. In ra log để xem màn hình Player có được gọi và có nhận được ID không
            Log.d("AppNavigation", "PlayerScreen is being composed with ID: $videoId")

            YouTubePlayerScreen(videoId = videoId)
        }
    }
}