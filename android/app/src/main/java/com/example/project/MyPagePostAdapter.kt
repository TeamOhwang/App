package com.example.project

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

data class GridItem(
    val id: Long,
    val imgUrl: String?
)

class MyPageGridAdapter(
    private var items: MutableList<GridItem>
) : RecyclerView.Adapter<MyPageGridAdapter.VH>() {

    class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val iv: ImageView = itemView.findViewById(R.id.postimg)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_post, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        if (!item.imgUrl.isNullOrEmpty()) {
            Glide.with(holder.itemView.context)
                .load(item.imgUrl)
                .centerCrop()
                .placeholder(R.drawable.img_salad)
                .error(R.drawable.img_salad)
                .into(holder.iv)
        } else {
            holder.iv.setImageResource(R.drawable.img_salad)
        }
    }

    override fun getItemCount() = items.size

    fun update(newItems: List<GridItem>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }
}
