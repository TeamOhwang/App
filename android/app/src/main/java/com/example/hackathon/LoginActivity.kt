package com.example.hackathon

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.hackathon.util.SessionManager
import com.example.project.MainActivity
import com.example.project.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var sessionManager: SessionManager
    private val RC_SIGN_IN = 1001

    // 간단한 쿠키 저장소
    private val cookieStore = mutableListOf<Cookie>()

    // 커스텀 CookieJar
    private val cookieJar = object : CookieJar {
        override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
            cookieStore.addAll(cookies)
        }

        override fun loadForRequest(url: HttpUrl): List<Cookie> {
            return cookieStore.filter { it.matches(url) }
        }
    }

    private val client = OkHttpClient.Builder()
        .cookieJar(cookieJar)
        .build()

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

        findViewById<Button>(R.id.btnGoogleLogin).setOnClickListener {
            signIn()
        }
    }

    private fun checkExistingSession() {
        sessionManager.checkSession { isValid ->
            if (isValid) {
                // 이미 로그인된 상태 - 메인으로 이동
                runOnUiThread {
                    moveToMainActivity()
                }
            }
            // 세션이 없으면 로그인 화면 그대로 표시
        }
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

                    // 서버로 사용자 정보 전송하여 세션 생성
                    sendUserDataToServer(uid, email, name, photoUrl)

                } else {
                    Log.w("GoogleLogin", "Firebase 로그인 실패", task.exception)
                    Toast.makeText(this, "인증 실패", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun sendUserDataToServer(uid: String, email: String, name: String, photoUrl: String) {
        Thread {
            try {
                val json = JSONObject().apply {
                    put("account_code", uid)
                    put("email", email)
                    put("nickname", name)
                    put("profileImage", photoUrl)
                }

                val requestBody = json.toString()
                    .toRequestBody("application/json".toMediaType())

                val request = Request.Builder()
                    .url("http://10.0.2.2:8081/api/users/social-login")
                    .post(requestBody)
                    .build()

                val response = client.newCall(request).execute()
                val responseBody = response.body?.string()
                Log.i("GoogleUser", "서버 응답: $responseBody")

                responseBody?.let { body ->
                    val jsonResponse = JSONObject(body)
                    if (jsonResponse.getBoolean("success")) {
                        // 세션 생성 성공
                        Log.i("GoogleUser", "세션 생성 완료")
                        val message = jsonResponse.getString("message")

                        runOnUiThread {
                            Toast.makeText(this@LoginActivity, message, Toast.LENGTH_SHORT).show()
                            moveToMainActivity()
                        }
                    } else {
                        runOnUiThread {
                            Toast.makeText(this@LoginActivity, "로그인 실패", Toast.LENGTH_SHORT).show()
                        }
                    }
                }

            } catch (e: Exception) {
                Log.e("GoogleUser", "서버 통신 실패", e)
                runOnUiThread {
                    Toast.makeText(this@LoginActivity, "서버 연결 실패", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }

    private fun moveToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }
}