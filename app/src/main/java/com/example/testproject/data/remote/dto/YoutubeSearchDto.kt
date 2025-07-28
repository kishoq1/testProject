package com.example.testproject.data.remote.dto


import com.google.gson.annotations.SerializedName

data class YouTubeSearchResponse(val items: List<VideoItemDto>)
data class VideoItemDto(val id: ResourceIdDto, val snippet: VideoSnippetDto)
data class ResourceIdDto(val videoId: String)
data class VideoSnippetDto(val title: String, val thumbnails: ThumbnailsDto)
data class ThumbnailsDto(@SerializedName("medium") val mediumQuality: ThumbnailInfoDto)
data class ThumbnailInfoDto(val url: String)