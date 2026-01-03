# Kotlin 코드 컨벤션

## 네이밍

- 클래스: `PascalCase` (UserEntity)
- 변수/함수: `camelCase` (loginId, fetchOne)
- DB 컬럼: `snake_case` (login_id)
- Enum: `UPPER_SNAKE_CASE` (PLANNING)

## 코드 스타일

### 파라미터 수직 정렬

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

### Trailing Comma 사용

```kotlin
data class SignupParam(
    val loginId: String,
    val password: String,
    val nickname: String,  // trailing comma
)
```
