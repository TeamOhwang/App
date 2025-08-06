package com.example.project

data class Post(
    val id: Long,
    val imgUrl: String,
    val likes: Int,
    val commentsNum: Int,
    val isMultiple: Boolean = false
)
