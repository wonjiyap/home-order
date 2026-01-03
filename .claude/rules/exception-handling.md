# 예외 처리

## ErrorCode Enum 사용

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

## HomeOrderException 사용

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
