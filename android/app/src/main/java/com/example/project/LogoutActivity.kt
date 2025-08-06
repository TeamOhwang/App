package com.example.project

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.project.util.SessionManager


class LogoutActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager
    private lateinit var tvWelcome: TextView
    private lateinit var tvUserEmail: TextView
    private lateinit var btnLogout: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_logout)

        Log.d("LogoutActivity", "LogoutActivity 시작")

        // SessionManager 인스턴스 생성
        sessionManager = SessionManager.getInstance(this)

        initViews()
        loadUserInfo()
        setupClickListeners()
    }

    private fun initViews() {
        tvWelcome = findViewById(R.id.tvWelcome)
        tvUserEmail = findViewById(R.id.tvUserEmail)
        btnLogout = findViewById(R.id.btnLogout)

        // 초기 상태 설정
        tvWelcome.text = "사용자 정보를 불러오는 중..."
        tvUserEmail.text = ""
        btnLogout.isEnabled = false
    }

    private fun loadUserInfo() {
        Log.d("LogoutActivity", "사용자 정보 로딩 시작")

        // 현재 로그인된 사용자 정보 표시
        sessionManager.getCurrentUser { success, message, userInfo ->
            runOnUiThread {
                if (success && userInfo != null) {
                    val nickname = userInfo["nickname"] as String
                    val email = userInfo["email"] as String

                    Log.d("LogoutActivity", "사용자 정보 로딩 성공: $nickname, $email")

                    tvWelcome.text = "안녕하세요, ${nickname}님!"
                    tvUserEmail.text = "이메일: $email"

                    btnLogout.isEnabled = true

                } else {
                    Log.e("LogoutActivity", "사용자 정보 로딩 실패: $message")
                    tvWelcome.text = "로그인 정보를 찾을 수 없습니다"
                    tvUserEmail.text = message ?: "알 수 없는 오류"

                    Toast.makeText(this, "로그인이 필요합니다", Toast.LENGTH_SHORT).show()
                    moveToLoginActivity()
                }
            }
        }
    }

    private fun setupClickListeners() {
        btnLogout.setOnClickListener {
            showLogoutDialog()
        }
    }

    private fun showLogoutDialog() {
        AlertDialog.Builder(this)
            .setTitle("로그아웃")
            .setMessage("정말 로그아웃 하시겠습니까?")
            .setPositiveButton("로그아웃") { _, _ ->
                performLogout()
            }
            .setNegativeButton("취소", null)
            .show()
    }

    private fun performLogout() {
        Log.d("LogoutActivity", "로그아웃 시작")

        btnLogout.isEnabled = false
        btnLogout.text = "로그아웃 중..."

        sessionManager.logout { success, message ->
            runOnUiThread {
                Log.d("LogoutActivity", "로그아웃 완료: success=$success, message=$message")

                btnLogout.isEnabled = true
                btnLogout.text = "로그아웃"

                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                moveToLoginActivity()
            }
        }
    }

    private fun moveToLoginActivity() {
        Log.d("LogoutActivity", "LoginActivity로 이동")

        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }

    override fun onResume() {
        super.onResume()
        Log.d("LogoutActivity", "onResume - 세션 확인")

        // 화면이 다시 보일 때마다 세션 확인
        sessionManager.checkSession { isValid ->
            runOnUiThread {
                if (!isValid) {
                    Log.d("LogoutActivity", "세션 만료 감지")
                    Toast.makeText(this, "세션이 만료되었습니다", Toast.LENGTH_SHORT).show()
                    moveToLoginActivity()
                } else {
                    Log.d("LogoutActivity", "세션 유효")
                }
            }
        }
    }
}