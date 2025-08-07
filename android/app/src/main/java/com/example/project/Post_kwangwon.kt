package com.example.project

data class Post_kwangwon(
    val username: String,
    val profileImageRes: Int,
    val postImageRes: Int,
    var likes: Int,
    val content: String,
    val recipeTitle: String,
    val recipeContent: String,
    val comments: List<Comment>,
    var isLiked: Boolean = false
)
