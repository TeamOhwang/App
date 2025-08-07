package com.example.project

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
            .inflate(R.layout.activity_post_detail, parent, false) // activity_post_detail.xml 사용

        // 하단 네비게이션 숨기기 (RecyclerView 아이템에서는 불필요)
        val bottomNav = view.findViewById<View>(R.id.bottomNav)
        bottomNav?.visibility = View.GONE

        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = posts[position]

        // 기본 정보 설정
        holder.username.text = post.username
        holder.likeCount.text = "좋아요 ${post.likeCount}개"
        holder.description.text = post.description

        // 프로필 이미지 로딩 (현재는 기본 이미지 사용, 추후 실제 프로필 이미지 URL 받으면 Glide로 로딩)
        holder.profileImage.setImageResource(post.profileImageRes)

        // 게시글 이미지 로딩 (실제 이미지 URL이 있다면 Glide 사용, 없으면 기본 이미지)
        // 추후 Post 클래스에 실제 imgUrl 필드 추가하여 사용
        // 현재는 기본 이미지 사용
        holder.postImage.setImageResource(post.imageRes)

        // 실제 서버 이미지 URL 로딩 예시 (Post 클래스에 imgUrl 필드 추가시 사용)

        if (!post.imgUrl.isNullOrEmpty()) {
            Glide.with(holder.itemView.context)
                .load(post.imgUrl)
                .placeholder(R.drawable.img_salad) // 로딩 중 표시할 기본 이미지
                .error(R.drawable.img_salad) // 로딩 실패시 표시할 기본 이미지
                .into(holder.postImage)
        } else {
            holder.postImage.setImageResource(R.drawable.img_salad)
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