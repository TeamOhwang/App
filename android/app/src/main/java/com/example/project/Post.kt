package com.example.project

data class Post(
    val username: String,
    val profileImageRes: Int,
    val imageRes: Int,
    val likeCount: Int,
    val description: String,
    val recipeTitle: String,
    val recipeContent: String,
    val comments: List<Comment>
)
