package com.example.project

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter


class PostAdapter(
    private val onPostClick: (Post) -> Unit
) : ListAdapter<Post, PostAdapter.PostViewHolder>(PostDiffCallback()) {

    class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.postimg)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_post, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = getItem(position)

        // 현재 Post 클래스 구조에 맞게 수정
        holder.imageView.setImageResource(post.imageRes)
        
        // 게시물 클릭 리스너 추가
        holder.itemView.setOnClickListener {
            onPostClick(post)
        }
    }
}

// DiffUtil을 사용한 효율적인 리스트 업데이트
class PostDiffCallback : DiffUtil.ItemCallback<Post>() {
    override fun areItemsTheSame(oldItem: Post, newItem: Post): Boolean {
        // Post 클래스에 id 필드가 추가되었으므로 id로 비교
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Post, newItem: Post): Boolean {
        return oldItem == newItem
    }
}



