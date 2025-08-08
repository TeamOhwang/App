package com.example.project

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.ScrollView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class PostDetailAdapter(
    private var posts: MutableList<Post>,
    private val onCommentClick: (Post) -> Unit,
    private val onLikeClick: (Post, Int) -> Unit
) : RecyclerView.Adapter<PostDetailAdapter.PostViewHolder>() {

    class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val profileImage: ImageView = itemView.findViewById(R.id.ivProfile)
        val username: TextView = itemView.findViewById(R.id.tvUsername)
        val postImage: ImageView = itemView.findViewById(R.id.ivPost)
        val likeButton: ImageView = itemView.findViewById(R.id.ivLike)
        val commentButton: ImageView = itemView.findViewById(R.id.ivComment)
        val likeCount: TextView = itemView.findViewById(R.id.tvLikeCount)
        val description: TextView = itemView.findViewById(R.id.tvDescription)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_post_detail, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = posts[position]

        // 기본 정보 설정
        holder.username.text = post.username
        holder.likeCount.text = "좋아요 ${post.likeCount}개"
        holder.description.text = post.description

        // 프로필 이미지 로딩
        if (!post.profileImgUrl.isNullOrEmpty()) {
            Glide.with(holder.itemView.context)
                .load(post.profileImgUrl)
                .circleCrop()
                .placeholder(post.profileImageRes)
                .error(post.profileImageRes)
                .into(holder.profileImage)
        } else {
            holder.profileImage.setImageResource(post.profileImageRes)
        }

        // 게시글 이미지 로딩
        if (!post.imgUrl.isNullOrEmpty()) {
            Glide.with(holder.itemView.context)
                .load(post.imgUrl)
                .placeholder(post.imageRes)
                .error(post.imageRes)
                .into(holder.postImage)
        } else {
            holder.postImage.setImageResource(post.imageRes)
        }

        // 좋아요 상태 업데이트
        updateLikeButton(holder.likeButton, post.isLiked)

        // 클릭 리스너 설정
        holder.commentButton.setOnClickListener {
            onCommentClick(post)
        }

        // 좋아요 버튼 클릭
        holder.likeButton.setOnClickListener {
            onLikeClick(post, position)
        }
    }

    override fun getItemCount(): Int = posts.size

    // 데이터 업데이트 함수
    fun updatePosts(newPosts: List<Post>) {
        this.posts.clear()
        this.posts.addAll(newPosts)
        notifyDataSetChanged()
    }
    
    // 좋아요 상태 업데이트
    fun updateLikeStatus(position: Int, isLiked: Boolean, likeCount: Int) {
        if (position < posts.size) {
            val updatedPost = posts[position].copy(isLiked = isLiked, likeCount = likeCount)
            posts[position] = updatedPost
            notifyItemChanged(position)
        }
    }
    
    // 좋아요 버튼 상태 업데이트
    private fun updateLikeButton(likeButton: ImageView, isLiked: Boolean) {
        if (isLiked) {
            likeButton.setImageResource(R.drawable.ic_heart_filled)
        } else {
            likeButton.setImageResource(R.drawable.ic_heart_empty)
        }
    }
}