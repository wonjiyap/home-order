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
├── domain/          # Entity (Exposed DAO)
├── tables/          # Exposed Table 정의
├── repository/      # Repository 클래스
│   └── dto/         # 조회 파라미터 DTO
├── service/         # 서비스 레이어
├── controller/      # 컨트롤러
└── enums/           # Enum (PartyStatus, OrderStatus)

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
