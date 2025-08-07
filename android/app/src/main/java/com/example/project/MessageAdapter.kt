package com.example.project

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import java.text.SimpleDateFormat
import java.util.*

class MessageAdapter(
    private val messages: List<Message>,
    private val currentUserNickname: String = "User"
) : RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {

    class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // 내 메시지 뷰들
        val myMessageLayout: LinearLayout = itemView.findViewById(R.id.my_message_layout)
        val myContentTextView: TextView = itemView.findViewById(R.id.my_content_text_view)
        val myImageView: ImageView = itemView.findViewById(R.id.my_image_view)
        val myTimestampTextView: TextView = itemView.findViewById(R.id.my_timestamp_text_view)
        
        // 상대방 메시지 뷰들
        val otherMessageLayout: LinearLayout = itemView.findViewById(R.id.other_message_layout)
        val senderTextView: TextView = itemView.findViewById(R.id.sender_text_view)
        val contentTextView: TextView = itemView.findViewById(R.id.content_text_view)
        val otherImageView: ImageView = itemView.findViewById(R.id.other_image_view)
        val timestampTextView: TextView = itemView.findViewById(R.id.timestamp_text_view)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_message, parent, false)
        return MessageViewHolder(view)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val message = messages[position]
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val timeString = try {
            // 다양한 시간 포맷 처리
            val date = when {
                message.timestamp.contains("T") -> {
                    // ISO 8601 형식: 2024-01-01T12:30:45
                    SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).parse(message.timestamp)
                }
                message.timestamp.contains("GMT") -> {
                    // GMT 포함 형식 처리
                    val cleanTimestamp = message.timestamp.replace(Regex("\\s*GMT.*"), "")
                    SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy", Locale.ENGLISH).parse(cleanTimestamp)
                }
                else -> {
                    // 기본 Date().toString() 형식
                    SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH).parse(message.timestamp)
                }
            }
            timeFormat.format(date ?: Date())
        } catch (e: Exception) {
            // 파싱 실패 시 현재 시간 사용
            timeFormat.format(Date())
        }

        // 현재 사용자가 보낸 메시지는 내 메시지로 처리
        if (message.sender == currentUserNickname) {
            // 내 메시지 표시
            holder.myMessageLayout.visibility = View.VISIBLE
            holder.otherMessageLayout.visibility = View.GONE
            
            if (message.messageType == "image" && !message.imageUrl.isNullOrEmpty()) {
                // 이미지 메시지
                holder.myContentTextView.visibility = View.GONE
                holder.myImageView.visibility = View.VISIBLE
                Glide.with(holder.itemView.context)
                    .load(message.imageUrl)
                    .into(holder.myImageView)
            } else {
                // 텍스트 메시지
                holder.myContentTextView.visibility = View.VISIBLE
                holder.myImageView.visibility = View.GONE
                holder.myContentTextView.text = message.content
            }
            holder.myTimestampTextView.text = timeString
        } else {
            // 상대방 메시지 표시
            holder.myMessageLayout.visibility = View.GONE
            holder.otherMessageLayout.visibility = View.VISIBLE
            
            holder.senderTextView.text = message.sender
            
            if (message.messageType == "image" && !message.imageUrl.isNullOrEmpty()) {
                // 이미지 메시지
                holder.contentTextView.visibility = View.GONE
                holder.otherImageView.visibility = View.VISIBLE
                Glide.with(holder.itemView.context)
                    .load(message.imageUrl)
                    .into(holder.otherImageView)
            } else {
                // 텍스트 메시지
                holder.contentTextView.visibility = View.VISIBLE
                holder.otherImageView.visibility = View.GONE
                holder.contentTextView.text = message.content
            }
            holder.timestampTextView.text = timeString
        }
    }

    override fun getItemCount(): Int = messages.size
}