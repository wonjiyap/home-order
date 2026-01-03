# Exposed ORM 패턴

## Table 정의 (`tables/`)

```kotlin
object Users : LongIdTable("users") {
    val loginId = varchar("login_id", 255).uniqueIndex()
    val deletedAt = timestamp("deleted_at").nullable()
}
```

## Entity 정의 (`domain/`)

```kotlin
class UserEntity(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<UserEntity>(Users)
    var loginId by Users.loginId
    fun isDeleted(): Boolean = deletedAt != null
}
```

## Repository 패턴 (`repository/`)

```kotlin
@Repository
class UserRepository {
    fun fetchOne(param: UserFetchOneParam): UserEntity? = transaction {
        // 조건부 쿼리
    }
    fun save(entity: UserEntity) = transaction { entity.flush() }
}
```

## 파라미터 DTO (`repository/dto/`)

```kotlin
data class UserFetchOneParam(
    val id: Long? = null,
    val deleted: Boolean? = null,  // Soft Delete 지원
)
```

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
