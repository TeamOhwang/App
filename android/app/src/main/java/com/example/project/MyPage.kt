package com.example.project

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class MyPage : AppCompatActivity() {

    private lateinit var profileImageView: ImageView
    private lateinit var nicknameTextView: TextView
    private lateinit var postCountTextView: TextView
    private lateinit var bookmarkCountTextView: TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: MyPagePostAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_page)

        profileImageView = findViewById(R.id.profile)
        nicknameTextView = findViewById(R.id.nickname)
        postCountTextView = findViewById(R.id.num1)
        bookmarkCountTextView = findViewById(R.id.num2)
        recyclerView = findViewById<RecyclerView>(R.id.postlist)

        recyclerView.layoutManager = GridLayoutManager(this, 3)
        adapter = MyPagePostAdapter()
        recyclerView.adapter = adapter

        fetchProfileAndPosts()

    }

    private fun fetchProfileAndPosts()  {
        // Retrofit 등으로 백엔드 API 호출해서 데이터 가져오기
        // 받아온 데이터를 화면에 세팅
        // 예: nicknameTextView.text = profile.nickname
        // adapter.submitList(posts)

        // 프로필 정보 임시 세팅
        nicknameTextView.text = "홍길동"
        postCountTextView.text = "15"
        bookmarkCountTextView.text = "20"
        // 프로필 이미지는 Glide 등으로 이미지 URL을 넣어주세요 (임시로 기본 이미지로 대체 가능)
        Glide.with(this)
            .load("https://www.google.com/url?sa=i&url=https%3A%2F%2Fwww.instagram.com%2Fp%2FDCefKYZzfNv%2F&psig=AOvVaw1ca8ooetGnWmvRR_CVgiRe&ust=1754601051838000&source=images&cd=vfe&opi=89978449&ved=0CBUQjRxqFwoTCMjJ07WM944DFQAAAAAdAAAAABAE")
            .circleCrop()
            .placeholder(R.drawable.userlogo) // 기본이미지
            .into(profileImageView)

        // 게시물 더미 데이터 생성 (MyPage용 간단한 구조)
        val dummyPosts = listOf(
            MyPagePost(1, R.drawable.img_salad),
            MyPagePost(2, R.drawable.img_salad),
            MyPagePost(3, R.drawable.img_salad),
            MyPagePost(4, R.drawable.img_salad),
            MyPagePost(5, R.drawable.img_salad),
            MyPagePost(6, R.drawable.img_salad)
        )
        adapter.submitList(dummyPosts)
    }
}