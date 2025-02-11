package com.klavs.bindle.helper.startdestination

import android.content.Intent
import com.klavs.bindle.data.routes.CommunityPage
import com.klavs.bindle.data.routes.EventChat
import com.klavs.bindle.data.routes.EventPage
import com.klavs.bindle.data.routes.Post

class StartDestinationProviderImpl : StartDestinationProvider {
    override fun determineStartDestination(intent: Intent?): Any? {
        return when (intent?.action) {
            "OPEN_EVENT" -> {
                val eventId = intent.getStringExtra("eventId")
                eventId?.let { EventPage(eventId) }
            }
            "OPEN_CHAT"->{
                val eventId = intent.getStringExtra("eventId")
                eventId?.let { EventChat(eventId, -1) }
            }
            "OPEN_COMMUNITY"->{
                val communityId = intent.getStringExtra("communityId")
                communityId?.let { CommunityPage(communityId) }
            }
            "OPEN_POST"->{
                val communityId = intent.getStringExtra("communityId")
                val postId = intent.getStringExtra("postId")
                if (communityId != null && postId != null) {
                    Post(communityId, postId)
                } else {
                    null
                }
            }

            else -> null

        }
    }
}