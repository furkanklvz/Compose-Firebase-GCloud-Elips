package com.klavs.bindle.data.entity.message

data class MessageFirestore(
    val id: String? = null,
    val message: String = "",
    val senderUid: String = "",
    val timestamp: Any? = null,
    val senderUsername: String = ""
)
