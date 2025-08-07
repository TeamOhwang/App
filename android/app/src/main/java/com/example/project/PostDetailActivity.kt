package com.example.project

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class PostDetailActivity : AppCompatActivity() {

    private lateinit var homeIcon: ImageView
    private lateinit var newPostIcon: ImageView
    private lateinit var chatIcon: ImageView
    private lateinit var myPageIcon: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post_detail)

        // UI 요소 초기화
        initViews()
        
        // 하단 네비게이션 설정
        setupBottomNavigation()
    }

    private fun initViews() {
        // 하단 네비게이션 아이콘들
        homeIcon = findViewById(R.id.homeicon)
        newPostIcon = findViewById(R.id.newposticon)
        chatIcon = findViewById(R.id.chaticon)
        myPageIcon = findViewById(R.id.mypageicon)
    }

    private fun setupBottomNavigation() {
        homeIcon.setOnClickListener {
            // 현재 페이지이므로 아무 동작 안함
            Log.d("PostDetailActivity", "홈 버튼 클릭 (현재 페이지)")
        }

        newPostIcon.setOnClickListener {
            // TODO: 새 게시물 작성 화면으로 이동
            Log.d("PostDetailActivity", "새 게시물 버튼 클릭")
        }

        chatIcon.setOnClickListener {
            val intent = Intent(this, LiveTalkActivity::class.java)
            startActivity(intent)
        }

        myPageIcon.setOnClickListener {
            val intent = Intent(this, MyPageActivity::class.java)
            startActivity(intent)
        }
    }
}