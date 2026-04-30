package com.chiko0085.testgym

data class ChatMessage(
    val senderId: String, // "admin" or member.id
    val receiverId: String,
    val message: String,
    val timestamp: Long = System.currentTimeMillis()
)
