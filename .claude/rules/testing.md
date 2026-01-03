# 테스트 규칙

## 테스트 컨벤션

- 한글 메서드명 (백틱 사용): `` `ID로 사용자 조회 테스트` ``
- Given-When-Then 구조
- `@SpringBootTest`, `@Transactional`, `@Rollback` 사용
- **Controller 테스트는 작성하지 않음** (Service 레벨에서 충분히 커버)

## 테스트 예시

```kotlin
@SpringBootTest
@Transactional
@Rollback
class UserServiceTest {

    @Autowired
    private lateinit var userService: UserService

    @Test
    fun `ID로 사용자 조회 테스트`() {
        // Given
        val user = createTestUser()

        // When
        val result = userService.get(GetUserParam(id = user.id.value))

        // Then
        assertThat(result.loginId).isEqualTo(user.loginId)
    }
}
```
