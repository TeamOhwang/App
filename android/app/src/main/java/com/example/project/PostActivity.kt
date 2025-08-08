package com.example.project

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import org.json.JSONArray
import org.json.JSONException

class PostActivity : AppCompatActivity() {

    private lateinit var postAdapter: PostDetailAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var requestQueue: RequestQueue

    // 서버 URL - 8081 포트 사용
    private val BASE_URL = "http://10.0.2.2:8081" // 안드로이드 에뮬레이터용

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post)

        // Volley 요청 큐 초기화
        requestQueue = Volley.newRequestQueue(this)

        setupBanner()
        setupMainRecyclerView()

        // 서버에서 게시글 데이터 가져오기
        fetchPostsFromServer()
    }

    private fun setupBanner() {
        val bannerViewPager: ViewPager2 = findViewById(R.id.bannerViewPager)
        val bannerItems = listOf(R.drawable.banner_1)
        val bannerAdapter = BannerAdapter(bannerItems)
        bannerViewPager.adapter = bannerAdapter
    }

    // 메인 화면의 게시글 RecyclerView 설정
    private fun setupMainRecyclerView() {
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // PostDetailAdapter 사용
        postAdapter = PostDetailAdapter(
            mutableListOf(),
            onCommentClick = { post ->
                // 댓글 클릭 처리
            },
            onLikeClick = { post, position ->
                // 좋아요 클릭 처리
            }
        )
        recyclerView.adapter = postAdapter
    }

    // 서버에서 게시글 데이터 가져오기
    private fun fetchPostsFromServer() {
        val url = "$BASE_URL/api/read"

        Log.d("PostActivity", "서버 요청 URL: $url")

        val jsonArrayRequest = JsonArrayRequest(
            Request.Method.GET,
            url,
            null,
            { response ->
                Log.d("PostActivity", "서버 응답 성공: $response")
                try {
                    val posts = parsePostsFromJson(response)
                    updatePostsAdapter(posts)
                    Log.d("PostActivity", "게시글 ${posts.size}개를 불러왔습니다!")
                } catch (e: JSONException) {
                    e.printStackTrace()
                    Log.e("PostActivity", "JSON 파싱 오류: ${e.message}")
                }
            },
            { error ->
                error.printStackTrace()
                Log.e("PostActivity", "서버 연결 오류: ${error.message}")
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

                // user 정보 파싱 (PostResponseDto 구조에 맞게 수정)
                val userObject = jsonObject.optJSONObject("user")
                val username = if (userObject != null) {
                    userObject.optString("nickname", "익명사용자")
                } else {
                    "익명${id}"
                }
                val profileImgUrl = userObject?.optString("profileImage")

                // 좋아요 정보 파싱
                val likeCount = jsonObject.optInt("likeCount", 0)
                val isLiked = jsonObject.optBoolean("isLiked", false)

                // Post 클래스에 맞게 매핑
                val post = Post(
                    id = id,
                    username = username,
                    profileImageRes = R.drawable.ic_profile_placeholder,
                    imageRes = R.drawable.img_salad, // 기본 이미지 (Glide 로딩 실패시 사용)
                    likeCount = likeCount,
                    description = if (content.length > 50) "${content.substring(0, 50)}..." else content,
                    recipeTitle = "${username}님의 레시피",
                    recipeContent = content,
                    comments = emptyList(), // 기본값
                    imgUrl = if (imgUrl == "null" || imgUrl.isEmpty()) null else imgUrl, // null 처리
                    profileImgUrl = if (profileImgUrl == "null" || profileImgUrl.isNullOrEmpty()) null else profileImgUrl,
                    isLiked = isLiked
                )

                posts.add(post)
                Log.d("PostActivity", "게시글 파싱 완료: ID=$id, Content=$content, ImgUrl=$imgUrl")

            } catch (e: JSONException) {
                e.printStackTrace()
                Log.e("PostActivity", "JSON 파싱 오류: ${e.message}")
                continue
            }
        }

        return posts
    }

    // 어댑터 업데이트
    private fun updatePostsAdapter(posts: List<Post>) {
        postAdapter.updatePosts(posts)
        Log.d("PostActivity", "게시글 목록 업데이트 완료: ${posts.size}개")
    }



    override fun onDestroy() {
        super.onDestroy()
        // 요청 큐 정리
        requestQueue.cancelAll(this)
    }
}