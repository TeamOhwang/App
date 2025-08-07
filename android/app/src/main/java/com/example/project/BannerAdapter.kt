package com.example.project

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView

// 배너 이미지를 표시할 RecyclerView 어댑터
// 생성자로 배너로 사용할 이미지 리소스 ID 목록을 받습니다.
class BannerAdapter(private val bannerItems: List<Int>) : RecyclerView.Adapter<BannerAdapter.BannerViewHolder>() {

    // 뷰 홀더 클래스: 각 배너 아이템의 뷰를 보관합니다.
    inner class BannerViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.imageViewBanner)
    }

    // 새로운 뷰 홀더를 생성할 때 호출됩니다.
    // item_banner.xml 레이아웃을 인플레이트하여 뷰 홀더를 생성합니다.
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BannerViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_banner, parent, false)
        return BannerViewHolder(view)
    }

    // 뷰 홀더에 데이터를 바인딩할 때 호출됩니다.
    // 현재 위치(position)에 해당하는 이미지 리소스를 ImageView에 설정합니다.
    override fun onBindViewHolder(holder: BannerViewHolder, position: Int) {
        holder.imageView.setImageResource(bannerItems[position])
    }

    // 데이터 세트의 크기를 반환합니다.
    override fun getItemCount(): Int {
        return bannerItems.size
    }
}
