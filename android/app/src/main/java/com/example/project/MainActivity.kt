package com.example.project

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.project.util.SessionManager

class MainActivity : AppCompatActivity() {
    private lateinit var sessionManager: SessionManager
    
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 로그인 상태 확인
        sessionManager = SessionManager.getInstance(this)
        checkLoginStatus()
        
        try {
            Log.d("MainActivity", "onCreate 시작")
            setContentView(R.layout.activity_main)
            Log.d("MainActivity", "setContentView 완료")

            val liveTalkButton = findViewById<Button>(R.id.liveTalkButton)
            Log.d("MainActivity", "버튼 찾기 완료")
            
            liveTalkButton.setOnClickListener {
                try {
                    Log.d("MainActivity", "버튼 클릭됨")
                    val intent = Intent(this, LiveTalkActivity::class.java)
                    startActivity(intent)
                } catch (e: Exception) {
                    Log.e("MainActivity", "버튼 클릭 오류", e)
                    Toast.makeText(this, "오류: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
            Log.d("MainActivity", "onCreate 완료")

            val mypagebtn = findViewById<Button>(R.id.mypagebtn)

            mypagebtn.setOnClickListener{
                try {
                    val intent = Intent(this, MyPage::class.java)
                    startActivity(intent)
                } catch (e : Exception) {
                    Toast.makeText(this, "오류: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "onCreate 오류", e)
            Toast.makeText(this, "앱 시작 오류: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
    
    private fun checkLoginStatus() {
        sessionManager.checkSession { isValid ->
            if (!isValid) {
                // 로그인이 필요한 경우 LoginActivity로 이동
                runOnUiThread {
                    Log.d("MainActivity", "로그인 필요 - LoginActivity로 이동")
                    val intent = Intent(this, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                    finish()
                }
            } else {
                Log.d("MainActivity", "로그인 상태 확인됨")
            }
        }
    }
}