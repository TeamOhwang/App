package com.example.project

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.project.util.SessionManager

class MyPageActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager
    private lateinit var nicknameTextView: TextView
    private lateinit var postCountTextView: TextView
    private lateinit var bookmarkCountTextView: TextView
    private lateinit var postRecyclerView: RecyclerView
    private lateinit var homeIcon: ImageView
    private lateinit var newPostIcon: ImageView
    private lateinit var chatIcon: ImageView
    private lateinit var myPageIcon: ImageView
    private lateinit var logoutButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_page)

        // SessionManager 초기화
        sessionManager = SessionManager.getInstance(this)

        // UI 요소 초기화
        initViews()
        
        // 사용자 정보 로드
        loadUserInfo()
        
        // 하단 네비게이션 설정
        setupBottomNavigation()
        
        // 로그아웃 버튼 설정
        setupLogoutButton()
    }



    private fun initViews() {
        nicknameTextView = findViewById(R.id.nickname)
        postCountTextView = findViewById(R.id.num1)
        bookmarkCountTextView = findViewById(R.id.num2)
        postRecyclerView = findViewById(R.id.postlist)
        
        // 하단 네비게이션 아이콘들
        homeIcon = findViewById(R.id.homeicon)
        newPostIcon = findViewById(R.id.newposticon)
        chatIcon = findViewById(R.id.chaticon)
        myPageIcon = findViewById(R.id.mypageicon)
        
        // 로그아웃 버튼
        logoutButton = findViewById(R.id.btnLogout)
        
        // RecyclerView 설정 (3열 그리드)
        postRecyclerView.layoutManager = GridLayoutManager(this, 3)
    }

    private fun loadUserInfo() {
        sessionManager.getCurrentUser { success, error, userInfo ->
            if (success && userInfo != null) {
                runOnUiThread {
                    val nickname = userInfo["nickname"] as? String ?: "사용자"
                    nicknameTextView.text = nickname
                    Log.d("MyPageActivity", "사용자 정보 로드 성공: $nickname")
                }
            } else {
                Log.e("MyPageActivity", "사용자 정보 로드 실패: $error")
                runOnUiThread {
                    nicknameTextView.text = "사용자"
                }
            }
        }
    }

    private fun setupBottomNavigation() {
        
        homeIcon.setOnClickListener {
            val intent = Intent(this, PostDetailActivity::class.java)
            startActivity(intent)
        }

        newPostIcon.setOnClickListener {
            val intent = Intent(this, NewPostActivity::class.java)
            startActivity(intent)
        }

        chatIcon.setOnClickListener {
            val intent = Intent(this, LiveTalkActivity::class.java)
            startActivity(intent)
        }

        myPageIcon.setOnClickListener {
            // 현재 페이지이므로 아무 동작 안함
            Log.d("MyPageActivity", "마이페이지 버튼 클릭 (현재 페이지)")
        }
    }

    private fun setupLogoutButton() {
        logoutButton.setOnClickListener {
            performLogout()
        }
    }

    private fun performLogout() {
        Log.d("MyPageActivity", "로그아웃 시작")
        
        // SessionManager를 통해 로그아웃 처리
        sessionManager.logout { success, message ->
            runOnUiThread {
                if (success) {
                    Log.d("MyPageActivity", "로그아웃 성공: $message")
                    Toast.makeText(this, "로그아웃되었습니다.", Toast.LENGTH_SHORT).show()
                    
                    // LoginActivity로 이동하고 현재 액티비티 스택 클리어
                    val intent = Intent(this, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                    finish()
                } else {
                    Log.e("MyPageActivity", "로그아웃 실패: $message")
                    Toast.makeText(this, "로그아웃 실패: $message", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}