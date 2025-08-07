package com.example.project.util

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.project.R
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class SessionManager private constructor(private val context: Context) {

    // SharedPreferences를 사용한 영구 쿠키 저장
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("session_cookies", Context.MODE_PRIVATE)

    // 메모리 쿠키 저장소 (중복 방지를 위한 Map 사용)
    private val cookieStore = mutableMapOf<String, Cookie>()

    // 익명 모드 플래그
    private var isAnonymousMode = false

    init {
        // 앱 시작 시 저장된 쿠키 복원
        loadCookiesFromStorage()
    }

    // 커스텀 CookieJar (개선됨)
    private val cookieJar = object : CookieJar {
        override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
            cookies.forEach { cookie ->
                val key = "${cookie.name}_${cookie.domain}"
                val oldCookie = cookieStore[key]

                // 기존 쿠키와 비교하여 변경사항 로깅
                if (oldCookie?.value != cookie.value) {
                    Log.d("SessionManager", "쿠키 업데이트: ${cookie.name} = ${cookie.value} (이전: ${oldCookie?.value})")
                }

                cookieStore[key] = cookie
            }
            saveCookiesToStorage()
        }

        override fun loadForRequest(url: HttpUrl): List<Cookie> {
            val validCookies = cookieStore.values.filter { cookie ->
                cookie.matches(url) && !cookie.expiresAt.let { it > 0 && it < System.currentTimeMillis() }
            }

            Log.d("SessionManager", "URL $url 에 대한 쿠키 ${validCookies.size}개 로드")
            validCookies.forEach { cookie ->
                Log.d("SessionManager", "  - ${cookie.name} = ${cookie.value}")
            }

            return validCookies
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

    // ==================== 쿠키 저장/복원 메소드 ====================

    private fun saveCookiesToStorage() {
        val editor = sharedPreferences.edit()
        cookieStore.forEach { (key, cookie) ->
            val cookieString = "${cookie.name}|${cookie.value}|${cookie.domain}|${cookie.path}|${cookie.expiresAt}|${cookie.secure}|${cookie.httpOnly}"
            editor.putString(key, cookieString)
        }
        editor.apply()
        Log.d("SessionManager", "쿠키 ${cookieStore.size}개 저장 완료")
    }

    private fun loadCookiesFromStorage() {
        val allEntries = sharedPreferences.all
        allEntries.forEach { (key, value) ->
            if (value is String) {
                try {
                    val parts = value.split("|")
                    if (parts.size >= 7) {
                        val cookie = Cookie.Builder()
                            .name(parts[0])
                            .value(parts[1])
                            .domain(parts[2])
                            .path(parts[3])
                            .apply {
                                val expiresAt = parts[4].toLong()
                                if (expiresAt > 0) expiresAt(expiresAt)
                                if (parts[5].toBoolean()) secure()
                                if (parts[6].toBoolean()) httpOnly()
                            }
                            .build()

                        // 만료되지 않은 쿠키만 복원
                        if (cookie.expiresAt == 0L || cookie.expiresAt > System.currentTimeMillis()) {
                            cookieStore[key] = cookie
                        }
                    }
                } catch (e: Exception) {
                    Log.e("SessionManager", "쿠키 복원 실패: $key", e)
                }
            }
        }
        Log.d("SessionManager", "쿠키 ${cookieStore.size}개 복원 완료")
    }

    // ==================== 익명 모드 관련 메소드 ====================

    fun setAnonymousMode(anonymous: Boolean) {
        isAnonymousMode = anonymous
        if (anonymous) {
            Log.d("SessionManager", "익명 모드로 설정됨")
        } else {
            Log.d("SessionManager", "익명 모드 해제됨")
        }
    }

    fun isAnonymousMode(): Boolean {
        return isAnonymousMode
    }

    // ==================== 쿠키 접근 메소드들 (개선됨) ====================

    fun getAllCookies(): List<Cookie> {
        return cookieStore.values.toList()
    }

    fun getCookie(name: String): Cookie? {
        return cookieStore.values.find { it.name == name }
    }

    fun getCookiesForUrl(url: String): List<Cookie> {
        val httpUrl = try {
            url.toHttpUrlOrNull()
        } catch (e: Exception) {
            null
        } ?: return emptyList()
        return cookieStore.values.filter { it.matches(httpUrl) }
    }

    // 세션 쿠키 검색 로직 개선
    fun getSessionCookie(): Cookie? {
        // 우선순위: JSESSIONID > sessionid > session 포함
        return getCookie("JSESSIONID")
            ?: getCookie("sessionid")
            ?: getCookie("SESSION")
            ?: cookieStore.values.find { it.name.contains("session", ignoreCase = true) }
    }

    fun getCookieHeader(url: String): String {
        val cookies = getCookiesForUrl(url)
        return cookies.joinToString("; ") { "${it.name}=${it.value}" }
    }

    fun getHttpClient(): OkHttpClient {
        return client
    }

    fun addCookie(cookie: Cookie) {
        val key = "${cookie.name}_${cookie.domain}"
        cookieStore[key] = cookie
        saveCookiesToStorage()
        Log.d("SessionManager", "쿠키 수동 추가: ${cookie.name}=${cookie.value}")
    }

    fun removeCookie(name: String) {
        val keysToRemove = cookieStore.keys.filter { it.startsWith("${name}_") }
        val removed = keysToRemove.isNotEmpty()

        keysToRemove.forEach { key ->
            cookieStore.remove(key)
            sharedPreferences.edit().remove(key).apply()
        }

        if (removed) {
            Log.d("SessionManager", "쿠키 삭제: $name")
        }
    }

    fun getSessionId(): String? {
        return getSessionCookie()?.value
    }

    fun getCookieCount(): Int {
        return cookieStore.size
    }

    // 세션 상태 디버깅 메소드 추가
    fun debugSessionState() {
        Log.d("SessionManager", "=== 세션 상태 디버그 ===")
        Log.d("SessionManager", "익명 모드: $isAnonymousMode")
        Log.d("SessionManager", "총 쿠키 개수: ${cookieStore.size}")
        Log.d("SessionManager", "세션 쿠키: ${getSessionCookie()?.let { "${it.name}=${it.value}" } ?: "없음"}")
        cookieStore.values.forEach { cookie ->
            Log.d("SessionManager", "  쿠키: ${cookie.name}=${cookie.value} (도메인: ${cookie.domain})")
        }
        Log.d("SessionManager", "========================")
    }

    // ==================== 인증 관련 메소드들 ====================

    fun socialLogin(uid: String, email: String, name: String, photoUrl: String,
                    callback: (Boolean, String) -> Unit) {
        setAnonymousMode(false)

        Thread {
            try {
                Log.d("SessionManager", "소셜 로그인 시작 - 현재 쿠키 개수: ${getCookieCount()}")
                debugSessionState()

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
                Log.d("SessionManager", "응답 후 쿠키 개수: ${getCookieCount()}")
                debugSessionState()

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

    fun getCurrentUser(callback: (Boolean, String?, Map<String, Any>?) -> Unit) {
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
                Log.d("SessionManager", "사용자 정보 조회 시작")
                debugSessionState()

                val request = Request.Builder()
                    .url("$baseUrl/users/current")
                    .get()
                    .build()

                val response = client.newCall(request).execute()
                val responseBody = response.body?.string()

                Log.d("SessionManager", "현재 사용자 조회 응답: $responseBody")

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

    fun checkSession(callback: (Boolean) -> Unit) {
        if (isAnonymousMode) {
            Log.d("SessionManager", "익명 모드 - 세션 유효함으로 처리")
            callback(true)
            return
        }

        Thread {
            try {
                Log.d("SessionManager", "세션 확인 시작")
                debugSessionState()

                val request = Request.Builder()
                    .url("$baseUrl/users/check-session")
                    .get()
                    .build()

                val response = client.newCall(request).execute()
                val responseBody = response.body?.string()

                Log.d("SessionManager", "세션 확인 응답: $responseBody")

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

    fun logout(callback: (Boolean, String) -> Unit) {
        if (isAnonymousMode) {
            setAnonymousMode(false)
            Log.d("SessionManager", "익명 모드 해제")
            callback(true, "로그아웃 완료")
            return
        }

        Thread {
            try {
                Log.d("SessionManager", "로그아웃 시작 - 현재 쿠키 개수: ${getCookieCount()}")

                val request = Request.Builder()
                    .url("$baseUrl/users/logout")
                    .post("{}".toRequestBody("application/json".toMediaType()))
                    .build()

                val response = client.newCall(request).execute()
                val responseBody = response.body?.string()

                Log.d("SessionManager", "로그아웃 응답: $responseBody")

                // 서버 응답과 관계없이 로컬 쿠키 삭제
                cookieStore.clear()
                sharedPreferences.edit().clear().apply()
                Log.d("SessionManager", "로컬 쿠키 삭제 완료, 현재 쿠키 개수: ${getCookieCount()}")

                if (response.isSuccessful && responseBody != null) {
                    val jsonResponse = JSONObject(responseBody)
                    callback(true, jsonResponse.optString("message", "로그아웃 완료"))
                } else {
                    callback(true, "로그아웃 완료")
                }

            } catch (e: Exception) {
                Log.e("SessionManager", "로그아웃 실패", e)
                // 네트워크 오류 시에도 로컬 쿠키는 삭제
                cookieStore.clear()
                sharedPreferences.edit().clear().apply()
                callback(true, "로그아웃 완료")
            }
        }.start()
    }
}