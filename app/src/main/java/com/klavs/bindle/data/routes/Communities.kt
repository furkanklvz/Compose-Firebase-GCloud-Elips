package com.klavs.bindle.data.routes

import kotlinx.serialization.Serializable

@Serializable
data object Communities

@Serializable
data object CreateCommunity

@Serializable
data object CommunitiesGraph

@Serializable
data class CommunityPage(val communityId: String)

@Serializable
data class CreatePost(val communityId: String)

@Serializable
data class CommunityEvents(val communityId: String, val communityName:String)

@Serializable
data class Post(val communityId: String, val postId: String)