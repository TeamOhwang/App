package com.example.project

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.net.Uri
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import com.bumptech.glide.Glide

class NewPostActivity : AppCompatActivity() {

    // 갤러리에서 이미지를 선택했을 때 결과를 처리할 ActivityResultLauncher
    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        // uri가 null이 아닐 경우에만 실행
        uri?.let {
            // 선택된 이미지를 ImageView에 설정
            val imageView: ImageView = findViewById(R.id.iv_add_image)
            //imageView.setImageURI(it)
            Glide.with(this)
                .load(it)
                .into(imageView)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_post)

        val addImageButton: ImageView = findViewById(R.id.iv_add_image)

        // 이미지뷰를 클릭했을 때의 동작 설정
        addImageButton.setOnClickListener {
            // 갤러리를 열어 이미지를 선택하도록 함
            openGallery()
        }
    }

    private fun openGallery() {
        // "image/*" 타입의 콘텐츠를 선택하도록 갤러리를 실행
        pickImageLauncher.launch("image/*")
    }
}