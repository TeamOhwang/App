package com.example.project

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class MessageAdapter(private val messages: List<Message>) : RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {

    class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // 내 메시지 뷰들
        val myMessageLayout: LinearLayout = itemView.findViewById(R.id.my_message_layout)
        val myContentTextView: TextView = itemView.findViewById(R.id.my_content_text_view)
        val myTimestampTextView: TextView = itemView.findViewById(R.id.my_timestamp_text_view)
        
        // 상대방 메시지 뷰들
        val otherMessageLayout: LinearLayout = itemView.findViewById(R.id.other_message_layout)
        val senderTextView: TextView = itemView.findViewById(R.id.sender_text_view)
        val contentTextView: TextView = itemView.findViewById(R.id.content_text_view)
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
            val date = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).parse(message.timestamp)
            timeFormat.format(date ?: Date())
        } catch (e: Exception) {
            message.timestamp.substring(message.timestamp.length - 8, message.timestamp.length - 3)
        }

        // "User"가 보낸 메시지는 내 메시지로 처리
        if (message.sender == "User") {
            // 내 메시지 표시
            holder.myMessageLayout.visibility = View.VISIBLE
            holder.otherMessageLayout.visibility = View.GONE
            
            holder.myContentTextView.text = message.content
            holder.myTimestampTextView.text = timeString
        } else {
            // 상대방 메시지 표시
            holder.myMessageLayout.visibility = View.GONE
            holder.otherMessageLayout.visibility = View.VISIBLE
            
            holder.senderTextView.text = message.sender
            holder.contentTextView.text = message.content
            holder.timestampTextView.text = timeString
        }
    }

    override fun getItemCount(): Int = messages.size
}