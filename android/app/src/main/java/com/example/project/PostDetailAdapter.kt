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
    private var posts: List<Post>,
    private val onRecipeClick: (Post) -> Unit,
    private val onCommentClick: (Post) -> Unit
) : RecyclerView.Adapter<PostDetailAdapter.PostViewHolder>() {

    class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val profileImage: ImageView = itemView.findViewById(R.id.ivProfile)
        val username: TextView = itemView.findViewById(R.id.tvUsername)
        val postImage: ImageView = itemView.findViewById(R.id.ivPost)
        val likeButton: ImageView = itemView.findViewById(R.id.ivLike)
        val commentButton: ImageView = itemView.findViewById(R.id.ivComment)
        val likeCount: TextView = itemView.findViewById(R.id.tvLikeCount)
        val description: TextView = itemView.findViewById(R.id.tvDescription)
        val recipeButton: TextView = itemView.findViewById(R.id.tvRecipeButton)
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

        // 좋아요 상태 업데이트 (현재는 기본 빈 하트, 추후 사용자별 좋아요 상태 확인 로직 추가)
        holder.likeButton.setImageResource(R.drawable.ic_heart_empty)

        // 클릭 리스너 설정
        holder.recipeButton.setOnClickListener {
            onRecipeClick(post)
        }

        holder.commentButton.setOnClickListener {
            onCommentClick(post)
        }

        // 좋아요 버튼 클릭 (추후 서버와 연동하여 실제 좋아요 기능 구현)
        holder.likeButton.setOnClickListener {
            // TODO: 서버에 좋아요 요청 보내기
            // 현재는 UI만 변경 (임시)
            val currentDrawable = holder.likeButton.drawable
            if (currentDrawable.constantState == holder.itemView.context.getDrawable(R.drawable.ic_heart_empty)?.constantState) {
                holder.likeButton.setImageResource(R.drawable.ic_heart_filled)
                // holder.likeCount.text = "좋아요 ${post.likeCount + 1}개"
            } else {
                holder.likeButton.setImageResource(R.drawable.ic_heart_empty)
                // holder.likeCount.text = "좋아요 ${post.likeCount}개"
            }
        }
    }

    override fun getItemCount(): Int = posts.size

    // 데이터 업데이트 함수
    fun updatePosts(newPosts: List<Post>) {
        this.posts = newPosts
        notifyDataSetChanged()
    }
}