# Home Order

홈파티/모임에서 음식 주문을 관리하는 시스템

## 기술 스택

- **Kotlin 1.9.25** + **Spring Boot 3.4.5**
- **Gradle** (Kotlin DSL)
- **Java 17**
- **PostgreSQL** + **Exposed ORM 0.61.0**
- **Flyway** (DB 마이그레이션)
- **JUnit 5** + **AssertJ** (테스트)

## 주요 명령어

```bash
# 빌드
./gradlew build

# 테스트
./gradlew test

# 실행
./gradlew bootRun

# 클린 빌드
./gradlew clean build
```

## 프로젝트 구조

```
src/main/kotlin/com/wonjiyap/homeorder/
├── config/          # 설정 (Swagger, WebMvc, PasswordEncoder 등)
├── controller/      # 컨트롤러
│   └── dto/         # Request/Response DTO
├── domain/          # Entity (Exposed DAO)
├── enums/           # Enum (PartyStatus, OrderStatus)
├── exception/       # 예외 처리 (HomeOrderException, ErrorCode, GlobalExceptionHandler)
├── interceptor/     # 인터셉터 (AuthInterceptor)
├── repository/      # Repository 클래스
│   └── dto/         # 조회 파라미터 DTO (xxxFetchOneParam, xxxFetchManyParam)
├── service/         # 서비스 레이어
│   └── dto/         # Param/Result DTO
├── tables/          # Exposed Table 정의
└── util/            # 유틸리티 (JwtUtil, AuthContext)

src/main/resources/
├── db/migration/    # Flyway 마이그레이션
└── application.yml  # 설정

src/test/kotlin/     # 테스트 코드
```

## 핵심 도메인

| 도메인 | 설명 |
|--------|------|
| User | 호스트 사용자 |
| Party | 파티/모임 |
| InviteCode | 파티 초대 코드 |
| PartyGuest | 파티 게스트 |
| Category | 메뉴 카테고리 |
| Menu | 메뉴 항목 |
| OptionGroup | 메뉴 옵션 그룹 |
| Option | 개별 옵션 |
| Order | 주문 |
| OrderItem | 주문 항목 |
| OrderItemOption | 주문 항목 옵션 |

## 환경 변수

| 변수명 | 필수 | 기본값 | 설명 |
|--------|------|--------|------|
| `JWT_SECRET` | O | - | JWT 서명용 비밀키 (최소 256bits) |
| `JWT_EXPIRATION` | X | 86400000 | JWT 만료시간 (ms, 기본 24시간) |

## 작업 완료 체크리스트

1. `./gradlew compileKotlin` - 컴파일 확인
2. `./gradlew test` - 테스트 실행
3. `./gradlew build` - 빌드 확인
