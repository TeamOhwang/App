package com.example.project

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class PostListAdapter(
    private val posts: List<Post>,
    private val onLikeClick: (Post, Int) -> Unit,
    private val onCommentClick: (Post) -> Unit,
    private val onRecipeClick: (Post) -> Unit
) : RecyclerView.Adapter<PostListAdapter.PostViewHolder>() {

    // 좋아요 상태를 추적하는 Map (임시로 로컬에서 관리)
    private val likedPosts = mutableSetOf<Int>()

    inner class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val profileImage: ImageView = itemView.findViewById(R.id.ivProfile)
        val username: TextView = itemView.findViewById(R.id.tvUsername)
        val postImage: ImageView = itemView.findViewById(R.id.ivPost)
        val likeButton: ImageView = itemView.findViewById(R.id.ivLike)
        val commentButton: ImageView = itemView.findViewById(R.id.ivComment)
        val likeCount: TextView = itemView.findViewById(R.id.tvLikeCount)
        val description: TextView = itemView.findViewById(R.id.tvDescription)
        val recipeButton: TextView = itemView.findViewById(R.id.tvRecipeButton)

        fun bind(post: Post, position: Int) {
            // 기본 정보 설정
            username.text = post.username
            description.text = "${post.description} ...레시피 확인하기"

            // 프로필 이미지 설정
            profileImage.setImageResource(post.profileImageRes)

            // 게시글 이미지 설정
            postImage.setImageResource(post.imageRes)

            // 실제 서버 이미지 URL 로딩 예시 (Glide 사용시)
            /*
            if (!post.imgUrl.isNullOrEmpty()) {
                Glide.with(itemView.context)
                    .load(post.imgUrl)
                    .placeholder(R.drawable.img_salad)
                    .error(R.drawable.img_salad)
                    .into(postImage)
            }
            */

            // 좋아요 상태 설정
            val isLiked = likedPosts.contains(position)
            val currentLikeCount = if (isLiked) post.likeCount + 1 else post.likeCount

            likeCount.text = currentLikeCount.toString()
            likeButton.setImageResource(
                if (isLiked) R.drawable.ic_heart_filled else R.drawable.ic_heart_empty
            )

            // 클릭 리스너 설정
            likeButton.setOnClickListener {
                if (likedPosts.contains(position)) {
                    likedPosts.remove(position)
                    likeButton.setImageResource(R.drawable.ic_heart_empty)
                    likeCount.text = post.likeCount.toString()
                } else {
                    likedPosts.add(position)
                    likeButton.setImageResource(R.drawable.ic_heart_filled)
                    likeCount.text = (post.likeCount + 1).toString()
                }
                onLikeClick(post, position)
            }

            commentButton.setOnClickListener {
                onCommentClick(post)
            }

            recipeButton.setOnClickListener {
                onRecipeClick(post)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_post, parent, false) // 위에서 만든 item_post.xml 사용
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        holder.bind(posts[position], position)
    }

    override fun getItemCount(): Int = posts.size
}