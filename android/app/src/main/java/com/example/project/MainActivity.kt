package com.example.project

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
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
        } catch (e: Exception) {
            Log.e("MainActivity", "onCreate 오류", e)
            Toast.makeText(this, "앱 시작 오류: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}