package com.example.project

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter

// MyPage에서 사용할 간단한 게시물 데이터 클래스
data class MyPagePost(
    val id: Long,
    val imageRes: Int
)

class MyPagePostAdapter : ListAdapter<MyPagePost, MyPagePostAdapter.PostViewHolder>(MyPagePostDiffCallback()) {

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
        holder.imageView.setImageResource(post.imageRes)
    }
}

// DiffUtil을 사용한 효율적인 리스트 업데이트
class MyPagePostDiffCallback : DiffUtil.ItemCallback<MyPagePost>() {
    override fun areItemsTheSame(oldItem: MyPagePost, newItem: MyPagePost): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: MyPagePost, newItem: MyPagePost): Boolean {
        return oldItem == newItem
    }
}