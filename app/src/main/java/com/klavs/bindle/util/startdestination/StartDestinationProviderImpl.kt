package com.klavs.bindle.util.startdestination

import android.content.Intent

class StartDestinationProviderImpl : StartDestinationProvider {
    override fun determineStartDestination(intent: Intent?): String? {
        return when (intent?.action) {
            "OPEN_EVENT" -> {
                val eventId = intent.getStringExtra("eventId")
                eventId?.let { "event_page/$eventId" }
            }
            "OPEN_CHAT"->{
                val eventId = intent.getStringExtra("eventId")
                eventId?.let { "event_chat/$eventId/${-1}" }
            }
            "OPEN_COMMUNITY"->{
                val communityId = intent.getStringExtra("communityId")
                communityId?.let { "community_page/$communityId" }
            }
            "OPEN_POST"->{
                val communityId = intent.getStringExtra("communityId")
                val postId = intent.getStringExtra("postId")
                if (communityId != null && postId != null) {
                    "post/$communityId/$postId"
                } else {
                    null
                }
            }

            else -> null

        }
    }
}