package com.example.project

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : AppCompatActivity() {
    private lateinit var messageTextView: TextView
    private lateinit var fetchButton: Button
    private lateinit var dbTestButton: Button
    private lateinit var showTestMessagesButton: Button
    private lateinit var liveTalkButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        messageTextView = findViewById(R.id.messageTextView)
        fetchButton = findViewById(R.id.fetchButton)
        dbTestButton = findViewById(R.id.dbTestButton)
        showTestMessagesButton = findViewById(R.id.showTestMessagesButton)
        liveTalkButton = findViewById(R.id.liveTalkButton)

        fetchButton.setOnClickListener {
            fetchMessageFromServer()
        }

        dbTestButton.setOnClickListener {
            testDatabaseConnection()
        }

        showTestMessagesButton.setOnClickListener {
            showTestMessages()
        }

        liveTalkButton.setOnClickListener {
            // 바로 채팅창으로 이동
            startActivity(android.content.Intent(this, LiveTalkActivity::class.java))
        }
    }

    private fun fetchMessageFromServer() {
        CoroutineScope(Dispatchers.IO).launch {
            val port = getString(R.string.server_port)
            val serverIp = getString(R.string.server_ip)

            // URL 목록 생성
            val urls = mutableListOf<String>()

            if (serverIp == "auto") {
                // 자동 모드: 에뮬레이터와 실제 기기 모두 시도
                urls.add("http://10.0.2.2:$port/api/message")  // 에뮬레이터용
                urls.add("http://127.0.0.1:$port/api/message")  // 실제 기기용
            } else {
                // 수동 설정된 IP 사용
                urls.add("http://$serverIp:$port/api/message")
            }

            var success = false
            var lastError = ""

            withContext(Dispatchers.Main) {
                messageTextView.text = "서버 연결 중..."
            }

            for (urlString in urls) {
                try {
                    val url = URL(urlString)
                    val connection = url.openConnection() as HttpURLConnection
                    connection.requestMethod = "GET"
                    connection.connectTimeout = 5000
                    connection.readTimeout = 5000

                    val responseCode = connection.responseCode
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        val reader = BufferedReader(InputStreamReader(connection.inputStream))
                        val response = reader.readText()
                        reader.close()

                        val jsonObject = JSONObject(response)
                        val message = jsonObject.getString("message")

                        withContext(Dispatchers.Main) {
                            messageTextView.text = message
                        }
                        success = true
                        connection.disconnect()
                        break
                    } else {
                        lastError = "HTTP $responseCode"
                    }
                    connection.disconnect()
                } catch (e: Exception) {
                    lastError = e.message ?: "알 수 없는 오류"
                }
            }

            if (!success) {
                withContext(Dispatchers.Main) {
                    messageTextView.text = """
                        연결 실패: $lastError
                        
                        확인사항:
                        1. 백엔드 서버가 $port 포트에서 실행 중인가요?
                        2. 실제 기기 사용시 컴퓨터와 같은 WiFi에 연결되어 있나요?
                        
                        실제 기기 사용시 strings.xml에서 
                        server_ip를 컴퓨터 IP로 변경하세요.
                    """.trimIndent()
                }
            }
        }
    }

    private fun testDatabaseConnection() {
        CoroutineScope(Dispatchers.IO).launch {
            val port = getString(R.string.server_port)
            val serverIp = getString(R.string.server_ip)

            val baseUrl = if (serverIp == "auto") {
                "http://10.0.2.2:$port"
            } else {
                "http://$serverIp:$port"
            }

            try {
                withContext(Dispatchers.Main) {
                    messageTextView.text = "DB 연결 테스트 중..."
                }

                val url = URL("$baseUrl/api/users/test")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connectTimeout = 5000
                connection.readTimeout = 5000

                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val reader = BufferedReader(InputStreamReader(connection.inputStream))
                    val response = reader.readText()
                    reader.close()

                    withContext(Dispatchers.Main) {
                        messageTextView.text = "✅ $response"
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        messageTextView.text = "❌ DB 테스트 실패: HTTP $responseCode"
                    }
                }
                connection.disconnect()
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    messageTextView.text = "❌ DB 테스트 오류: ${e.message}"
                }
            }
        }
    }



    private fun showTestMessages() {
        CoroutineScope(Dispatchers.IO).launch {
            val port = getString(R.string.server_port)
            val serverIp = getString(R.string.server_ip)

            val baseUrl = if (serverIp == "auto") {
                "http://10.0.2.2:$port"
            } else {
                "http://$serverIp:$port"
            }

            try {
                withContext(Dispatchers.Main) {
                    messageTextView.text = "Test 테이블 메시지 가져오는 중..."
                }

                val url = URL("$baseUrl/api/test/messages")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connectTimeout = 5000
                connection.readTimeout = 5000

                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val reader = BufferedReader(InputStreamReader(connection.inputStream))
                    val response = reader.readText()
                    reader.close()

                    // JSON 배열 파싱
                    val jsonArray = org.json.JSONArray(response)
                    val messages = mutableListOf<String>()

                    for (i in 0 until jsonArray.length()) {
                        val jsonObject = jsonArray.getJSONObject(i)
                        val id = jsonObject.getLong("id")
                        val message = jsonObject.getString("message")
                        messages.add("ID: $id - $message")
                    }

                    withContext(Dispatchers.Main) {
                        if (messages.isNotEmpty()) {
                            messageTextView.text = "📋 Test 테이블 메시지들:\n\n" + messages.joinToString("\n\n")
                        } else {
                            messageTextView.text = "Test 테이블에 메시지가 없습니다."
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        messageTextView.text = "❌ 메시지 조회 실패: HTTP $responseCode"
                    }
                }
                connection.disconnect()
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    messageTextView.text = "❌ 메시지 조회 오류: ${e.message}"
                }
            }
        }
    }


}