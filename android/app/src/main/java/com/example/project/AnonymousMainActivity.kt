package com.example.project

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class AnonymousMainActivity : AppCompatActivity() {

    // private lateinit var homeIcon: ImageView // homeIcon 변수 삭제
    private lateinit var myPageIcon: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_anonymous_main)

        // UI 요소 초기화
        initViews()

        // 네비게이션 설정
        setupNavigation()
    }

    private fun initViews() {
        // homeIcon = findViewById(R.id.homeicon) // homeicon을 찾는 코드 삭제
        myPageIcon = findViewById(R.id.mypageicon)
    }

    private fun setupNavigation() {
        // homeIcon.setOnClickListener { // homeIcon 이벤트 리스너 삭제
        //     // 현재 페이지이므로 아무 동작 안함
        //     Log.d("AnonymousMainActivity", "홈 버튼 클릭 (현재 페이지)")
        // }

        myPageIcon.setOnClickListener {
            Log.d("AnonymousMainActivity", "마이페이지 버튼 클릭 -> 로그인 화면으로 이동")
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }
}
