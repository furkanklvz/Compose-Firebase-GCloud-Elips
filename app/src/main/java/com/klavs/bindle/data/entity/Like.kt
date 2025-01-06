package com.klavs.bindle.data.entity

data class Like(
    val uid: String ="",
    val likedUsername: String = "",
    val likedUserPictureUrl: String?=null,
    val postId: String = "",
)
