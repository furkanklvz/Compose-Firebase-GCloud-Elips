package com.klavs.bindle.data.routes

import kotlinx.serialization.Serializable

@Serializable
data object MapGraph

@Serializable
data object Map

@Serializable
data class CreateEvent(val latitude: String, val longitude: String)