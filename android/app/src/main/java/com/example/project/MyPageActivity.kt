package com.example.project

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.project.util.SessionManager
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

class MyPageActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager
    private lateinit var client: OkHttpClient
    private val baseUrl = "http://192.168.219.180:8081/api"

    // 상단 UI
    private lateinit var profileImage: ImageView
    private lateinit var nickname: TextView
    private lateinit var postCount: TextView
    private lateinit var likeCount: TextView
    private lateinit var logoutBtn: Button
    private lateinit var editProfileBtn: Button
    private lateinit var withdrawBtn: Button

    // 두 개의 그리드
    private lateinit var gridMy: RecyclerView
    private lateinit var gridLiked: RecyclerView
    private val myAdapter = MyPageGridAdapter(mutableListOf())
    private val likedAdapter = MyPageGridAdapter(mutableListOf())

    // 하단 네비
    private lateinit var homeIcon: ImageView
    private lateinit var newPostIcon: ImageView
    private lateinit var chatIcon: ImageView
    private lateinit var myPageIcon: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_page)

        sessionManager = SessionManager.getInstance(this)
        client = sessionManager.getHttpClient() // JSESSIONID 쿠키 자동 전송

        initViews()
        setupBottomNavigation()
        setupLogout()
        setupEditProfile()
        setupWithdraw()

        // 최초 로드
        fetchProfile()
        fetchMyPostsCount()   // num1
        fetchMyPostsGrid()    // 내 게시글
        fetchLikedGrid()      // 좋아요한 게시글 + num2
    }

    override fun onResume() {
        super.onResume()
        fetchProfile()
        fetchMyPostsCount()
        fetchMyPostsGrid()
        fetchLikedGrid()
    }

    private fun initViews() {
        profileImage = findViewById(R.id.profile)
        nickname     = findViewById(R.id.nickname)
        postCount    = findViewById(R.id.num1)
        likeCount    = findViewById(R.id.num2)
        logoutBtn    = findViewById(R.id.btnLogout)
        editProfileBtn = findViewById(R.id.btnEditProfile)
        withdrawBtn    = findViewById(R.id.btnWithdraw)

        gridMy = findViewById(R.id.postlistMy)
        gridMy.layoutManager = GridLayoutManager(this, 3)
        gridMy.adapter = myAdapter

        gridLiked = findViewById(R.id.postlistLiked)
        gridLiked.layoutManager = GridLayoutManager(this, 3)
        gridLiked.adapter = likedAdapter

        homeIcon   = findViewById(R.id.homeicon)
        newPostIcon= findViewById(R.id.newposticon)
        chatIcon   = findViewById(R.id.chaticon)
        myPageIcon = findViewById(R.id.mypageicon)
    }

    // ───────────── API ─────────────

    private fun fetchProfile() {
        val req = Request.Builder().url("$baseUrl/mypage").get().build()
        client.newCall(req).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("MyPage", "프로필 실패", e)
            }
            override fun onResponse(call: Call, response: Response) {
                response.use {
                    val body = it.body?.string()
                    if (it.code == 401) return goLogin("세션이 만료되었습니다.")
                    if (!it.isSuccessful || body == null) return
                    try {
                        val json = JSONObject(body)
                        val nick = json.optString("nickname", "사용자")
                        val profileImgUrl = json.optString("profileImage", null)
                        runOnUiThread {
                            nickname.text = nick
                            if (!profileImgUrl.isNullOrEmpty()) {
                                Glide.with(this@MyPageActivity)
                                    .load(absoluteUrl(profileImgUrl))
                                    .circleCrop()
                                    .placeholder(R.drawable.userlogo)
                                    .into(profileImage)
                            } else profileImage.setImageResource(R.drawable.userlogo)
                        }
                    } catch (e: Exception) {
                        Log.e("MyPage", "프로필 파싱 오류", e)
                    }
                }
            }
        })
    }

    private fun fetchMyPostsCount() {
        val req = Request.Builder().url("$baseUrl/mypage/posts-with-likes").get().build()
        client.newCall(req).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread { postCount.text = "0" }
            }
            override fun onResponse(call: Call, response: Response) {
                response.use {
                    val body = it.body?.string()
                    if (it.code == 401) return goLogin("세션이 만료되었습니다.")
                    if (!it.isSuccessful || body == null) return runOnUiThread { postCount.text = "0" }
                    try {
                        val arr = JSONArray(body)
                        runOnUiThread { postCount.text = arr.length().toString() }
                    } catch (_: Exception) {
                        runOnUiThread { postCount.text = "0" }
                    }
                }
            }
        })
    }

    // 내 게시글 썸네일
    private fun fetchMyPostsGrid() {
        // 백엔드에 my-posts 없으면 posts-with-likes 사용
        val req = Request.Builder().url("$baseUrl/mypage/posts-with-likes").get().build()
        client.newCall(req).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread { myAdapter.update(emptyList()) }
            }
            override fun onResponse(call: Call, response: Response) {
                response.use {
                    val body = it.body?.string()
                    if (it.code == 401) return goLogin("세션이 만료되었습니다.")
                    if (!it.isSuccessful || body == null) return runOnUiThread { myAdapter.update(emptyList()) }

                    try {
                        val arr = JSONArray(body) // [{id|postId, imgUrl, ...}]
                        val ui = mutableListOf<GridItem>()
                        for (i in 0 until arr.length()) {
                            val o = arr.getJSONObject(i)
                            val id = o.optLong("id", o.optLong("postId", 0)) // ← 둘 다 대응
                            val img = o.optString("imgUrl", null)
                            val url = img?.takeIf { it.isNotBlank() && it != "null" }?.let { absoluteUrl(it) }
                            ui.add(GridItem(id, url))
                        }
                        runOnUiThread { myAdapter.update(ui) }
                    } catch (_: Exception) {
                        runOnUiThread { myAdapter.update(emptyList()) }
                    }
                }
            }
        })
    }

    // 좋아요한 게시글 썸네일 + num2
    private fun fetchLikedGrid() {
        val req = Request.Builder().url("$baseUrl/mypage/likes").get().build()
        client.newCall(req).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    likeCount.text = "0"
                    likedAdapter.update(emptyList())
                }
            }
            override fun onResponse(call: Call, response: Response) {
                response.use {
                    val body = it.body?.string()
                    if (it.code == 401) return goLogin("세션이 만료되었습니다.")
                    if (!it.isSuccessful || body == null) return runOnUiThread {
                        likeCount.text = "0"
                        likedAdapter.update(emptyList())
                    }
                    try {
                        val arr = JSONArray(body) // [{id,imgUrl}]
                        val ui = mutableListOf<GridItem>()
                        for (i in 0 until arr.length()) {
                            val o = arr.getJSONObject(i)
                            val id = o.optLong("id", 0)
                            val img = o.optString("imgUrl", null)
                            val url = img?.takeIf { it.isNotBlank() && it != "null" }?.let { absoluteUrl(it) }
                            ui.add(GridItem(id, url))
                        }
                        runOnUiThread {
                            likeCount.text = ui.size.toString()
                            likedAdapter.update(ui)
                        }
                    } catch (_: Exception) {
                        runOnUiThread {
                            likeCount.text = "0"
                            likedAdapter.update(emptyList())
                        }
                    }
                }
            }
        })
    }

    // 프로필 수정 (닉네임)
    private fun setupEditProfile() {
        editProfileBtn.setOnClickListener {
            val input = EditText(this).apply { hint = "새 닉네임" }
            AlertDialog.Builder(this)
                .setTitle("프로필 수정")
                .setView(input)
                .setPositiveButton("저장") { _, _ ->
                    val newNick = input.text.toString().trim()
                    if (newNick.isEmpty()) {
                        Toast.makeText(this, "닉네임을 입력하세요", Toast.LENGTH_SHORT).show()
                        return@setPositiveButton
                    }
                    val json = JSONObject().put("nickname", newNick)
                    val body = json.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
                    val req = Request.Builder().url("$baseUrl/mypage/profile").patch(body).build()
                    client.newCall(req).enqueue(object : Callback {
                        override fun onFailure(call: Call, e: IOException) {
                            runOnUiThread { Toast.makeText(this@MyPageActivity, "수정 실패: ${e.message}", Toast.LENGTH_SHORT).show() }
                        }
                        override fun onResponse(call: Call, response: Response) {
                            runOnUiThread {
                                if (response.isSuccessful) {
                                    Toast.makeText(this@MyPageActivity, "수정 완료", Toast.LENGTH_SHORT).show()
                                    fetchProfile()
                                } else if (response.code == 401) {
                                    goLogin("세션 만료")
                                } else {
                                    Toast.makeText(this@MyPageActivity, "수정 실패: ${response.code}", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    })
                }
                .setNegativeButton("취소", null)
                .show()
        }
    }

    // 로그아웃 처리 (함수 없어서 빨간 줄 뜨던 부분)
    private fun setupLogout() {
        logoutBtn.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("로그아웃")
                .setMessage("정말 로그아웃 하시겠습니까?")
                .setPositiveButton("예") { _, _ ->
                    // ✅ 백엔드 매핑: POST /api/users/logout
                    val req = Request.Builder()
                        .url("$baseUrl/users/logout")
                        .post(FormBody.Builder().build()) // 빈 바디 OK
                        .build()

                    client.newCall(req).enqueue(object : Callback {
                        override fun onFailure(call: Call, e: IOException) {
                            runOnUiThread {
                                Toast.makeText(this@MyPageActivity, "로그아웃 실패: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                        override fun onResponse(call: Call, response: Response) {
                            runOnUiThread {
                                if (response.isSuccessful) {
                                    // 서버 세션 무효화 성공 → 로컬 쿠키도 정리
                                    sessionManager.logout { _, _ -> }
                                    goLogin("로그아웃 되었습니다.")
                                } else if (response.code == 401) {
                                    goLogin("세션 만료")
                                } else {
                                    Toast.makeText(
                                        this@MyPageActivity,
                                        "로그아웃 실패: ${response.code}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }
                    })
                }
                .setNegativeButton("아니오", null)
                .show()
        }
    }



    private fun setupWithdraw() {
        withdrawBtn.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("회원 탈퇴")
                .setMessage("정말 탈퇴하시겠습니까?")
                .setPositiveButton("예") { _, _ ->
                    val req = Request.Builder().url("$baseUrl/mypage/withdraw").delete().build()
                    client.newCall(req).enqueue(object : Callback {
                        override fun onFailure(call: Call, e: IOException) {
                            runOnUiThread { Toast.makeText(this@MyPageActivity, "탈퇴 실패", Toast.LENGTH_SHORT).show() }
                        }
                        override fun onResponse(call: Call, response: Response) {
                            runOnUiThread {
                                if (response.isSuccessful) {
                                    Toast.makeText(this@MyPageActivity, "탈퇴 완료", Toast.LENGTH_SHORT).show()
                                    sessionManager.logout { _, _ -> }
                                    goLogin("탈퇴 완료")
                                } else if (response.code == 401) {
                                    goLogin("세션 만료")
                                } else {
                                    Toast.makeText(this@MyPageActivity, "탈퇴 실패: ${response.code}", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    })
                }
                .setNegativeButton("아니오", null)
                .show()
        }
    }

    private fun setupBottomNavigation() {
        homeIcon.setOnClickListener { startActivity(Intent(this, PostDetailActivity::class.java)) }
        newPostIcon.setOnClickListener { startActivity(Intent(this, NewPostActivity::class.java)) }
        chatIcon.setOnClickListener { startActivity(Intent(this, LiveTalkActivity::class.java)) }
        myPageIcon.setOnClickListener { /* 현재 페이지 */ }
    }

    private fun goLogin(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }

    private fun absoluteUrl(url: String): String {
        if (url.startsWith("http")) return url
        val host = baseUrl.substringBefore("/api")
        return host + url
    }
}
