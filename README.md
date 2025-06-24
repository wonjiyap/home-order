# Home Order

## 목차
- [개요](#개요)
- [기술스택](#기술스택)
- [ERD](#ERD)

---

## 개요

**Home Order**는 작은 규모의 홈파티나 모임에서 음식 주문을 효율적으로 관리할 수 있는 시스템입니다.

#### 🏠 호스트
- **회원가입**: 계정 생성 및 로그인
- **파티 관리**: 파티 생성, 수정, 삭제(취소)
- **게스트 관리**: 초대 코드 생성 및 파티 참여자 관리
- **메뉴 관리**: 카테고리별 메뉴 및 옵션 설정
- **주문 처리**: 파티 내 주문 현황 확인 및 상태 관리

#### 👥 게스트
- **파티 참여**: 초대 코드를 통한 파티 참여
- **주문 관리**: 개인 주문 생성, 취소

---

## 기술스택

### 백엔드
- **언어**: Kotlin
- **프레임워크**: Spring Boot

### 데이터베이스
- **RDBMS**: PostgreSQL
- **ORM**: Exposed ORM
- **마이그레이션**: Flyway

---

## ERD (Entity Relationship Diagram)

```mermaid
erDiagram
    users {
        BIGINT id PK
        VARCHAR login_id UK
        VARCHAR password
        VARCHAR nickname
        TIMESTAMP created_at
        TIMESTAMP updated_at
        TIMESTAMP deleted_at
    }

    parties {
        BIGINT id PK
        BIGINT host_id
        VARCHAR name
        VARCHAR description
        TIMESTAMP date
        VARCHAR location
        VARCHAR status
        TIMESTAMP created_at
        TIMESTAMP updated_at
        TIMESTAMP deleted_at
    }

    invite_codes {
        BIGINT id PK
        BIGINT party_id
        VARCHAR code UK
        BOOLEAN is_active
        TIMESTAMP created_at
        TIMESTAMP expires_at
        TIMESTAMP deleted_at
    }

    party_guests {
        BIGINT id PK
        BIGINT party_id
        VARCHAR nickname
        BOOLEAN is_blocked
        TIMESTAMP joined_at
        TIMESTAMP deleted_at
    }

    categories {
        BIGINT id PK
        BIGINT party_id
        VARCHAR name
        INTEGER display_order
        TIMESTAMP created_at
        TIMESTAMP updated_at
    }

    menus {
        BIGINT id PK
        BIGINT category_id
        VARCHAR name
        TEXT description
        BOOLEAN is_recommended
        BOOLEAN is_sold_out
        INTEGER display_order
        TIMESTAMP created_at
        TIMESTAMP updated_at
    }

    option_groups {
        BIGINT id PK
        BIGINT menu_id
        VARCHAR name
        BOOLEAN is_required
        TIMESTAMP created_at
        TIMESTAMP updated_at
    }

    options {
        BIGINT id PK
        BIGINT option_group_id
        VARCHAR name
        INTEGER display_order
        TIMESTAMP created_at
        TIMESTAMP updated_at
    }

    orders {
        BIGINT id PK
        BIGINT party_id
        BIGINT guest_id
        VARCHAR status
        TIMESTAMP ordered_at
        TIMESTAMP updated_at
    }

    order_items {
        BIGINT id PK
        BIGINT order_id
        BIGINT menu_id
        INTEGER quantity
        TEXT notes
        TIMESTAMP created_at
    }

    order_item_options {
        BIGINT id PK
        BIGINT order_item_id
        BIGINT option_id
        TIMESTAMP created_at
    }

    users ||--o{ parties : hosts
    parties ||--o{ invite_codes : generates
    parties ||--o{ party_guests : invites
    parties ||--o{ categories : contains
    parties ||--o{ orders : receives
    party_guests ||--o{ orders : places
    categories ||--o{ menus : includes
    menus ||--o{ option_groups : has
    option_groups ||--o{ options : contains
    orders ||--o{ order_items : includes
    order_items ||--o{ order_item_options : selects
    options ||--o{ order_item_options : chosen_in
    menus ||--o{ order_items : ordered_as
```