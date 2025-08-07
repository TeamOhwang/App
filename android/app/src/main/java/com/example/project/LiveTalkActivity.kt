package com.example.project

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.project.util.SessionManager
import com.google.gson.Gson
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import okio.ByteString
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.*
import org.json.JSONException
import java.io.FileOutputStream
import java.io.InputStream

class LiveTalkActivity : AppCompatActivity() {

    private lateinit var client: OkHttpClient
    private lateinit var webSocket: WebSocket
    private lateinit var messageRecyclerView: RecyclerView
    private lateinit var messageEditText: EditText
    private lateinit var sendButton: Button
    private lateinit var imageButton: ImageButton
    private lateinit var messageAdapter: MessageAdapter
    private val messages = mutableListOf<Message>()
    private val gson = Gson()
    private var isWebSocketConnected = false
    private lateinit var sessionManager: SessionManager
    private var currentUserNickname: String = "User"

    // 안드로이드 에뮬레이터에서 로컬 스프링 서버에 접속할 주소
    private lateinit var serverUrl: String
    private lateinit var httpUrl: String

    // 이미지 선택을 위한 ActivityResultLauncher
    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                handleSelectedImage(uri)
            }
        }
    }

    // 권한 요청을 위한 ActivityResultLauncher
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Log.d("LiveTalkActivity", "권한 요청 성공: 갤러리 열기")
            openImagePicker()
        } else {
            Log.w("LiveTalkActivity", "권한 요청 거부")
            Toast.makeText(this, "갤러리 접근 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_live_talk)

        // 서버 URL 동적 설정
        val port = getString(R.string.server_port)
        val serverIp = getString(R.string.server_ip)
        val baseIp = if (serverIp == "auto") {
            // 에뮬레이터에서 로컬 서버 접속용
            "10.0.2.2"
        } else {
            // 실제 서버 IP 주소 사용
            serverIp
        }
        serverUrl = "ws://$baseIp:$port/ws"
        httpUrl = "http://$baseIp:$port"
        Log.i("LiveTalkActivity", "서버 연결 URL: $serverUrl")

        messageRecyclerView = findViewById(R.id.message_recycler_view)
        messageEditText = findViewById(R.id.message_edit_text)
        sendButton = findViewById(R.id.send_button)
        imageButton = findViewById(R.id.image_button)

        // 뒤로가기 버튼 설정
        findViewById<android.widget.ImageButton>(R.id.back_button).setOnClickListener {
            finish()
        }

        messageRecyclerView.layoutManager = LinearLayoutManager(this)

        // 초기 상태에서 전송 버튼 비활성화
        sendButton.isEnabled = false

        // SessionManager 초기화
        sessionManager = SessionManager.getInstance(this)

        // 현재 사용자 정보 가져오기
        getCurrentUserInfo()

        sendButton.setOnClickListener {
            sendTextMessage()
        }

        // Enter 키로 메시지 전송 - EditorActionListener
        messageEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                sendTextMessage()
                true
            } else {
                false
            }
        }

        // 하드웨어 키보드 Enter 키 처리
        messageEditText.setOnKeyListener { _, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_ENTER) {
                when (event.action) {
                    KeyEvent.ACTION_DOWN -> {
                        // Enter 키를 눌렀을 때 메시지 전송
                        sendTextMessage()
                        true
                    }
                    else -> false
                }
            } else {
                false
            }
        }

        Log.d("LiveTalkActivity", "이미지 버튼 클릭 리스너 설정 시작")
        imageButton.setOnClickListener {
            Log.d("LiveTalkActivity", "이미지 버튼 클릭됨")
            checkPermissionAndOpenGallery()
        }
    }

    private fun connectWebSocket() {
        val request = Request.Builder().url(serverUrl).build()
        val listener = object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.i("LiveTalkActivity", "WebSocket 연결 성공")
                isWebSocketConnected = true
                runOnUiThread {
                    sendButton.isEnabled = true
                }
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                Log.d("LiveTalkActivity", "WebSocket 메시지 수신: $text")
                try {
                    val receivedMessage = gson.fromJson(text, Message::class.java)
                    runOnUiThread {
                        // 시스템 연결 메시지는 무시
                        if (receivedMessage.sender == "System" && receivedMessage.content == "채팅방에 연결되었습니다.") {
                            Log.d("LiveTalkActivity", "시스템 연결 메시지 무시")
                            return@runOnUiThread
                        }

                        // 중복 메시지 방지 (내용, 발신자, 시간, 메시지 타입으로 비교)
                        val isDuplicate = messages.any { existingMessage ->
                            existingMessage.content == receivedMessage.content &&
                                    existingMessage.sender == receivedMessage.sender &&
                                    existingMessage.messageType == receivedMessage.messageType &&
                                    // 이미지 메시지의 경우 imageUrl도 비교
                                    if (receivedMessage.messageType == "image") {
                                        existingMessage.imageUrl == receivedMessage.imageUrl
                                    } else {
                                        existingMessage.timestamp == receivedMessage.timestamp
                                    }
                        }

                        if (!isDuplicate) {
                            messages.add(receivedMessage)
                            messageAdapter.notifyItemInserted(messages.size - 1)
                            messageRecyclerView.scrollToPosition(messages.size - 1)
                            Log.d("LiveTalkActivity", "새 메시지 추가: ${receivedMessage.sender}: ${receivedMessage.content}")
                        } else {
                            Log.d("LiveTalkActivity", "중복 메시지 무시: ${receivedMessage.content}")
                        }
                    }
                } catch (e: Exception) {
                    Log.e("LiveTalkActivity", "WebSocket 메시지 파싱 오류: ${e.message}", e)
                    runOnUiThread {
                        messages.add(Message("System", "메시지 파싱 오류: ${e.message}", Date().toString()))
                        messageAdapter.notifyItemInserted(messages.size - 1)
                    }
                }
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                Log.i("LiveTalkActivity", "WebSocket 연결 종료 중: Code $code, Reason: $reason")
                isWebSocketConnected = false
                runOnUiThread {
                    sendButton.isEnabled = false
                }
                webSocket.close(1000, null)
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                isWebSocketConnected = false
                runOnUiThread {
                    sendButton.isEnabled = false
                    val errorMsg = if (response != null) {
                        "연결 실패: HTTP ${response.code} - ${t.message}"
                    } else {
                        "연결 실패: ${t.message}"
                    }
                    Log.e("LiveTalkActivity", "WebSocket 연결 실패: $errorMsg", t)
                    messages.add(Message("System", "$errorMsg\n서버 URL: $serverUrl", Date().toString()))
                    messageAdapter.notifyItemInserted(messages.size - 1)
                }
            }
        }

        webSocket = client.newWebSocket(request, listener)
    }

    private fun sendTextMessage() {
        val messageContent = messageEditText.text.toString().trim()
        if (messageContent.isNotEmpty()) {
            val message = Message(currentUserNickname, messageContent, Date().toString())
            sendMessage(message)
            messageEditText.text.clear()
        }
    }

    private fun sendMessage(message: Message) {
        if (isWebSocketConnected) {
            val jsonMessage = gson.toJson(message)
            Log.d("LiveTalkActivity", "메시지 전송: $jsonMessage")
            webSocket.send(jsonMessage)
        } else {
            Log.w("LiveTalkActivity", "WebSocket 연결되지 않음 - 메시지 전송 실패")
            runOnUiThread {
                messages.add(Message("System", "연결이 끊어져 메시지를 전송할 수 없습니다.", Date().toString()))
                messageAdapter.notifyItemInserted(messages.size - 1)
            }
        }
    }

    private fun loadChatHistory() {
        Thread {
            try {
                val port = getString(R.string.server_port)
                val serverIp = getString(R.string.server_ip)
                val baseIp = if (serverIp == "auto") {
                    "10.0.2.2"
                } else {
                    serverIp
                }
                val url = URL("http://$baseIp:$port/api/chat/history")
                Log.i("LiveTalkActivity", "채팅 히스토리 요청 URL: $url")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connectTimeout = 5000
                connection.readTimeout = 5000

                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val reader = BufferedReader(InputStreamReader(connection.inputStream))
                    val response = reader.readText()
                    reader.close()

                    try {
                        val jsonArray = org.json.JSONArray(response)
                        runOnUiThread {
                            for (i in 0 until jsonArray.length()) {
                                val jsonObject = jsonArray.getJSONObject(i)

                                try {
                                    val content = jsonObject.getString("content")
                                    // 시스템 연결 메시지 필터링
                                    if (content != "채팅방에 연결되었습니다.") {
                                        val messageType = jsonObject.optString("messageType", "text")
                                        val imageUrl = jsonObject.optString("imageUrl", null)

                                        val message = Message(
                                            jsonObject.getString("sender"),
                                            content,
                                            jsonObject.getString("timestamp"),
                                            messageType,
                                            imageUrl
                                        )
                                        messages.add(message)
                                    }
                                } catch (jsonEx: JSONException) {
                                    Log.e("LiveTalkActivity", "채팅 히스토리 개별 메시지 파싱 오류: ${jsonEx.message}. JSON: $jsonObject", jsonEx)
                                }
                            }
                            messageAdapter.notifyDataSetChanged()
                            if (messages.isNotEmpty()) {
                                messageRecyclerView.scrollToPosition(messages.size - 1)
                            }
                        }
                    } catch (jsonArrayEx: JSONException) {
                        Log.e("LiveTalkActivity", "채팅 히스토리 전체 JSON 배열 파싱 오류: ${jsonArrayEx.message}. 응답: $response", jsonArrayEx)
                        runOnUiThread {
                            messages.add(Message("System", "채팅 히스토리 JSON 오류: ${jsonArrayEx.message}", ""))
                            messageAdapter.notifyDataSetChanged()
                        }
                    }
                } else {
                    Log.e("LiveTalkActivity", "채팅 히스토리 HTTP 요청 실패: 응답 코드 $responseCode")
                    val errorStream = connection.errorStream
                    val errorResponse = if (errorStream != null) BufferedReader(InputStreamReader(errorStream)).readText() else "No error stream"
                    Log.e("LiveTalkActivity", "HTTP 에러 응답: $errorResponse")
                    runOnUiThread {
                        messages.add(Message("System", "채팅 히스토리 로드 실패: HTTP $responseCode", ""))
                        messageAdapter.notifyDataSetChanged()
                    }
                }
                connection.disconnect()
            } catch (e: Exception) {
                Log.e("LiveTalkActivity", "채팅 히스토리 로드 중 네트워크/일반 오류: ${e.message}", e)
                runOnUiThread {
                    messages.add(Message("System", "채팅 히스토리 로드 실패: ${e.message}", ""))
                    messageAdapter.notifyDataSetChanged()
                }
                // WebSocket이 연결되어 있지 않으면 연결 시도
                if (!isWebSocketConnected) {
                    runOnUiThread {
                        Toast.makeText(this, "채팅 히스토리 로드 실패. 다시 연결 중...", Toast.LENGTH_SHORT).show()
                    }
                    connectWebSocket()
                }
            }
        }.start()
    }

    private fun getCurrentUserInfo() {
        sessionManager.getCurrentUser { success, error, userInfo ->
            if (success && userInfo != null) {
                currentUserNickname = userInfo["nickname"] as? String ?: "User"
                Log.d("LiveTalkActivity", "현재 사용자: $currentUserNickname")

                // 사용자 정보를 가져온 후 UI 초기화
                runOnUiThread {
                    initializeChat()
                }
            } else {
                Log.e("LiveTalkActivity", "사용자 정보 가져오기 실패: $error")
                // 로그인이 필요한 경우 LoginActivity로 이동
                runOnUiThread {
                    finish()
                }
            }
        }
    }

    private fun initializeChat() {
        messageAdapter = MessageAdapter(messages, currentUserNickname)
        messageRecyclerView.adapter = messageAdapter

        client = OkHttpClient()
        loadChatHistory() // 채팅 히스토리 먼저 로드
        connectWebSocket()
    }

    /**
     * Android 버전에 따라 갤러리 접근 권한을 확인하고 요청합니다.
     * Android 13 (API 33) 이상에서는 READ_MEDIA_IMAGES 권한을 사용합니다.
     * 그 이하 버전에서는 READ_EXTERNAL_STORAGE 권한을 사용합니다.
     */
    private fun checkPermissionAndOpenGallery() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        when {
            ContextCompat.checkSelfPermission(
                this,
                permission
            ) == PackageManager.PERMISSION_GRANTED -> {
                // 권한이 이미 허용된 경우 갤러리 열기
                Log.d("LiveTalkActivity", "갤러리 권한 이미 부여됨. 갤러리 열기.")
                openImagePicker()
            }
            else -> {
                // 권한이 없는 경우 요청
                Log.d("LiveTalkActivity", "갤러리 권한 요청 시작.")
                permissionLauncher.launch(permission)
            }
        }
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        imagePickerLauncher.launch(intent)
    }

    // 이미지 전송 로직 수정: 이미지 URI를 서버에 업로드하고 URL을 받아서 메시지로 전송
    private fun handleSelectedImage(uri: Uri) {

        // 스레드에서 이미지 업로드 및 메시지 전송 처리
        Thread {
            try {
                // URI로부터 파일 경로 가져오기 (임시 파일 생성)
                val file = uriToFile(uri)
                if (file == null) {
                    runOnUiThread {
                        Log.e("LiveTalkActivity", "handleSelectedImage: URI to File conversion failed")
                        Toast.makeText(this, "이미지 파일을 읽을 수 없습니다.", Toast.LENGTH_SHORT).show()
                    }
                    return@Thread
                }
                Log.d("LiveTalkActivity", "handleSelectedImage: 파일 변환 성공, 파일 경로: ${file.absolutePath}")

                // 이미지 업로드
                uploadImageToServer(file) { imageUrl ->
                    if (imageUrl != null) {
                        // 업로드 성공 시, 이미지 URL을 포함한 메시지 전송
                        val imageMessage = Message(
                            sender = currentUserNickname,
                            content = "", // 이미지 메시지는 content를 비워둡니다.
                            timestamp = Date().toString(),
                            messageType = "image",
                            imageUrl = imageUrl
                        )
                        sendMessage(imageMessage)
                    } else {
                        // 업로드 실패 시 사용자에게 알림
                        runOnUiThread {
                            Toast.makeText(this, "이미지 업로드에 실패했습니다.", Toast.LENGTH_SHORT).show()
                        }
                    }
                    // 임시 파일 삭제
                    Log.d("LiveTalkActivity", "handleSelectedImage: 임시 파일 삭제 시도: ${file.absolutePath}")
                    file.delete()
                }

            } catch (e: Exception) {
                Log.e("LiveTalkActivity", "이미지 처리 오류: ${e.message}", e)
                runOnUiThread {
                    Toast.makeText(this, "이미지를 처리할 수 없습니다.", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }

    private fun uriToFile(uri: Uri): File? {
        val contentResolver = this.contentResolver
        try {
            val inputStream: InputStream? = contentResolver.openInputStream(uri)
            if (inputStream != null) {
                val tempFile = File(this.cacheDir, "temp_image_${System.currentTimeMillis()}.jpg")
                val outputStream = FileOutputStream(tempFile)
                inputStream.use { input ->
                    outputStream.use { output ->
                        input.copyTo(output)
                    }
                }
                Log.d("LiveTalkActivity", "uriToFile: 파일 변환 성공: ${tempFile.absolutePath}")
                return tempFile
            }
        } catch (e: Exception) {
            Log.e("LiveTalkActivity", "URI to File conversion failed: ${e.message}", e)
        }
        return null
    }

    private fun uploadImageToServer(file: File, callback: (String?) -> Unit) {
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("file", file.name, file.asRequestBody("image/jpeg".toMediaTypeOrNull()))
            .build()

        val url = "$httpUrl/api/upload/image"

        Log.d("LiveTalkActivity", "uploadImageToServer: 이미지 업로드 시작. URL: $url")
        Log.d("LiveTalkActivity", "uploadImageToServer: 파일명: ${file.name}, 파일 크기: ${file.length()} bytes")

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: java.io.IOException) {
                Log.e("LiveTalkActivity", "uploadImageToServer: 이미지 업로드 실패", e)
                callback(null)
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    try {
                        val responseBody = response.body?.string()
                        Log.d("LiveTalkActivity", "uploadImageToServer: 성공 응답 수신. 응답 본문: $responseBody")
                        if (responseBody != null) {
                            // 서버 응답이 JSON 형식일 경우, JSON 객체에서 "url" 필드 값을 추출
                            val jsonObject = org.json.JSONObject(responseBody)
                            val imageUrl = jsonObject.getString("url")
                            Log.i("LiveTalkActivity", "uploadImageToServer: 이미지 업로드 성공. URL: $imageUrl")
                            callback(imageUrl)
                        } else {
                            Log.e("LiveTalkActivity", "uploadImageToServer: 이미지 업로드 실패: 응답 본문이 비어있습니다.")
                            callback(null)
                        }
                    } catch (e: JSONException) {
                        // JSON 파싱 실패 시, 응답 본문을 그대로 URL로 시도
                        val imageUrl = response.body?.string()
                        Log.w("LiveTalkActivity", "uploadImageToServer: 이미지 업로드 응답 JSON 파싱 실패, URL을 직접 사용합니다. URL: $imageUrl")
                        callback(imageUrl)
                    }
                } else {
                    Log.e("LiveTalkActivity", "uploadImageToServer: 이미지 업로드 실패: HTTP ${response.code} ${response.message}")
                    val errorBody = response.body?.string()
                    Log.e("LiveTalkActivity", "uploadImageToServer: 에러 응답: $errorBody")
                    callback(null)
                }
            }
        })
    }


    override fun onDestroy() {
        if (isWebSocketConnected) {
            webSocket.close(1000, "Activity destroyed")
        }
        client.dispatcher.executorService.shutdown()
        super.onDestroy()
    }
}
