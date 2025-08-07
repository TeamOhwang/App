package com.example.project

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.project.util.SessionManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var sessionManager: SessionManager
    private val RC_SIGN_IN = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        sessionManager = SessionManager.getInstance(this)

        // 앱 시작 시 세션 확인 (자동 로그인)
        checkExistingSession()

        // Firebase Auth 초기화
        auth = FirebaseAuth.getInstance()

        // Google 로그인 옵션
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        findViewById<ImageView>(R.id.btnGoogleLogin).setOnClickListener {
            signIn()
        }

        // 익명 로그인 버튼 처리 수정
        findViewById<Button>(R.id.btnAnonymousStart).setOnClickListener {
            startAnonymousMode()
        }
    }

    private fun checkExistingSession() {
        sessionManager.checkSession { isValid ->
            if (isValid) {
                // 이미 로그인된 상태 (일반 로그인 또는 익명 모드) - MainActivity로 이동
                runOnUiThread {
                    Log.d("LoginActivity", "기존 세션 발견 - MainActivity로 이동")
                    moveToMainActivity()
                }
            } else {
                Log.d("LoginActivity", "세션 없음 - 로그인 화면 표시")
            }
        }
    }

    // 익명 모드 시작
    private fun startAnonymousMode() {
        sessionManager.setAnonymousMode(true)
        Log.d("LoginActivity", "익명 모드로 MainActivity 이동")
        moveToMainActivityForAnonymous()
    }

    private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account)
            } catch (e: ApiException) {
                Log.w("GoogleLogin", "Google sign in failed", e)
                Toast.makeText(this, "Google 로그인 실패", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    val uid = user?.uid ?: ""
                    val name = user?.displayName ?: ""
                    val email = user?.email ?: ""
                    val photoUrl = user?.photoUrl?.toString() ?: ""

                    Log.i("GoogleUser", "Firebase 인증 성공")
                    Log.i("GoogleUser", "account_code: $uid")
                    Log.i("GoogleUser", "nickname: $name")
                    Log.i("GoogleUser", "email: $email")
                    Log.i("GoogleUser", "profileImage: $photoUrl")

                    // SessionManager를 통해 서버로 사용자 정보 전송
                    // socialLogin 메소드에서 자동으로 익명 모드가 해제됩니다
                    sendUserDataToServer(uid, email, name, photoUrl)

                } else {
                    Log.w("GoogleLogin", "Firebase 로그인 실패", task.exception)
                    Toast.makeText(this, "인증 실패", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun sendUserDataToServer(uid: String, email: String, name: String, photoUrl: String) {
        // SessionManager의 socialLogin 메소드 사용
        sessionManager.socialLogin(uid, email, name, photoUrl) { success, message ->
            runOnUiThread {
                if (success) {
                    Log.i("LoginActivity", "서버 로그인 성공: $message")
                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                    moveToMainActivity()
                } else {
                    Log.e("LoginActivity", "서버 로그인 실패: $message")
                    Toast.makeText(this, "로그인 실패: $message", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun moveToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }

    // 익명 모드용 MainActivity 이동 (플래그 없음)
    private fun moveToMainActivityForAnonymous() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        // finish()를 호출하지 않음 - 뒤로가기로 로그인 화면 복귀 가능
    }
}