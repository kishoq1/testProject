package com.example.testproject.data.remote


import com.example.testproject.data.remote.dto.YouTubeSearchResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface YouTubeApiService {
    @GET("youtube/v3/search")
    suspend fun searchVideos(
        @Query("part") part: String = "snippet",
        @Query("q") query: String,
        @Query("type") type: String = "video",
        @Query("key") apiKey: String,
        @Query("maxResults") maxResults: Int = 25
    ): Response<YouTubeSearchResponse>
}