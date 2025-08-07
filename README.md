# 팀 프로젝트 - 안드로이드 + 스프링 부트

## 🚀 빠른 시작

### 1. 백엔드 실행
```bash
cd backend
./mvnw.cmd spring-boot:run    # Windows
./mvnw spring-boot:run        # Mac/Linux
```
서버가 `http://localhost:8081`에서 실행됩니다.

### 2. 안드로이드 앱 실행
- Android Studio에서 `android` 폴더 열기
- 앱 빌드 및 실행

## 📱 안드로이드 설정

### 에뮬레이터 사용시
- 별도 설정 불필요 (자동으로 `10.0.2.2:8081` 사용)

### 실제 기기 사용시
1. 컴퓨터와 같은 WiFi에 연결
2. 컴퓨터 IP 주소 확인:
   - Windows: `ipconfig` 
   - Mac/Linux: `ifconfig`
3. `android/app/src/main/res/values/strings.xml` 수정:
   ```xml
   <string name="server_ip">192.168.1.100</string>  <!-- 실제 IP로 변경 -->
   ```

## 🔧 포트 변경
`backend/src/main/resources/application.properties`에서:
```properties
server.port=8081  # 원하는 포트로 변경
```

안드로이드 `strings.xml`에서도 동일하게 변경:
```xml
<string name="server_port">8081</string>
```

## 📋 API 테스트
브라우저에서 `http://localhost:8081/api/message` 접속하여 JSON 응답 확인

## �  실시간 채팅 설정 (다중 사용자)

### 중요: 서버는 한 곳에서만 실행!
- **한 명만** 백엔드 서버를 실행합니다
- **모든 사용자**가 같은 서버에 연결해야 실시간 채팅이 가능합니다

### 설정 방법

#### 1. 서버 실행자 (한 명만)
```bash
cd backend
./mvnw.cmd spring-boot:run
```

#### 2. 서버 실행자의 IP 주소 확인
```bash
# Windows
ipconfig

# Mac/Linux  
ifconfig
```
예: `192.168.219.180`

#### 3. 모든 사용자의 안드로이드 앱 설정
`android/app/src/main/res/values/strings.xml` 수정:
```xml
<string name="server_ip">192.168.219.180</string>  <!-- 서버 PC의 실제 IP -->
```

#### 4. 네트워크 요구사항
- 모든 사용자가 **같은 WiFi 네트워크**에 연결
- 서버 PC의 방화벽에서 8081 포트 허용

### 테스트 방법
1. 서버 실행 후 브라우저에서 `http://[서버IP]:8081/api/chat/history` 접속
2. 빈 배열 `[]` 응답이 오면 정상
3. 각 기기에서 앱 실행하여 채팅 테스트

## 🛠️ 문제 해결

### "연결 실패" 오류시
1. 백엔드 서버가 실행 중인지 확인
2. 서버 PC의 방화벽 설정 확인 (8081 포트 허용)
3. 모든 기기가 같은 WiFi에 연결되어 있는지 확인
4. 안드로이드 앱의 server_ip가 올바른지 확인

### 실시간 채팅이 안 될 때
1. 각자 다른 서버를 실행하고 있지 않은지 확인
2. WebSocket 연결 로그 확인
3. 서버 콘솔에서 "WebSocket 연결 성공" 메시지 확인

### 빌드 오류시
```bash
# 안드로이드
cd android
./gradlew clean build

# 백엔드  
cd backend
./mvnw.cmd clean compile
```