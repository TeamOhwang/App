package com.example.project

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CommentAdapter(private val comments: List<Comment>) :
    RecyclerView.Adapter<CommentAdapter.CommentViewHolder>() {

    // ViewHolder: item_comment.xml의 뷰들을 멤버 변수로 가집니다.
    inner class CommentViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivProfile: ImageView = view.findViewById(R.id.ivCommentProfile)
        val tvUsername: TextView = view.findViewById(R.id.tvCommentUsername)
        val tvTime: TextView = view.findViewById(R.id.tvCommentTime)
        val tvContent: TextView = view.findViewById(R.id.tvCommentContent)

        // 데이터를 뷰에 바인딩하는 함수
        fun bind(comment: Comment) {
            ivProfile.setImageResource(comment.profileImageRes)
            tvUsername.text = comment.username
            tvTime.text = comment.time
            tvContent.text = comment.content
        }
    }

    // ViewHolder를 생성하고 레이아웃을 인플레이트합니다.
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_comment, parent, false)
        return CommentViewHolder(view)
    }

    // ViewHolder에 데이터를 바인딩합니다.
    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        holder.bind(comments[position])
    }

    // 전체 아이템 개수를 반환합니다.
    override fun getItemCount(): Int {
        return comments.size
    }
}