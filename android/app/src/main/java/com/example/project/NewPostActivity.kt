package com.example.project

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.net.Uri
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.bumptech.glide.Glide
import com.example.project.util.SessionManager
import com.example.project.R
import java.util.*
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.io.InputStream

class NewPostActivity : AppCompatActivity() {

    // UI 요소 선언
    private lateinit var btnBack: Button
    private lateinit var userProfileImg: ImageView
    private lateinit var userNick: TextView
    private lateinit var ivAddImage: ImageView
    private lateinit var newContent: EditText
    private lateinit var btnPosting: Button

    private lateinit var sessionManager: SessionManager
    private var currentUserNickname: String = "User"
    private var selectedImageUri: Uri? = null
    private var currentUserId: String = "guest" // 사용자 ID를 저장할 변수
    
    private lateinit var client: OkHttpClient
    private val baseUrl = "http://192.168.219.180:8081/api"

    // 갤러리에서 이미지를 선택했을 때 결과를 처리할 ActivityResultLauncher
    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            // 선택된 이미지를 클래스 변수에 저장
            selectedImageUri = it
            // Glide 라이브러리를 사용하여 이미지를 로드하고 ImageView에 표시
            Glide.with(this)
                .load(it)
                .into(ivAddImage)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_post)

        // UI 요소 초기화
        btnBack = findViewById(R.id.btnBack)
        userProfileImg = findViewById(R.id.userProfileImg)
        userNick = findViewById(R.id.userNick)
        ivAddImage = findViewById(R.id.iv_add_image)
        newContent = findViewById(R.id.newContent)
        btnPosting = findViewById(R.id.btnPosting)

        // SessionManager 초기화
        sessionManager = SessionManager.getInstance(this)
        
        // SessionManager의 HttpClient 사용 (쿠키 포함)
        client = sessionManager.getHttpClient()

        // 현재 사용자 정보 가져오기 및 UI 업데이트
        getCurrentUserInfo()

        // 뒤로가기 버튼 클릭 리스너
        btnBack.setOnClickListener {
            finish()
        }

        // 이미지 추가 ImageView 클릭 리스너
        ivAddImage.setOnClickListener {
            openGallery()
        }

        // 게시물 등록 버튼 클릭 리스너
        btnPosting.setOnClickListener {
            // 게시물 등록 로직 실행
            uploadPost()
        }
    }

    /**
     * 현재 로그인된 사용자의 정보를 가져와 UI에 표시합니다.
     */
    private fun getCurrentUserInfo() {
        sessionManager.getCurrentUser { success, error, userInfo ->
            // UI 업데이트는 메인 스레드에서 실행
            runOnUiThread {
                if (success && userInfo != null) {
                    // SessionManager에서 반환하는 키 이름에 맞게 수정
                    currentUserId = userInfo["userId"]?.toString() ?: "guest"
                    currentUserNickname = userInfo["nickname"] as? String ?: "User"
                    userNick.text = currentUserNickname
                    
                    // 프로필 이미지 로드 (메인 스레드에서 실행)
                    val profileImageUrl = userInfo["profileImage"] as? String
                    if (!profileImageUrl.isNullOrEmpty()) {
                        Glide.with(this@NewPostActivity)
                            .load(profileImageUrl)
                            .circleCrop()
                            .into(userProfileImg)
                    }
                    
                    Log.d("NewPostActivity", "현재 사용자 ID: $currentUserId, 닉네임: $currentUserNickname")
                } else {
                    Log.e("NewPostActivity", "사용자 정보 가져오기 실패: $error")
                    Toast.makeText(this@NewPostActivity, "사용자 정보를 가져올 수 없습니다", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun openGallery() {
        pickImageLauncher.launch("image/*")
    }

    /**
     * 게시물 등록 로직을 처리하는 함수
     */
    private fun uploadPost() {
        val postContent = newContent.text.toString().trim()

        if (selectedImageUri == null && postContent.isEmpty()) {
            Toast.makeText(this, "이미지 또는 내용을 입력해주세요.", Toast.LENGTH_SHORT).show()
            return
        }

        // TODO: 로딩 인디케이터 표시 (예: ProgressBar)
        Toast.makeText(this, "게시물 등록 중...", Toast.LENGTH_SHORT).show()

        // 1. 이미지를 서버에 업로드하고 URL을 받아오는 로직
        // 이 부분은 사용자님의 백엔드 구현에 따라 달라집니다.
        // 예를 들어 Firebase Storage를 사용하면 아래와 같은 로직이 필요합니다.
        if (selectedImageUri != null) {
            uploadImage(selectedImageUri!!)
        } else {
            // 이미지가 없는 경우, 게시물만 DB에 저장
            uploadPostToDatabase(postContent, null)
        }
    }

    /**
     * 이미지를 서버에 업로드하는 함수
     */
    private fun uploadImage(imageUri: Uri) {
        Log.d("NewPostActivity", "이미지 업로드 시작: $imageUri")
        
        try {
            // URI에서 실제 파일을 생성
            val inputStream: InputStream? = contentResolver.openInputStream(imageUri)
            val tempFile = File.createTempFile("upload", ".jpg", cacheDir)
            tempFile.outputStream().use { fileOut ->
                inputStream?.copyTo(fileOut)
            }
            
            // Multipart 요청 생성
            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart(
                    "file",
                    tempFile.name,
                    tempFile.asRequestBody("image/*".toMediaType())
                )
                .build()
            
            val request = Request.Builder()
                .url("$baseUrl/upload/image")
                .post(requestBody)
                .build()
            
            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    runOnUiThread {
                        Log.e("NewPostActivity", "이미지 업로드 실패", e)
                        Toast.makeText(this@NewPostActivity, "이미지 업로드 실패: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
                
                override fun onResponse(call: Call, response: Response) {
                    response.use {
                        if (response.isSuccessful) {
                            val responseBody = response.body?.string()
                            val jsonObject = JSONObject(responseBody ?: "{}")
                            val imageUrl = jsonObject.getString("url")
                            
                            Log.d("NewPostActivity", "이미지 업로드 완료, URL: $imageUrl")
                            
                            // UI 스레드에서 게시물 저장
                            runOnUiThread {
                                val postContent = newContent.text.toString().trim()
                                uploadPostToDatabase(postContent, imageUrl)
                            }
                        } else {
                            runOnUiThread {
                                Log.e("NewPostActivity", "이미지 업로드 실패: ${response.code}")
                                Toast.makeText(this@NewPostActivity, "이미지 업로드 실패", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                    
                    // 임시 파일 삭제
                    tempFile.delete()
                }
            })
            
        } catch (e: Exception) {
            Log.e("NewPostActivity", "이미지 업로드 중 오류", e)
            Toast.makeText(this, "이미지 업로드 중 오류가 발생했습니다", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * 게시물 데이터를 데이터베이스에 저장하는 함수
     * @param content 게시물 내용
     * @param imageUrl 업로드된 이미지 URL (없을 경우 null)
     */
    private fun uploadPostToDatabase(content: String, imageUrl: String?) {
        Log.d("NewPostActivity", "게시물 데이터 DB 저장 요청 - content: $content, imageUrl: $imageUrl")
        
        // 세션 정보 디버깅
        Log.d("NewPostActivity", "현재 쿠키 개수: ${sessionManager.getCookieCount()}")
        Log.d("NewPostActivity", "세션 쿠키: ${sessionManager.getSessionCookie()?.value}")
        Log.d("NewPostActivity", "쿠키 헤더: ${sessionManager.getCookieHeader(baseUrl)}")
        
        // JSON 데이터 생성
        val jsonObject = JSONObject().apply {
            put("content", content)
            if (imageUrl != null) {
                put("imgUrl", imageUrl)
            }
        }
        
        val requestBody = jsonObject.toString().toRequestBody("application/json".toMediaType())
        
        val request = Request.Builder()
            .url("$baseUrl/post")
            .post(requestBody)
            .build()
        
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Log.e("NewPostActivity", "게시물 저장 실패", e)
                    Toast.makeText(this@NewPostActivity, "게시물 저장 실패: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
            
            override fun onResponse(call: Call, response: Response) {
                response.use {
                    val responseBody = response.body?.string()
                    Log.d("NewPostActivity", "게시물 저장 응답 코드: ${response.code}")
                    Log.d("NewPostActivity", "게시물 저장 응답 본문: $responseBody")
                    
                    runOnUiThread {
                        if (response.isSuccessful) {
                            try {
                                val jsonResponse = JSONObject(responseBody ?: "{}")
                                val success = jsonResponse.optBoolean("success", false)
                                val message = jsonResponse.optString("message", "게시물 등록 완료!")
                                
                                if (success) {
                                    Toast.makeText(this@NewPostActivity, message, Toast.LENGTH_SHORT).show()
                                    finish() // Activity 종료
                                } else {
                                    Toast.makeText(this@NewPostActivity, "게시물 저장 실패: $message", Toast.LENGTH_SHORT).show()
                                }
                            } catch (e: Exception) {
                                Toast.makeText(this@NewPostActivity, "게시물 등록 완료!", Toast.LENGTH_SHORT).show()
                                finish()
                            }
                        } else {
                            Log.e("NewPostActivity", "게시물 저장 실패: ${response.code}")
                            if (response.code == 401) {
                                Toast.makeText(this@NewPostActivity, "로그인이 필요합니다", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(this@NewPostActivity, "게시물 저장 실패", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            }
        })
    }
}
