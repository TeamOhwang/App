package com.example.project

data class Post(
    val id: Long = 0,                   // 게시물 ID 추가
    val username: String,
    val profileImageRes: Int,
    val imageRes: Int,
    val likeCount: Int,
    val description: String,
    val recipeTitle: String,
    val recipeContent: String,
    val comments: List<Comment>,
    val imgUrl: String? = null,         // ← nullable (꼭 = null로 디폴트 둘 필요는 없지만 있으면 더 안전)
    val profileImgUrl: String? = null
)
