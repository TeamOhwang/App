package com.example.project

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.project.BannerAdapter
import android.view.View
import android.widget.TextView
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomsheet.BottomSheetBehavior


//private var ViewPager2.adapter: BannerAdapter

class PostActivity : AppCompatActivity() {

    private lateinit var bottomSheetBehavior: BottomSheetBehavior<NestedScrollView>
    private lateinit var bottomSheet: NestedScrollView
    private lateinit var commentHeader: TextView
    private lateinit var postAdapter: PostDetailAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post)

        // 1. activity_main.xml에 있는 ViewPager2 뷰를 찾습니다.
        val bannerViewPager: ViewPager2 = findViewById(R.id.bannerViewPager)

        // 2. 배너에 표시할 이미지 데이터 목록을 생성합니다.
        //    (미리 res/drawable 폴더에 banner_1, banner_2, banner_3 같은 이미지 파일을 추가해야 합니다.)
        val bannerItems = listOf(
            R.drawable.banner_1

            // 필요에 따라 더 많은 배너 이미지를 추가할 수 있습니다.
        )

        // 3. BannerAdapter 인스턴스를 생성하고 이미지 목록을 전달합니다.
        val bannerAdapter = BannerAdapter(bannerItems)

        // 4. ViewPager2에 어댑터를 설정합니다.
        bannerViewPager.adapter = bannerAdapter

        // (선택 사항) 배너가 좌우로 무한 스크롤되게 하려면 아래와 같이 설정할 수 있습니다.
        // 하지만 사용자가 첫 페이지와 마지막 페이지를 인지하기 어려울 수 있으므로 신중하게 사용하세요.
        // bannerViewPager.orientation = ViewPager2.ORIENTATION_HORIZONTAL
        // bannerViewPager.setCurrentItem(bannerAdapter.itemCount * 1000, false) // 시작점을 중간으로 설정

        setupBottomSheet()
        setupMainRecyclerView()
        setupCommentRecyclerView()
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
        val recyclerView: RecyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // 더미 데이터 생성
        val posts = listOf(
            Post(
                "집밥나선생",
                R.drawable.ic_profile_placeholder,
                R.drawable.img_salad,
                10,
                "오늘 저녁! ...레시피 확인하기",
                "토마토 샐러드",
                "[재료]\n방울토마토 10개\n파인애플 약간\n어린잎채소 먹고싶은 만큼!\n식초 1큰술\n올리브유 1.5큰술\n소금 약간\n꿀 1스푼\n\n[Recipe]\n1. 방울토마토를 열심자로 칼집을 약간 내서 끓는 물에 살짝 데쳐 줍니다. 약 1분 정도면 다 익으니 찬물에 샤워 한 번 해주고 껍질을 벗겨주면 되요~\n\n2. 샐러드 소스는 식초 1큰술, 올리브유 1.5큰술, 양파 다진것 1큰술, 소금 약간, 꿀 1스푼 넣어서 잘 섞어주시면 되요~ 꿀이 없으면 시럽으로 대체 해도 되고 올리고당 넣어주셔도 되요.\n\n3. 볼에 파인애플 담아주고 껍질을 깐 방울토마토 그리고 어린잎 채소 순으로 예쁘게 담아주시고요.\n\n4. 먹기 직전에 샐러드 소스를 골고루 뿌려서 섞어 주시면 근사한 저칼로리 다이어트 요리 토마토 샐러드 드가 완성이 되요.\n\n다들 맛있는 저녁 드세요!",
                listOf(
                    Comment("프로자취러", R.drawable.ic_profile_placeholder_2, "4시간 전", "헉 완전 맛있어 보여요!! 토마토에 파인애플이라니 생각도 못해봤네요"),
                    Comment("꼬질 냥이", R.drawable.ic_profile_placeholder_3, "1시간 전", "헉 토마토에 파인애플...? 상상이 안가는...")
                )
            )
            // 다른 게시글 추가 가능
        )

        postAdapter = PostDetailAdapter(
            posts,
            onRecipeClick = { post -> // '레시피 확인하기' 클릭 시
                showBottomSheet(post)
            },
            onCommentClick = { post -> // 댓글 아이콘 클릭 시
                showBottomSheet(post, true)
            }
        )
        recyclerView.adapter = postAdapter
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
}