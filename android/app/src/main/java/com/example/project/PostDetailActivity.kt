package com.example.project

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import org.json.JSONArray
import org.json.JSONException

class PostDetailActivity : AppCompatActivity() {

    // 하단 네비게이션 아이콘들
    private lateinit var homeIcon: ImageView
    private lateinit var newPostIcon: ImageView
    private lateinit var chatIcon: ImageView
    private lateinit var myPageIcon: ImageView

    // 게시글 목록 관련 변수들
    private lateinit var recyclerView: RecyclerView
    private lateinit var postAdapter: PostDetailAdapter
    private lateinit var requestQueue: RequestQueue

    // 서버 URL - 8081 포트 사용
    private val BASE_URL = "http://10.0.2.2:8081" // 안드로이드 에뮬레이터용

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post)

        // Volley 요청 큐 초기화
        requestQueue = Volley.newRequestQueue(this)

        // UI 요소 초기화
        initViews()

        // RecyclerView 설정
        setupRecyclerView()

        // 하단 네비게이션 설정
        setupBottomNavigation()

        // 서버에서 게시글 데이터 가져오기
        fetchPostsFromServer()
    }

    private fun initViews() {
        // RecyclerView (필수)
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

        // PostDetailAdapter 사용
        postAdapter = PostDetailAdapter(
            emptyList(),
            onRecipeClick = { post ->
                Toast.makeText(this, "${post.username}님의 레시피:\n${post.recipeContent}", Toast.LENGTH_LONG).show()
            },
            onCommentClick = { post ->
                Toast.makeText(this, "댓글 ${post.comments.size}개", Toast.LENGTH_SHORT).show()
            }
        )

        recyclerView.adapter = postAdapter
    }

    // 서버에서 게시글 데이터 가져오기
    private fun fetchPostsFromServer() {
        val url = "$BASE_URL/api/read"

        Log.d("PostDetailActivity", "서버 요청 URL: $url")

        val jsonArrayRequest = JsonArrayRequest(
            Request.Method.GET,
            url,
            null,
            { response ->
                Log.d("PostDetailActivity", "서버 응답 성공: $response")
                try {
                    val posts = parsePostsFromJson(response)
                    updatePostsList(posts)
                } catch (e: JSONException) {
                    e.printStackTrace()
                    Log.e("PostDetailActivity", "JSON 파싱 오류: ${e.message}")
                    Toast.makeText(this, "데이터 파싱 오류: ${e.message}", Toast.LENGTH_LONG).show()
                    loadDummyData()
                }
            },
            { error ->
                error.printStackTrace()
                Log.e("PostDetailActivity", "서버 연결 오류: ${error.message}")

                val errorMsg = when {
                    error.networkResponse == null -> "네트워크 연결 실패 - 서버가 8081포트에서 실행중인지 확인하세요"
                    error.networkResponse.statusCode == 404 -> "API 엔드포인트를 찾을 수 없습니다 (404)"
                    error.networkResponse.statusCode >= 500 -> "서버 내부 오류 (${error.networkResponse.statusCode})"
                    else -> "연결 오류: ${error.message}"
                }

                Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show()
                loadDummyData()
            }
        )

        requestQueue.add(jsonArrayRequest)
    }

    // JSON 응답을 Post 객체 리스트로 변환 (실제 백엔드 JSON 구조에 맞게)
    private fun parsePostsFromJson(jsonArray: JSONArray): List<Post> {
        val posts = mutableListOf<Post>()

        for (i in 0 until jsonArray.length()) {
            try {
                val jsonObject = jsonArray.getJSONObject(i)

                // 실제 백엔드 JSON 구조에 맞게 필드 추출
                val id = jsonObject.optLong("id", 0)
                val content = jsonObject.optString("content", "내용 없음")
                val imgUrl = jsonObject.optString("imgUrl")  // null일 수 있음
                val createdAt = jsonObject.optString("createdAt", "")

                // user 객체가 JSON에 없으므로 기본값 사용
                val username = "익명${id}" // id로 구분

                // Post 클래스에 맞게 매핑
                val post = Post(
                    username = username,
                    profileImageRes = R.drawable.ic_profile_placeholder,
                    imageRes = R.drawable.img_salad, // 기본 이미지 (Glide 로딩 실패시 사용)
                    likeCount = 0, // 기본값
                    description = if (content.length > 50) "${content.substring(0, 50)}..." else content,
                    recipeTitle = "${username}님의 레시피",
                    recipeContent = content,
                    comments = emptyList(), // 기본값
                    imgUrl = if (imgUrl == "null" || imgUrl.isEmpty()) null else imgUrl, // null 처리
                    profileImgUrl = null // 프로필 이미지 URL 없음
                )

                posts.add(post)
                Log.d("PostDetailActivity", "게시글 파싱 완료: ID=$id, Content=$content, ImgUrl=$imgUrl")

            } catch (e: JSONException) {
                e.printStackTrace()
                Log.e("PostDetailActivity", "JSON 파싱 오류: ${e.message}")
                continue
            }
        }

        return posts
    }

    // 게시글 목록 업데이트
    private fun updatePostsList(posts: List<Post>) {
        postAdapter.updatePosts(posts)
        Log.d("PostDetailActivity", "게시글 목록 업데이트 완료: ${posts.size}개")
    }

    // 네트워크 오류시 더미 데이터 로드 (테스트용)
    private fun loadDummyData() {
        Toast.makeText(this, "서버 연결 실패 - 테스트 데이터를 표시합니다", Toast.LENGTH_LONG).show()

        val dummyPosts = listOf(
            Post(
                "테스트유저1",
                R.drawable.ic_profile_placeholder,
                R.drawable.img_salad,
                5,
                "서버 연결 테스트를 위한 첫 번째 더미 데이터입니다",
                "테스트유저1님의 레시피",
                "이것은 서버 연결이 안될 때 표시되는 테스트 데이터입니다.\n\n실제 서버가 연결되면 DB의 실제 게시글이 순서대로 표시됩니다.",
                listOf(
                    Comment("댓글유저1", R.drawable.ic_profile_placeholder_2, "1시간 전", "테스트 댓글입니다")
                ),
                imgUrl = "https://picsum.photos/400/300?random=1",
                profileImgUrl = "https://picsum.photos/100/100?random=101"
            ),
            Post(
                "테스트유저2",
                R.drawable.ic_profile_placeholder,
                R.drawable.img_salad,
                3,
                "서버 연결 테스트를 위한 두 번째 더미 데이터입니다",
                "테스트유저2님의 레시피",
                "이것도 테스트 데이터입니다. 실제로는 DB에 저장된 순서대로 여러 개의 게시글이 목록으로 표시됩니다.",
                listOf(
                    Comment("댓글유저2", R.drawable.ic_profile_placeholder_3, "30분 전", "서버 연결을 확인해보세요")
                ),
                imgUrl = "https://picsum.photos/400/300?random=2",
                profileImgUrl = "https://picsum.photos/100/100?random=102"
            ),
            Post(
                "테스트유저3",
                R.drawable.ic_profile_placeholder,
                R.drawable.img_salad,
                8,
                "서버 연결 테스트를 위한 세 번째 더미 데이터입니다",
                "테스트유저3님의 레시피",
                "세 번째 테스트 게시글입니다. 백엔드 DB에서 findAll()로 가져온 순서대로 목록이 표시됩니다.",
                emptyList(),
                imgUrl = "https://picsum.photos/400/300?random=3",
                profileImgUrl = "https://picsum.photos/100/100?random=103"
            )
        )

        updatePostsList(dummyPosts)
    }

    private fun setupBottomNavigation() {
        if (!::homeIcon.isInitialized) {
            Log.d("PostDetailActivity", "하단 네비게이션이 레이아웃에 없습니다")
            return
        }

        homeIcon.setOnClickListener {
            Log.d("PostDetailActivity", "홈 버튼 클릭 (현재 페이지)")
        }

        newPostIcon.setOnClickListener {
            Log.d("PostDetailActivity", "새 게시물 버튼 클릭")
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

    override fun onDestroy() {
        super.onDestroy()
        // 요청 큐 정리
        requestQueue.cancelAll(this)
    }
}