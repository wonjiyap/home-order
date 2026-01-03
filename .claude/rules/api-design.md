# API 설계 규칙

## 레이어별 DTO 규칙

각 레이어마다 별도의 DTO를 만들고 파라미터로는 DTO만 받는다.

| 레이어 | 요청 DTO | 응답 DTO | 위치 |
|--------|----------|----------|------|
| Controller | `엔티티명 + 목적 + Request` | `엔티티명 + Response` | `controller/dto/` |
| Service | `엔티티명 + 목적 + Param` | `엔티티명 + 목적 + Result` | `service/dto/` |
| Repository | `엔티티명 + FetchOneParam`, `엔티티명 + FetchManyParam` | Entity | `repository/dto/` |

### DTO 네이밍 예시

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

## REST API 규칙

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

## 인증

- JWT 기반 인증 (AuthInterceptor)
- `/api/auth/**` 경로는 인증 제외
- 현재 사용자 ID 조회: `AuthContext.getCurrentUserId()`

## Controller 작성 패턴

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

## Service 작성 패턴

### 메서드 네이밍

- 목록 조회: `list(param)`
- 단건 조회: `get(param)`
- 생성: `create(param)`
- 수정: `update(param)`
- 삭제: `delete(param)`

### 파라미터 규칙

파라미터가 2개 이상이면 반드시 xxxParam DTO 생성

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
