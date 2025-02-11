package com.klavs.bindle.data.routes

import kotlinx.serialization.Serializable

@Serializable
data object Events

@Serializable
data class EventPage(val eventId: String)

@Serializable
data class EditEvent(val event: String)

@Serializable
data class EventChat(val eventId: String, val numParticipants: Int = -1)