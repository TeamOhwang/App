package com.example.project

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class PostAdapter_kwangwon(
    private val posts: List<Post>,
    private val onRecipeClick: (Post) -> Unit,
    private val onCommentClick: (Post) -> Unit
) : RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    // ViewHolder: item_post.xml의 뷰들을 멤버 변수로 가집니다.
    inner class PostViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivProfile: ImageView = view.findViewById(R.id.ivProfile)
        val tvUsername: TextView = view.findViewById(R.id.tvUsername)
        val ivPostImage: ImageView = view.findViewById(R.id.ivPostImage)
        val ivLike: ImageView = view.findViewById(R.id.ivLike)
        val ivComment: ImageView = view.findViewById(R.id.ivComment)
        val tvLikeCount: TextView = view.findViewById(R.id.tvLikeCount)
        val tvPostContent: TextView = view.findViewById(R.id.tvPostContent)

        // 데이터를 뷰에 바인딩하는 함수
        fun bind(post: Post) {
            ivProfile.setImageResource(post.profileImageRes)
            tvUsername.text = post.username
            ivPostImage.setImageResource(post.postImageRes)
            tvLikeCount.text = "좋아요 ${post.likes}개"
            tvPostContent.text = post.content

            updateLikeButton(post)

            // 클릭 리스너 설정
            tvPostContent.setOnClickListener { onRecipeClick(post) }
            ivComment.setOnClickListener { onCommentClick(post) }
            ivLike.setOnClickListener {
                post.isLiked = !post.isLiked // 좋아요 상태 변경
                if (post.isLiked) {
                    post.likes++
                } else {
                    post.likes--
                }
                tvLikeCount.text = "좋아요 ${post.likes}개"
                updateLikeButton(post)
            }
        }

        private fun updateLikeButton(post: Post) {
            if (post.isLiked) {
                ivLike.setImageResource(R.drawable.ic_heart_filled) // 꽉 찬 하트
            } else {
                ivLike.setImageResource(R.drawable.ic_heart_empty) // 빈 하트
            }
        }
    }

    // ViewHolder를 생성하고 레이아웃을 인플레이트합니다.
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_post, parent, false)
        return PostViewHolder(view)
    }

    // ViewHolder에 데이터를 바인딩합니다.
    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        holder.bind(posts[position])
    }

    // 전체 아이템 개수를 반환합니다.
    override fun getItemCount(): Int {
        return posts.size
    }
}