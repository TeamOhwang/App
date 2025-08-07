package com.example.project

data class Message(
    val sender: String,
    val content: String,
    val timestamp: String,
    val messageType: String = "text", // "text" 또는 "image"
    val imageUrl: String? = null
)