# ChatForYou.io - Backend Server

## 1. 프로젝트 개요
- ChatForYou.io는 OpenVidu 기반의 실시간 화상 채팅 서비스입니다.   
- 사용자들이 채팅방을 생성하고 참여하여 화상 통화, 텍스트 채팅, 화면 공유 등의 기능을 제공합니다.  
- Spring Boot 3.3.0과 Java 17을 기반으로 구축된 RESTful API 서버입니다.  

### 주요 특징
- 실시간 화상 통화 및 음성 통화
- 다중 사용자 채팅방 지원
- 통화 녹화 및 재생 기능
- JWT 기반 인증 시스템
- 소셜 로그인 지원 (OAuth2)
- 실시간 알림 시스템 (SSE)
- Redis 기반 세션 관리

## 2. 프로젝트 구조

```
src/
├── main/
│   ├── java/com/chatforyou/io/
│   │   ├── batch/              # 배치 작업 관련
│   │   ├── client/             # OpenVidu 클라이언트 관련
│   │   ├── config/             # 설정 클래스들
│   │   │   ├── AsyncConfig.java
│   │   │   ├── MailConfig.java
│   │   │   ├── RedisConfig.java
│   │   │   ├── SecurityConfig.java
│   │   │   └── WebMvcConfig.java
│   │   ├── controller/         # REST API 컨트롤러
│   │   │   ├── AuthController.java
│   │   │   ├── CallController.java
│   │   │   ├── ChatRoomController.java
│   │   │   ├── OpenViduController.java
│   │   │   ├── RecordingController.java
│   │   │   ├── SessionController.java
│   │   │   ├── SSEController.java
│   │   │   └── UserController.java
│   │   ├── entity/             # JPA 엔티티
│   │   │   ├── Board.java
│   │   │   ├── ChatRoom.java
│   │   │   ├── OpenViduInfo.java
│   │   │   ├── SocialUser.java
│   │   │   └── User.java
│   │   ├── models/             # DTO 및 모델 클래스
│   │   │   ├── in/             # 입력 VO
│   │   │   ├── out/            # 출력 VO
│   │   │   └── sse/            # SSE 관련 모델
│   │   ├── repository/         # 데이터 접근 계층
│   │   ├── services/           # 비즈니스 로직 계층
│   │   │   └── impl/           # 서비스 구현체
│   │   └── utils/              # 유틸리티 클래스
│   └── resources/
│       ├── application.properties
│       └── static/
└── test/                       # 테스트 코드
```

## 3. 주요 기능

### 🔐 사용자 인증 및 관리
- JWT 기반 로그인/로그아웃
- 소셜 로그인 (OAuth2)
- 이메일 인증
- 토큰 갱신 (Refresh Token)
- 사용자 프로필 관리

### 💬 채팅방 관리
- 채팅방 생성/수정/삭제
- 채팅방 목록 조회 및 검색
- 사용자 입장/퇴장 관리

### 📹 화상 통화 기능
- 다중 사용자 화상 통화
- 카메라/마이크 제어
- 실시간 연결 상태 관리


### 🔔 실시간 알림
- SSE(Server-Sent Events) 기반 실시간 알림
- 채팅방 입장/퇴장 알림
- 시스템 알림
- 사용자별 알림 구독 관리

### 📧 메일 서비스
- 이메일 인증 코드 발송
- 비밀번호 재설정 메일
- 시스템 알림 메일

## 4. 주요 기술 스택

### Backend Framework
- **Spring Boot** 3.3.0
- **Java** 17
- **Spring Data JPA**
- **Spring WebSocket**

### Database & Cache
- **MariaDB** - 메인 데이터베이스
- **Redis** - 캐싱 및 세션 관리
- **RedisSearch** - 검색 기능

### 화상 통화
- **OpenVidu** - 화상 통화 플랫폼
- **WebRTC** - 실시간 통신

### 메시징 & 알림
- **SSE (Server-Sent Events)** - 실시간 알림

### 인증 & 보안
- **JWT (JSON Web Token)** - 토큰 기반 인증
- **Spring Security** - 보안 프레임워크

### 기타 라이브러리
- **Lombok** - 코드 간소화
- **Gson** - JSON 처리
- **Apache HttpClient** - HTTP 통신
- **JavaMail** - 이메일 발송

### 빌드 도구
- **Gradle** 8.x
- **Docker** - 컨테이너화

## 5. 환경 설정

### 5.1 필수 요구사항
- **Java 17** 이상
- **Gradle 8.x** 이상
- **MariaDB 10.x** 이상
- **Redis 6.x** 이상
- **OpenVidu Server** 2.x 이상

### 5.2 데이터베이스 설정
```properties
# MariaDB 설정
spring.datasource.driver-class-name=org.mariadb.jdbc.Driver
spring.datasource.url=jdbc:mariadb://localhost:3306/chatforyou_io
spring.datasource.username=your_username
spring.datasource.password=your_password

# JPA 설정
spring.jpa.hibernate.ddl-auto=none
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect
```

### 5.3 Redis 설정
```properties
# Redis 설정
spring.data.redis.host=localhost
spring.data.redis.master.port=6379
spring.data.redis.slave.port=6380
spring.data.redis.password=your_redis_password
spring.cache.type=redis
```

### 5.4 OpenVidu 설정
```properties
# OpenVidu 설정
OPENVIDU_URL=https://your-openvidu-server.com
OPENVIDU_SECRET=your_openvidu_secret
CALL_OPENVIDU_CERTTYPE=selfsigned
CALL_PRIVATE_ACCESS=ENABLED
CALL_RECORDING=ENABLED
CALL_BROADCAST=DISABLED
```

### 5.5 JWT 설정
```properties
# JWT 설정
spring.jwt.issuer=chatforyou-io
spring.jwt.secret-key=your_jwt_secret_key_base64_encoded
spring.jwt.access-expiration=3600000  # 1시간
spring.jwt.refresh-expiration=86400000  # 24시간
```

### 5.6 메일 서버 설정
```properties
# Gmail SMTP 설정
spring.mail.host=smtp.gmail.com
spring.mail.port=465
spring.mail.username=your_email@gmail.com
spring.mail.password=your_app_password
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
```

### 5.7 서버 설정
```properties
# 서버 포트
server.port=8443

# SSL 설정 (운영 환경에서는 true로 설정)
server.ssl.enabled=false

# 세션 설정
server.servlet.session.tracking-modes=cookie

# SSE 설정
sse.keep-alive-timeout=20

# 스레드 풀 설정
spring.thread.bound.multi=6

```

### 5.8 애플리케이션 실행

#### 개발 환경
```bash
# 프로젝트 클론
git clone <repository-url>
cd back-chatforyou-io

# 의존성 설치 및 빌드
./gradlew build

# 애플리케이션 실행
./gradlew bootRun
```

#### 운영 환경 (Docker)
```bash
# Docker 이미지 빌드
docker build -t chatforyou-backend .

# 컨테이너 실행
docker run -p 8443:8443 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e SPRING_DATASOURCE_URL=jdbc:mariadb://your-db-host:3306/chatforyou_io \
  -e SPRING_DATASOURCE_USERNAME=your_username \
  -e SPRING_DATASOURCE_PASSWORD=your_password \
  chatforyou-backend
```

---

## 라이선스
이 프로젝트는 MIT 라이선스 하에 배포됩니다. 자세한 내용은 [LICENSE](LICENSE) 파일을 참조하세요.