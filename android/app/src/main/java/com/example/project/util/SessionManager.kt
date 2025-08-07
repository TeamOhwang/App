package com.example.project.util

import android.content.Context
import android.util.Log
import com.example.project.R
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class SessionManager private constructor(private val context: Context) {

    // 간단한 쿠키 저장소
    private val cookieStore = mutableListOf<Cookie>()

    // 익명 모드 플래그 추가
    private var isAnonymousMode = false

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

    private val baseUrl: String
        get() {
            val port = context.getString(R.string.server_port)
            val serverIp = context.getString(R.string.server_ip)
            val baseIp = if (serverIp == "auto") {
                "10.0.2.2"
            } else {
                serverIp
            }
            return "http://$baseIp:$port/api"
        }

    companion object {
        @Volatile
        private var INSTANCE: SessionManager? = null

        fun getInstance(context: Context): SessionManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: SessionManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    // 익명 모드 설정
    fun setAnonymousMode(anonymous: Boolean) {
        isAnonymousMode = anonymous
        if (anonymous) {
            Log.d("SessionManager", "익명 모드로 설정됨")
        } else {
            Log.d("SessionManager", "익명 모드 해제됨")
        }
    }

    // 익명 모드 확인
    fun isAnonymousMode(): Boolean {
        return isAnonymousMode
    }

    // 소셜 로그인
    fun socialLogin(uid: String, email: String, name: String, photoUrl: String,
                    callback: (Boolean, String) -> Unit) {
        // 익명 모드 해제
        setAnonymousMode(false)

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
                    .url("$baseUrl/users/social-login")
                    .post(requestBody)
                    .build()

                val response = client.newCall(request).execute()
                val responseBody = response.body?.string()

                Log.d("SessionManager", "소셜 로그인 응답: $responseBody")

                if (response.isSuccessful && responseBody != null) {
                    val jsonResponse = JSONObject(responseBody)
                    if (jsonResponse.getBoolean("success")) {
                        callback(true, jsonResponse.getString("message"))
                    } else {
                        callback(false, jsonResponse.getString("message"))
                    }
                } else {
                    callback(false, "서버 연결 실패")
                }

            } catch (e: Exception) {
                Log.e("SessionManager", "소셜 로그인 실패", e)
                callback(false, "네트워크 오류가 발생했습니다.")
            }
        }.start()
    }

    // 현재 로그인된 사용자 정보 조회 (수정)
    fun getCurrentUser(callback: (Boolean, String?, Map<String, Any>?) -> Unit) {
        // 익명 모드인 경우 익명 사용자 정보 반환
        if (isAnonymousMode) {
            val anonymousUserInfo = mapOf(
                "userId" to -1L,
                "email" to "anonymous@example.com",
                "nickname" to "익명 사용자",
                "profileImage" to "",
                "accountCode" to "anonymous"
            )
            Log.d("SessionManager", "익명 모드 - 익명 사용자 정보 반환")
            callback(true, null, anonymousUserInfo)
            return
        }

        Thread {
            try {
                val request = Request.Builder()
                    .url("$baseUrl/users/current")
                    .get()
                    .build()

                val response = client.newCall(request).execute()
                val responseBody = response.body?.string()

                Log.d("SessionManager", "현재 사용자 조회: $responseBody")

                if (response.isSuccessful && responseBody != null) {
                    val jsonResponse = JSONObject(responseBody)
                    if (jsonResponse.getBoolean("success")) {
                        val userInfo = mapOf(
                            "userId" to jsonResponse.getLong("userId"),
                            "email" to jsonResponse.getString("email"),
                            "nickname" to jsonResponse.getString("nickname"),
                            "profileImage" to jsonResponse.optString("profileImage", ""),
                            "accountCode" to jsonResponse.optString("accountCode", "")
                        )
                        callback(true, null, userInfo)
                    } else {
                        callback(false, jsonResponse.getString("message"), null)
                    }
                } else {
                    callback(false, "세션이 만료되었습니다.", null)
                }

            } catch (e: Exception) {
                Log.e("SessionManager", "사용자 정보 조회 실패", e)
                callback(false, "네트워크 오류가 발생했습니다.", null)
            }
        }.start()
    }

    // 세션 유효성 확인 (수정)
    fun checkSession(callback: (Boolean) -> Unit) {
        // 익명 모드인 경우 true 반환
        if (isAnonymousMode) {
            Log.d("SessionManager", "익명 모드 - 세션 유효함으로 처리")
            callback(true)
            return
        }

        Thread {
            try {
                val request = Request.Builder()
                    .url("$baseUrl/users/check-session")
                    .get()
                    .build()

                val response = client.newCall(request).execute()
                val responseBody = response.body?.string()

                Log.d("SessionManager", "세션 확인: $responseBody")

                if (response.isSuccessful && responseBody != null) {
                    val jsonResponse = JSONObject(responseBody)
                    callback(jsonResponse.getBoolean("success"))
                } else {
                    callback(false)
                }

            } catch (e: Exception) {
                Log.e("SessionManager", "세션 확인 실패", e)
                callback(false)
            }
        }.start()
    }

    // 로그아웃 (수정)
    fun logout(callback: (Boolean, String) -> Unit) {
        // 익명 모드인 경우 플래그만 해제
        if (isAnonymousMode) {
            setAnonymousMode(false)
            Log.d("SessionManager", "익명 모드 해제")
            callback(true, "로그아웃 완료")
            return
        }

        Thread {
            try {
                val request = Request.Builder()
                    .url("$baseUrl/users/logout")
                    .post("{}".toRequestBody("application/json".toMediaType()))
                    .build()

                val response = client.newCall(request).execute()
                val responseBody = response.body?.string()

                Log.d("SessionManager", "로그아웃 응답: $responseBody")

                if (response.isSuccessful && responseBody != null) {
                    val jsonResponse = JSONObject(responseBody)
                    if (jsonResponse.getBoolean("success")) {
                        // 서버 로그아웃 성공 시 로컬 쿠키도 삭제
                        cookieStore.clear()
                        Log.d("SessionManager", "로컬 쿠키 삭제 완료")
                        callback(true, jsonResponse.getString("message"))
                    } else {
                        callback(false, "서버 로그아웃 실패")
                    }
                } else {
                    // 서버 요청 실패 시에도 로컬 쿠키는 삭제
                    cookieStore.clear()
                    Log.d("SessionManager", "서버 요청 실패했지만 로컬 쿠키 삭제")
                    callback(true, "로그아웃 완료")
                }

            } catch (e: Exception) {
                Log.e("SessionManager", "로그아웃 실패", e)
                // 네트워크 오류 시에도 로컬 쿠키는 삭제
                cookieStore.clear()
                callback(true, "로그아웃 완료")
            }
        }.start()
    }
}