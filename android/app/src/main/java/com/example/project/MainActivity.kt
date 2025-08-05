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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        messageTextView = findViewById(R.id.messageTextView)
        fetchButton = findViewById(R.id.fetchButton)

        fetchButton.setOnClickListener {
            fetchMessageFromServer()
        }
    }

    private fun fetchMessageFromServer() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // 에뮬레이터용: 10.0.2.2는 호스트 머신의 localhost를 의미
                // 실제 기기용: 컴퓨터의 실제 IP 주소로 변경 필요 (예: 192.168.1.100)
                val url = URL("http://10.0.2.2:8081/api/message")
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
                } else {
                    withContext(Dispatchers.Main) {
                        messageTextView.text = "서버 연결 실패: $responseCode"
                    }
                }
                connection.disconnect()
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    messageTextView.text = "오류 발생: ${e.message}"
                }
            }
        }
    }
}