package com.example.project

import android.os.Bundle
import android.util.Log // Log import 추가
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import okhttp3.*
import okio.ByteString
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.*
import org.json.JSONException // JSONException import 추가

class LiveTalkActivity : AppCompatActivity() {

    private lateinit var client: OkHttpClient
    private lateinit var webSocket: WebSocket
    private lateinit var messageRecyclerView: RecyclerView
    private lateinit var messageEditText: EditText
    private lateinit var sendButton: Button
    private lateinit var messageAdapter: MessageAdapter
    private val messages = mutableListOf<Message>()
    private val gson = Gson()

    // 안드로이드 에뮬레이터에서 로컬 스프링 서버에 접속할 주소
    private lateinit var serverUrl: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_live_talk)

        // 서버 URL 동적 설정
        val port = getString(R.string.server_port)
        val serverIp = getString(R.string.server_ip)
        val baseIp = if (serverIp == "auto") "10.0.2.2" else serverIp
        // 일반적인 WebSocket 엔드포인트들을 시도
        // 백엔드 설정에 따라 다음 중 하나를 사용:
        // serverUrl = "ws://$baseIp:$port/ws"
        // serverUrl = "ws://$baseIp:$port/websocket"
        // serverUrl = "ws://$baseIp:$port/chat"
        serverUrl = "ws://$baseIp:$port/ws"

        messageRecyclerView = findViewById(R.id.message_recycler_view)
        messageEditText = findViewById(R.id.message_edit_text)
        sendButton = findViewById(R.id.send_button)

        // 뒤로가기 버튼 설정
        findViewById<android.widget.ImageButton>(R.id.back_button).setOnClickListener {
            finish()
        }

        messageAdapter = MessageAdapter(messages)
        messageRecyclerView.layoutManager = LinearLayoutManager(this)
        messageRecyclerView.adapter = messageAdapter

        client = OkHttpClient()
        loadChatHistory() // 채팅 히스토리 먼저 로드
        connectWebSocket()

        sendButton.setOnClickListener {
            val messageContent = messageEditText.text.toString()
            if (messageContent.isNotEmpty()) {
                val message = Message("User", messageContent, Date().toString())
                sendMessage(message)
                messageEditText.text.clear()
            }
        }
    }

    private fun connectWebSocket() {
        val request = Request.Builder().url(serverUrl).build()
        val listener = object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.i("LiveTalkActivity", "WebSocket 연결 성공")
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                try {
                    val receivedMessage = gson.fromJson(text, Message::class.java)
                    runOnUiThread {
                        messages.add(receivedMessage)
                        messageAdapter.notifyItemInserted(messages.size - 1)
                        messageRecyclerView.scrollToPosition(messages.size - 1)
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
                webSocket.close(1000, null)
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                runOnUiThread {
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

    private fun sendMessage(message: Message) {
        val jsonMessage = gson.toJson(message)
        webSocket.send(jsonMessage)
    }

    private fun loadChatHistory() {
        Thread {
            try {
                val port = getString(R.string.server_port)
                val serverIp = getString(R.string.server_ip)
                val baseIp = if (serverIp == "auto") "10.0.2.2" else serverIp
                val url = URL("http://$baseIp:$port/api/chat/history")
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
                                        val message = Message(
                                            jsonObject.getString("sender"),
                                            content,
                                            jsonObject.getString("timestamp")
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
            }
        }.start()
    }

    override fun onDestroy() {
        webSocket.close(1000, null)
        super.onDestroy()
    }
}
