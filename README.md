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

## 🛠️ 문제 해결

### "연결 실패" 오류시
1. 백엔드 서버가 실행 중인지 확인
2. 방화벽 설정 확인
3. 실제 기기 사용시 IP 주소가 올바른지 확인

### 빌드 오류시
```bash
# 안드로이드
cd android
./gradlew clean build

# 백엔드  
cd backend
./mvnw.cmd clean compile
```