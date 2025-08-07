package com.example.project

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class PostDetailAdapter(
    private val posts: List<Post>,
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
        
        holder.profileImage.setImageResource(post.profileImageRes)
        holder.username.text = post.username
        holder.postImage.setImageResource(post.imageRes)
        holder.likeCount.text = "${post.likeCount}"
        holder.description.text = post.description
        
        // 레시피 확인하기 버튼 클릭
        holder.recipeButton.setOnClickListener {
            onRecipeClick(post)
        }
        
        // 댓글 버튼 클릭
        holder.commentButton.setOnClickListener {
            onCommentClick(post)
        }
        
        // 좋아요 버튼 클릭 (추후 구현)
        holder.likeButton.setOnClickListener {
            // TODO: 좋아요 기능 구현
        }
    }

    override fun getItemCount(): Int = posts.size
}