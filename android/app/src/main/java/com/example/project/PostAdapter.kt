package com.example.project

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions

class PostAdapter : ListAdapter<Post, PostAdapter.PostViewHolder>(PostDiffCallback()) {

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

        // 🔥 개선된 Glide 사용법
        Glide.with(holder.itemView.context)
            .load(post.imageUrl)
            .apply(RequestOptions()
//                .placeholder(R.drawable.ic_placeholder) // 로딩 중 이미지
//                .error(R.drawable.ic_error) // 에러 시 이미지
                .centerCrop() // 이미지 크기 조정
                .diskCacheStrategy(DiskCacheStrategy.ALL) // 캐싱 전략
            )
            .into(holder.imageView)
    }
}

// DiffUtil을 사용한 효율적인 리스트 업데이트
class PostDiffCallback : DiffUtil.ItemCallback<Post>() {
    override fun areItemsTheSame(oldItem: Post, newItem: Post): Boolean {
        return oldItem.id == newItem.id // Post 객체에 id가 있다고 가정
    }

    override fun areContentsTheSame(oldItem: Post, newItem: Post): Boolean {
        return oldItem == newItem
    }
}


//class PostAdapter : RecyclerView.Adapter<PostAdapter.PostViewHolder>() {
//
//    private var posts = listOf<Post>()
//
//    fun submitList(list: List<Post>) {
//        posts = list
//        notifyDataSetChanged()
//    }
//
//    class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
//        val imageView: ImageView = itemView.findViewById(R.id.postimg)
//    }
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
//        val view = LayoutInflater.from(parent.context)
//            .inflate(R.layout.item_post, parent, false)
//        return PostViewHolder(view)
//    }
//
//    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
//        val post = posts[position]
//        // Glide 등 이미지 라이브러리로 이미지 로딩
//        Glide.with(holder.itemView.context)
//            .load(post.imageUrl)
//            .apply(RequestOptions()
//                .centerCrop()
//                .diskCacheStrategy(DiskCacheStrategy.ALL)
//            )
//            .into(holder.imageView)
//
//    }
//
//    override fun getItemCount(): Int = posts.size
//}

