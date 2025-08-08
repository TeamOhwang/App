package com.example.project

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.project.util.SessionManager
import okhttp3.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

class PostDetailActivity : AppCompatActivity() {

    // UI 요소들
    private lateinit var recyclerView: RecyclerView
    private lateinit var postDetailAdapter: PostDetailAdapter

    // 하단 네비게이션 아이콘들
    private lateinit var homeIcon: ImageView
    private lateinit var newPostIcon: ImageView
    private lateinit var chatIcon: ImageView
    private lateinit var myPageIcon: ImageView

    // 네트워크 관련
    private lateinit var sessionManager: SessionManager
    private lateinit var client: OkHttpClient
    private val baseUrl = "http://192.168.219.180:8081/api"
    
    // 게시물 목록
    private val postsList = mutableListOf<Post>()
    
    // ActivityResultLauncher for NewPostActivity
    private val newPostLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            Log.d("PostDetailActivity", "새 게시물 등록 완료 - 데이터 새로고침")
            fetchAllPosts()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post_detail)

        // SessionManager 초기화
        sessionManager = SessionManager.getInstance(this)
        client = sessionManager.getHttpClient()

        // UI 요소 초기화
        initViews()

        // RecyclerView 설정
        setupRecyclerView()

        // 하단 네비게이션 설정
        setupBottomNavigation()

        // 서버에서 모든 게시물 가져오기
        fetchAllPosts()
    }

    override fun onResume() {
        super.onResume()
        // 화면이 다시 보여질 때마다 데이터 새로고침
        Log.d("PostDetailActivity", "onResume - 데이터 새로고침")
        fetchAllPosts()
    }

    private fun initViews() {
        // RecyclerView 초기화
        recyclerView = findViewById(R.id.recyclerView)

        // 하단 네비게이션 아이콘들
        try {
            homeIcon = findViewById(R.id.homeicon)
            newPostIcon = findViewById(R.id.newposticon)
            chatIcon = findViewById(R.id.chaticon)
            myPageIcon = findViewById(R.id.mypageicon)
        } catch (e: Exception) {
            Log.d("PostDetailActivity", "하단 네비게이션을 찾을 수 없음: ${e.message}")
        }
    }

    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(this)
        
        postDetailAdapter = PostDetailAdapter(
            postsList,
            onRecipeClick = { post ->
                Toast.makeText(this, "${post.username}님의 레시피:\n${post.recipeContent}", Toast.LENGTH_LONG).show()
            },
            onCommentClick = { post ->
                Toast.makeText(this, "댓글 기능은 추후 구현 예정입니다", Toast.LENGTH_SHORT).show()
            }
        )
        
        recyclerView.adapter = postDetailAdapter
    }

    // 서버에서 모든 게시물 가져오기
    private fun fetchAllPosts() {
        Log.d("PostDetailActivity", "모든 게시물 요청 시작")
        
        // 새로고침 시에는 토스트 메시지 표시하지 않음 (onCreate에서만 표시)

        val request = Request.Builder()
            .url("$baseUrl/read")
            .get()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Log.e("PostDetailActivity", "게시물 목록 가져오기 실패", e)
                    Toast.makeText(this@PostDetailActivity, "게시물 목록을 가져올 수 없습니다: ${e.message}", Toast.LENGTH_SHORT).show()
                    loadDummyData() // 실패 시 더미 데이터 로드
                }
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    val responseBody = response.body?.string()
                    Log.d("PostDetailActivity", "게시물 목록 응답: $responseBody")

                    runOnUiThread {
                        if (response.isSuccessful && responseBody != null) {
                            try {
                                val jsonArray = JSONArray(responseBody)
                                val posts = parsePostsFromJson(jsonArray)
                                updatePostsList(posts)
                            } catch (e: Exception) {
                                Log.e("PostDetailActivity", "JSON 파싱 오류", e)
                                Toast.makeText(this@PostDetailActivity, "데이터 파싱 오류가 발생했습니다", Toast.LENGTH_SHORT).show()
                                loadDummyData()
                            }
                        } else {
                            Log.e("PostDetailActivity", "게시물 목록 가져오기 실패: ${response.code}")
                            Toast.makeText(this@PostDetailActivity, "게시물 목록을 가져올 수 없습니다", Toast.LENGTH_SHORT).show()
                            loadDummyData()
                        }
                    }
                }
            }
        })
    }

    // JSON 배열을 Post 객체 리스트로 변환
    private fun parsePostsFromJson(jsonArray: JSONArray): List<Post> {
        val posts = mutableListOf<Post>()

        for (i in 0 until jsonArray.length()) {
            try {
                val jsonObject = jsonArray.getJSONObject(i)
                val post = parsePostFromJson(jsonObject)
                posts.add(post)
            } catch (e: Exception) {
                Log.e("PostDetailActivity", "게시물 파싱 오류: ${e.message}")
                continue
            }
        }

        Log.d("PostDetailActivity", "총 ${posts.size}개의 게시물 파싱 완료")
        return posts
    }

    // JSON 객체를 Post 객체로 변환
    private fun parsePostFromJson(jsonObject: JSONObject): Post {
        val id = jsonObject.optLong("id", 0)
        val content = jsonObject.optString("content", "내용 없음")
        val imgUrl = jsonObject.optString("imgUrl")
        val createdAt = jsonObject.optString("createdAt", "")

        // user 정보 파싱 (PostResponseDto 구조에 맞게 수정)
        val userObject = jsonObject.optJSONObject("user")
        val username = if (userObject != null) {
            userObject.optString("nickname", "익명사용자")
        } else {
            "익명${id}"
        }
        val profileImgUrl = userObject?.optString("profileImage")

        Log.d("PostDetailActivity", "게시물 파싱 - ID: $id, 작성자: $username, 내용: $content")

        return Post(
            id = id,
            username = username,
            profileImageRes = R.drawable.ic_profile_placeholder,
            imageRes = R.drawable.img_salad,
            likeCount = 0, // TODO: 좋아요 수 API 연동
            description = content,
            recipeTitle = "${username}님의 레시피",
            recipeContent = content,
            comments = emptyList(), // TODO: 댓글 API 연동
            imgUrl = if (imgUrl == "null" || imgUrl.isEmpty()) null else imgUrl,
            profileImgUrl = if (profileImgUrl == "null" || profileImgUrl.isNullOrEmpty()) null else profileImgUrl
        )
    }

    // 게시물 목록 업데이트
    private fun updatePostsList(posts: List<Post>) {
        postsList.clear()
        postsList.addAll(posts)
        postDetailAdapter.notifyDataSetChanged()
        
        Log.d("PostDetailActivity", "게시물 목록 업데이트 완료: ${posts.size}개")
        
        if (posts.isEmpty()) {
            Toast.makeText(this, "등록된 게시물이 없습니다", Toast.LENGTH_SHORT).show()
        }
    }

    // 네트워크 오류 시 더미 데이터 로드
    private fun loadDummyData() {
        val dummyPosts = listOf(
            Post(
                id = 1,
                username = "테스트유저1",
                profileImageRes = R.drawable.ic_profile_placeholder,
                imageRes = R.drawable.img_salad,
                likeCount = 5,
                description = "서버 연결 테스트를 위한 첫 번째 더미 데이터입니다",
                recipeTitle = "테스트유저1님의 레시피",
                recipeContent = "이것은 서버 연결이 안될 때 표시되는 테스트 데이터입니다.\n\n실제 서버가 연결되면 DB의 실제 게시글이 순서대로 표시됩니다.",
                comments = listOf(
                    Comment("댓글유저1", R.drawable.ic_profile_placeholder, "1시간 전", "테스트 댓글입니다")
                ),
                imgUrl = null,
                profileImgUrl = null
            ),
            Post(
                id = 2,
                username = "테스트유저2",
                profileImageRes = R.drawable.ic_profile_placeholder,
                imageRes = R.drawable.img_salad,
                likeCount = 3,
                description = "서버 연결 테스트를 위한 두 번째 더미 데이터입니다",
                recipeTitle = "테스트유저2님의 레시피",
                recipeContent = "이것도 테스트 데이터입니다. 실제로는 DB에 저장된 순서대로 여러 개의 게시글이 목록으로 표시됩니다.",
                comments = listOf(
                    Comment("댓글유저2", R.drawable.ic_profile_placeholder, "30분 전", "서버 연결을 확인해보세요")
                ),
                imgUrl = null,
                profileImgUrl = null
            )
        )
        
        updatePostsList(dummyPosts)
        Toast.makeText(this, "서버 연결 실패 - 테스트 데이터를 표시합니다", Toast.LENGTH_LONG).show()
    }

    private fun setupBottomNavigation() {
        if (!::homeIcon.isInitialized) {
            Log.d("PostDetailActivity", "하단 네비게이션이 레이아웃에 없습니다")
            return
        }

        homeIcon.setOnClickListener {
            Log.d("PostDetailActivity", "홈 버튼 클릭 (현재 페이지)")
            // 현재 페이지이므로 아무 동작 안함
        }

        newPostIcon.setOnClickListener {
            val intent = Intent(this, NewPostActivity::class.java)
            newPostLauncher.launch(intent)
        }

        chatIcon.setOnClickListener {
            val intent = Intent(this, LiveTalkActivity::class.java)
            startActivity(intent)
        }

        myPageIcon.setOnClickListener {
            val intent = Intent(this, MyPageActivity::class.java)
            startActivity(intent)
        }
    }
}