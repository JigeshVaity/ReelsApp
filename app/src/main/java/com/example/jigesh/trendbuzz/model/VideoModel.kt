package com.example.jigesh.trendbuzz.model

import com.google.firebase.Timestamp

data class VideoModel(
    var videoId: String = "",
    var title: String = "",
    var url: String = "",
    var uploaderId: String = "",
    var createdTime: Timestamp = Timestamp.now(),
    var likeCount: Long = 0,
    var dislikeCount: Long = 0,
    var like: List<String> = emptyList(),
    var dislike: List<String> = emptyList()
)
