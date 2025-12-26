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

## 코드 컨벤션

### Exposed ORM 패턴

**Table 정의** (`tables/`)
```kotlin
object Users : LongIdTable("users") {
    val loginId = varchar("login_id", 255).uniqueIndex()
    val deletedAt = timestamp("deleted_at").nullable()
}
```

**Entity 정의** (`domain/`)
```kotlin
class UserEntity(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<UserEntity>(Users)
    var loginId by Users.loginId
    fun isDeleted(): Boolean = deletedAt != null
}
```

**Repository 패턴** (`repository/`)
```kotlin
@Repository
class UserRepository {
    fun fetchOne(param: UserFetchOneParam): UserEntity? = transaction {
        // 조건부 쿼리
    }
    fun save(entity: UserEntity) = transaction { entity.flush() }
}
```

**파라미터 DTO** (`repository/dto/`)
```kotlin
data class UserFetchOneParam(
    val id: Long? = null,
    val deleted: Boolean? = null,  // Soft Delete 지원
)
```

### 네이밍

- 클래스: `PascalCase` (UserEntity)
- 변수/함수: `camelCase` (loginId, fetchOne)
- DB 컬럼: `snake_case` (login_id)
- Enum: `UPPER_SNAKE_CASE` (PLANNING)

### 테스트

- 한글 메서드명 (백틱 사용): `` `ID로 사용자 조회 테스트` ``
- Given-When-Then 구조
- `@SpringBootTest`, `@Transactional`, `@Rollback` 사용
- **Controller 테스트는 작성하지 않음** (Service 레벨에서 충분히 커버)

## Soft Delete

모든 주요 엔티티에 `deleted_at` 필드로 Soft Delete 구현:
- `deleted = false`: 활성 데이터만 조회
- `deleted = true`: 삭제된 데이터만 조회
- `deleted = null`: 전체 조회

## 데이터베이스

PostgreSQL 로컬 접속:
```bash
psql -h localhost -p 5432 -d home-order
```

마이그레이션 파일: `src/main/resources/db/migration/V{n}__description.sql`

## 작업 완료 체크리스트

1. `./gradlew compileKotlin` - 컴파일 확인
2. `./gradlew test` - 테스트 실행
3. `./gradlew build` - 빌드 확인

## 코드 작업 원칙

### 레이어별 DTO 규칙

각 레이어마다 별도의 DTO를 만들고 파라미터로는 DTO만 받는다.

| 레이어 | 요청 DTO | 응답 DTO | 위치 |
|--------|----------|----------|------|
| Controller | `엔티티명 + 목적 + Request` | `엔티티명 + Response` | `controller/dto/` |
| Service | `엔티티명 + 목적 + Param` | `엔티티명 + 목적 + Result` | `service/dto/` |
| Repository | `엔티티명 + FetchOneParam`, `엔티티명 + FetchManyParam` | Entity | `repository/dto/` |

**DTO 네이밍 예시**
```kotlin
// Controller DTO
PartyCreateRequest    // 파티 생성 요청
PartyUpdateRequest    // 파티 수정 요청
PartyResponse         // 파티 응답

// Service DTO
PartyCreateParam      // 파티 생성 파라미터
PartyUpdateParam      // 파티 수정 파라미터
PartyListParam        // 파티 목록 조회 파라미터
PartyGetParam         // 파티 상세 조회 파라미터
PartyDeleteParam      // 파티 삭제 파라미터

// Repository DTO
PartyFetchOneParam    // 파티 단건 조회 파라미터
PartyFetchParam       // 파티 목록 조회 파라미터
```

- 별도의 DTO가 필요하지 않은 경우 Service에서 Entity 또는 Entity List를 반환한다.
- 반환값이 필요 없는 경우:
  - Service: 반환값 없음 (Unit)
  - Controller: `ResponseEntity<Void>` 반환, `ResponseEntity.noContent().build()` 사용

### 코드 스타일

**파라미터 수직 정렬**
```kotlin
// 파라미터가 여러 개일 때 줄바꿈하여 수직 정렬
val result = authService.signup(
    SignupParam(
        loginId = request.loginId,
        password = request.password,
        nickname = request.nickname,
    )
)
```

**Trailing Comma 사용**
```kotlin
data class SignupParam(
    val loginId: String,
    val password: String,
    val nickname: String,  // trailing comma
)
```

### 예외 처리

**ErrorCode Enum 사용**
```kotlin
enum class ErrorCode(
    val code: Int,
    val message: String,
) {
    BAD_REQUEST(code = 400, message = "잘못된 요청입니다"),
    UNAUTHORIZED(code = 401, message = "인증이 필요합니다"),
    FORBIDDEN(code = 403, message = "권한이 없습니다"),
    NOT_FOUND(code = 404, message = "리소스를 찾을 수 없습니다"),
    CONFLICT(code = 409, message = "이미 존재하는 리소스입니다"),
    INTERNAL_SERVER_ERROR(code = 500, message = "서버 오류가 발생했습니다"),
}
```

**HomeOrderException 사용**
- 추가 코드가 필요한 경우가 아니라면, 새로운 ErrorCode를 별도로 만들지 않고, 기존 ErrorCode + 커스텀 메시지 조합으로 사용
- HTTP 상태 코드에 맞는 ErrorCode 선택 후 구체적인 메시지는 직접 전달

```kotlin
// 기본 메시지 사용
throw HomeOrderException(ErrorCode.NOT_FOUND)

// 커스텀 메시지 사용 (권장)
throw HomeOrderException(ErrorCode.NOT_FOUND, "사용자를 찾을 수 없습니다")
throw HomeOrderException(ErrorCode.BAD_REQUEST, "파티 날짜는 현재 시간 이후여야 합니다")
throw HomeOrderException(ErrorCode.CONFLICT, "같은 날짜에 동일한 이름의 파티가 이미 존재합니다")
```

### 인증

- JWT 기반 인증 (AuthInterceptor)
- `/api/auth/**` 경로는 인증 제외
- 현재 사용자 ID 조회: `AuthContext.getCurrentUserId()`

### REST API 규칙

- **수정(Update)은 항상 PATCH 사용** (PUT 사용하지 않음)
- 상태 변경도 별도 API 없이 PATCH로 처리
- PatchRequest의 필드는 nullable로 선언하여 값이 있을 때만 업데이트

```kotlin
// PatchRequest 예시
data class UpdatePartyRequest(
    val name: String? = null,        // optional
    val status: PartyStatus? = null, // optional (상태 변경도 여기서)
)

// Service에서 null 체크 후 업데이트
fun update(param: UpdatePartyParam): PartyEntity = transaction {
    val party = findById(param.id)
    param.name?.let { party.name = it }
    param.status?.let { party.status = it }
    party
}
```

### Controller 작성 패턴

```kotlin
@Tag(name = "Auth", description = "인증 API")
@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val authService: AuthService,
) {

    @Operation(summary = "회원가입")
    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    fun signup(@Valid @RequestBody request: SignupRequest): SignupResponse {
        val user = authService.signup(
            SignupParam(
                loginId = request.loginId,
                password = request.password,
                nickname = request.nickname,
            )
        )
        return SignupResponse(userId = user.id.value)
    }
}
```

### Service 작성 패턴

**메서드 네이밍**
- 목록 조회: `list(param)`
- 단건 조회: `get(param)`
- 생성: `create(param)`
- 수정: `update(param)`
- 삭제: `delete(param)`

**파라미터 규칙: 파라미터가 2개 이상이면 반드시 xxxParam DTO 생성**
```kotlin
// Bad - 파라미터 2개 이상을 직접 받음
fun get(id: Long, hostId: Long): PartyEntity

// Good - Param DTO로 감싸기
fun get(param: GetPartyParam): PartyEntity

data class GetPartyParam(
    val id: Long,
    val hostId: Long,
)
```

```kotlin
@Service
class PartyService(
    private val partyRepository: PartyRepository,
) {

    fun list(param: ListPartyParam): List<PartyEntity> { ... }
    fun get(param: GetPartyParam): PartyEntity { ... }
    fun create(param: CreatePartyParam): PartyEntity { ... }
    fun update(param: UpdatePartyParam): PartyEntity { ... }
    fun delete(param: DeletePartyParam) { ... }
}
```

### 환경 변수

| 변수명 | 필수 | 기본값 | 설명 |
|--------|------|--------|------|
| `JWT_SECRET` | O | - | JWT 서명용 비밀키 (최소 256bits) |
| `JWT_EXPIRATION` | X | 86400000 | JWT 만료시간 (ms, 기본 24시간) |