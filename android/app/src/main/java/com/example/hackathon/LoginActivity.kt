package com.example.hackathon

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.project.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private val RC_SIGN_IN = 1001
    private val client = OkHttpClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

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

                    // 📌 로그 출력
                    Log.i("GoogleUser", "아이디 : $uid")
                    Log.i("GoogleUser", "이름 : $name")
                    Log.i("GoogleUser", "이메일 : $email")
                    Log.i("GoogleUser", "프로필 이미지 : $photoUrl")

                    // 📌 MySQL 서버로 전송
                    sendUserDataToServer(uid, name, email, photoUrl)

                    Toast.makeText(this, "환영합니다, $name", Toast.LENGTH_SHORT).show()
                } else {
                    Log.w("GoogleLogin", "Firebase 로그인 실패", task.exception)
                    Toast.makeText(this, "Firebase 로그인 실패", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun sendUserDataToServer(uid: String, name: String, email: String, photoUrl: String) {
        Thread {
            try {
                val json = JSONObject().apply {
                    put("uid", uid)
                    put("name", name)
                    put("email", email)
                    put("photoUrl", photoUrl)
                }

                val requestBody = json.toString()
                    .toRequestBody("application/json".toMediaType())

                val request = Request.Builder()
                    .url("http://10.0.2.2:8080/api/user/save") // 서버 URL
                    .post(requestBody)
                    .build()

                val response = client.newCall(request).execute()
                Log.i("GoogleUser", "서버 응답: ${response.body?.string()}")
            } catch (e: Exception) {
                Log.e("GoogleUser", "서버 전송 실패", e)
            }
        }.start()
    }
}
