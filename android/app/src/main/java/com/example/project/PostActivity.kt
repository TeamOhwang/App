package com.example.project

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.project.BannerAdapter
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import org.json.JSONArray
import org.json.JSONException

class PostActivity : AppCompatActivity() {

    private lateinit var bottomSheetBehavior: BottomSheetBehavior<NestedScrollView>
    private lateinit var bottomSheet: NestedScrollView
    private lateinit var commentHeader: TextView
    private lateinit var postAdapter: PostDetailAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var requestQueue: RequestQueue

    // 서버 URL - 8081 포트 사용
    private val BASE_URL = "http://10.0.2.2:8081" // 안드로이드 에뮬레이터용
    // private val BASE_URL = "http://192.168.0.xxx:8081" // 실제 기기용 - PC IP로 변경

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post)

        // Volley 요청 큐 초기화
        requestQueue = Volley.newRequestQueue(this)

        setupBanner()
        setupBottomSheet()
        setupMainRecyclerView()
        setupCommentRecyclerView()

        // 서버에서 게시글 데이터 가져오기
        fetchPostsFromServer()
    }

    private fun setupBanner() {
        // 1. activity_main.xml에 있는 ViewPager2 뷰를 찾습니다.
        val bannerViewPager: ViewPager2 = findViewById(R.id.bannerViewPager)

        // 2. 배너에 표시할 이미지 데이터 목록을 생성합니다.
        val bannerItems = listOf(R.drawable.banner_1)

        // 3. BannerAdapter 인스턴스를 생성하고 이미지 목록을 전달합니다.
        val bannerAdapter = BannerAdapter(bannerItems)

        // 4. ViewPager2에 어댑터를 설정합니다.
        bannerViewPager.adapter = bannerAdapter
    }

    // 하단 시트 (상세보기) 설정
    private fun setupBottomSheet() {
        bottomSheet = findViewById(R.id.bottomSheet)
        commentHeader = bottomSheet.findViewById(R.id.tvCommentHeader)
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)

        // 초기에는 숨겨진 상태
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN

        // BottomSheet 상태 변화 리스너
        bottomSheetBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                // 상태 변화에 따른 로직 (필요시 추가)
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                // 슬라이드 시 로직 (필요시 추가)
            }
        })
    }

    // 메인 화면의 게시글 RecyclerView 설정
    private fun setupMainRecyclerView() {
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // 빈 리스트로 초기화
        postAdapter = PostDetailAdapter(
            emptyList(),
            onRecipeClick = { post ->
                showBottomSheet(post)
            },
            onCommentClick = { post ->
                showBottomSheet(post, true)
            }
        )
        recyclerView.adapter = postAdapter
    }

    // 서버에서 게시글 데이터 가져오기
    private fun fetchPostsFromServer() {
        val url = "$BASE_URL/api/read"

        val jsonArrayRequest = JsonArrayRequest(
            Request.Method.GET,
            url,
            null,
            { response ->
                try {
                    val posts = parsePostsFromJson(response)
                    updatePostsAdapter(posts)
                } catch (e: JSONException) {
                    e.printStackTrace()
                    Toast.makeText(this, "데이터 파싱 오류", Toast.LENGTH_SHORT).show()
                    // 오류 발생시 더미 데이터로 대체
                    loadDummyData()
                }
            },
            { error ->
                error.printStackTrace()
                Toast.makeText(this, "서버 연결 오류: ${error.message}", Toast.LENGTH_SHORT).show()
                // 네트워크 오류시 더미 데이터로 대체
                loadDummyData()
            }
        )

        requestQueue.add(jsonArrayRequest)
    }

    // JSON 응답을 Post 객체 리스트로 변환
    private fun parsePostsFromJson(jsonArray: JSONArray): List<Post> {
        val posts = mutableListOf<Post>()

        for (i in 0 until jsonArray.length()) {
            try {
                val jsonObject = jsonArray.getJSONObject(i)

                // 백엔드 Post 엔티티 구조에 맞게 필드 추출
                val id = jsonObject.optLong("id", 0)
                val content = jsonObject.optString("content", "내용 없음")
                val imgUrl = jsonObject.optString("imgUrl", "")
                val createdAt = jsonObject.optString("createdAt", "")

                // User 객체에서 정보 추출 (nested object)
                val userObject = jsonObject.optJSONObject("user")
                val username = userObject?.optString("username") ?: "익명"
                val userProfileImg = userObject?.optString("profileImg") ?: ""

                // Likes 배열에서 좋아요 개수 계산
                val likesArray = jsonObject.optJSONArray("likes")
                val likeCount = likesArray?.length() ?: 0

                // Comments 배열에서 댓글 리스트 생성
                val commentsArray = jsonObject.optJSONArray("comments")
                val comments = mutableListOf<Comment>()

                if (commentsArray != null) {
                    for (j in 0 until commentsArray.length()) {
                        val commentObj = commentsArray.getJSONObject(j)
                        val commentUserObj = commentObj.optJSONObject("user")
                        val commentUsername = commentUserObj?.optString("username") ?: "익명"
                        val commentContent = commentObj.optString("content", "")
                        val commentCreatedAt = commentObj.optString("createdAt", "")

                        val comment = Comment(
                            commentUsername,
                            R.drawable.ic_profile_placeholder, // 기본 댓글 프로필 이미지
                            commentCreatedAt,
                            commentContent
                        )
                        comments.add(comment)
                    }
                }

                // 현재 Post 클래스에 맞게 매핑
                val post = Post(
                    username = username,
                    profileImageRes = R.drawable.ic_profile_placeholder, // 기본 프로필 이미지 (추후 Glide로 실제 이미지 로드)
                    imageRes = if (imgUrl.isNotEmpty()) R.drawable.img_salad else R.drawable.img_salad, // 기본 게시글 이미지 (추후 Glide로 실제 이미지 로드)
                    likeCount = likeCount,
                    description = if (content.length > 50) "${content.substring(0, 50)}... 레시피 확인하기" else "$content ...레시피 확인하기",
                    recipeTitle = "${username}님의 레시피", // 백엔드에 별도 제목 필드가 없으므로 임시로 생성
                    recipeContent = content,
                    comments = comments
                )

                posts.add(post)
            } catch (e: JSONException) {
                e.printStackTrace()
                continue // 해당 게시글은 건너뛰고 계속 진행
            }
        }

        return posts
    }

    // 어댑터 업데이트
    private fun updatePostsAdapter(posts: List<Post>) {
        postAdapter = PostDetailAdapter(
            posts,
            onRecipeClick = { post ->
                showBottomSheet(post)
            },
            onCommentClick = { post ->
                showBottomSheet(post, true)
            }
        )
        recyclerView.adapter = postAdapter
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
                )
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
                )
            ),
            Post(
                "테스트유저3",
                R.drawable.ic_profile_placeholder,
                R.drawable.img_salad,
                8,
                "서버 연결 테스트를 위한 세 번째 더미 데이터입니다",
                "테스트유저3님의 레시피",
                "세 번째 테스트 게시글입니다. 백엔드 DB에서 findAll()로 가져온 순서대로 목록이 표시됩니다.",
                emptyList()
            )
        )
        updatePostsAdapter(dummyPosts)
    }

    // 하단 시트의 댓글 RecyclerView 설정
    private fun setupCommentRecyclerView() {
        val rvComments: RecyclerView = bottomSheet.findViewById(R.id.rvComments)
        rvComments.layoutManager = LinearLayoutManager(this)
        rvComments.isNestedScrollingEnabled = false // 중첩 스크롤 비활성화
    }

    // 하단 시트를 보여주는 함수
    private fun showBottomSheet(post: Post, scrollToComments: Boolean = false) {
        // 데이터 채우기
        val tvDetailUsername: TextView = bottomSheet.findViewById(R.id.tvDetailUsername)
        val tvRecipeTitle: TextView = bottomSheet.findViewById(R.id.tvRecipeTitle)
        val tvRecipeContent: TextView = bottomSheet.findViewById(R.id.tvRecipeContent)
        val rvComments: RecyclerView = bottomSheet.findViewById(R.id.rvComments)

        tvDetailUsername.text = post.username
        tvRecipeTitle.text = post.recipeTitle
        tvRecipeContent.text = post.recipeContent
        commentHeader.text = "댓글 ${post.comments.size}"
        rvComments.adapter = CommentAdapter(post.comments)

        // BottomSheet를 펼침
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED

        if (scrollToComments) {
            // 댓글로 스크롤 (약간의 딜레이 후 실행해야 정확히 동작)
            bottomSheet.postDelayed({
                bottomSheet.smoothScrollTo(0, commentHeader.top)
            }, 200)
        } else {
            // 최상단으로 스크롤
            bottomSheet.postDelayed({
                bottomSheet.smoothScrollTo(0, 0)
            }, 200)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // 요청 큐 정리
        requestQueue.cancelAll(this)
    }
}