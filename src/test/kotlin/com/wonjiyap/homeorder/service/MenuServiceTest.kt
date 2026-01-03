package com.wonjiyap.homeorder.service

import com.wonjiyap.homeorder.domain.CategoryEntity
import com.wonjiyap.homeorder.domain.PartyEntity
import com.wonjiyap.homeorder.domain.UserEntity
import com.wonjiyap.homeorder.enums.PartyStatus
import com.wonjiyap.homeorder.exception.ErrorCode
import com.wonjiyap.homeorder.exception.HomeOrderException
import com.wonjiyap.homeorder.service.dto.MenuCreateParam
import com.wonjiyap.homeorder.service.dto.MenuDeleteParam
import com.wonjiyap.homeorder.service.dto.MenuGetParam
import com.wonjiyap.homeorder.service.dto.MenuListParam
import com.wonjiyap.homeorder.service.dto.MenuUpdateParam
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.Rollback
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@SpringBootTest
@Transactional
@Rollback
class MenuServiceTest {

    @Autowired
    private lateinit var menuService: MenuService

    private var testUserId: Long = 0
    private var testPartyId: Long = 0
    private var testCategoryId: Long = 0

    @BeforeEach
    fun setUp() {
        testUserId = transaction {
            UserEntity.new {
                loginId = "menutest_${System.nanoTime()}"
                password = "password123"
                nickname = "메뉴테스트유저"
            }.id.value
        }
        testPartyId = transaction {
            PartyEntity.new {
                hostId = testUserId
                name = "테스트 파티"
                status = PartyStatus.PLANNING
                createdAt = Instant.now()
                updatedAt = Instant.now()
            }.id.value
        }
        testCategoryId = transaction {
            CategoryEntity.new {
                partyId = testPartyId
                name = "테스트 카테고리"
                displayOrder = 0
                createdAt = Instant.now()
                updatedAt = Instant.now()
            }.id.value
        }
    }

    @Test
    fun `메뉴 생성 테스트`() {
        // Given
        val param = MenuCreateParam(
            categoryId = testCategoryId,
            hostId = testUserId,
            name = "피자",
            description = "맛있는 피자",
            isRecommended = true,
            isSoldOut = false,
        )

        // When
        val menu = menuService.create(param)

        // Then
        assertThat(menu.id.value).isGreaterThan(0)
        assertThat(menu.categoryId).isEqualTo(testCategoryId)
        assertThat(menu.name).isEqualTo("피자")
        assertThat(menu.description).isEqualTo("맛있는 피자")
        assertThat(menu.isRecommended).isTrue()
        assertThat(menu.isSoldOut).isFalse()
        assertThat(menu.displayOrder).isEqualTo(0)
        assertThat(menu.deletedAt).isNull()
    }

    @Test
    fun `메뉴 생성시 displayOrder 자동 증가 테스트`() {
        // Given
        menuService.create(
            MenuCreateParam(
                categoryId = testCategoryId,
                hostId = testUserId,
                name = "피자",
            )
        )

        // When
        val menu2 = menuService.create(
            MenuCreateParam(
                categoryId = testCategoryId,
                hostId = testUserId,
                name = "파스타",
            )
        )
        val menu3 = menuService.create(
            MenuCreateParam(
                categoryId = testCategoryId,
                hostId = testUserId,
                name = "샐러드",
            )
        )

        // Then
        assertThat(menu2.displayOrder).isEqualTo(1)
        assertThat(menu3.displayOrder).isEqualTo(2)
    }

    @Test
    fun `중복된 메뉴 이름 생성시 예외 발생 테스트`() {
        // Given
        menuService.create(
            MenuCreateParam(
                categoryId = testCategoryId,
                hostId = testUserId,
                name = "피자",
            )
        )

        // When & Then
        assertThatThrownBy {
            menuService.create(
                MenuCreateParam(
                    categoryId = testCategoryId,
                        hostId = testUserId,
                    name = "피자",
                )
            )
        }.isInstanceOf(HomeOrderException::class.java)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.CONFLICT)
            .hasMessageContaining("같은 이름의 메뉴가 이미 존재합니다")
    }

    @Test
    fun `존재하지 않는 카테고리에 메뉴 생성시 예외 발생 테스트`() {
        // When & Then
        assertThatThrownBy {
            menuService.create(
                MenuCreateParam(
                    categoryId = 999999L,
                        hostId = testUserId,
                    name = "피자",
                )
            )
        }.isInstanceOf(HomeOrderException::class.java)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FOUND)
            .hasMessageContaining("카테고리를 찾을 수 없습니다")
    }

    @Test
    fun `다른 사용자 파티에 메뉴 생성시 예외 발생 테스트`() {
        // Given
        val otherUserId = transaction {
            UserEntity.new {
                loginId = "otheruser_${System.nanoTime()}"
                password = "password123"
                nickname = "다른유저"
            }.id.value
        }

        // When & Then
        assertThatThrownBy {
            menuService.create(
                MenuCreateParam(
                    categoryId = testCategoryId,
                        hostId = otherUserId,
                    name = "피자",
                )
            )
        }.isInstanceOf(HomeOrderException::class.java)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FORBIDDEN)
    }

    @Test
    fun `메뉴 목록 조회 테스트`() {
        // Given
        menuService.create(
            MenuCreateParam(
                categoryId = testCategoryId,
                hostId = testUserId,
                name = "피자",
            )
        )
        menuService.create(
            MenuCreateParam(
                categoryId = testCategoryId,
                hostId = testUserId,
                name = "파스타",
            )
        )

        // When
        val menus = menuService.list(
            MenuListParam(
                categoryId = testCategoryId,
                hostId = testUserId,
            )
        )

        // Then
        assertThat(menus).hasSize(2)
    }

    @Test
    fun `삭제된 메뉴는 목록에서 제외됨 테스트`() {
        // Given
        val menu1 = menuService.create(
            MenuCreateParam(
                categoryId = testCategoryId,
                hostId = testUserId,
                name = "피자",
            )
        )
        menuService.create(
            MenuCreateParam(
                categoryId = testCategoryId,
                hostId = testUserId,
                name = "파스타",
            )
        )

        menuService.delete(
            MenuDeleteParam(
                id = menu1.id.value,
                categoryId = testCategoryId,
                hostId = testUserId,
            )
        )

        // When
        val menus = menuService.list(
            MenuListParam(
                categoryId = testCategoryId,
                hostId = testUserId,
            )
        )

        // Then
        assertThat(menus).hasSize(1)
        assertThat(menus[0].name).isEqualTo("파스타")
    }

    @Test
    fun `메뉴 상세 조회 테스트`() {
        // Given
        val created = menuService.create(
            MenuCreateParam(
                categoryId = testCategoryId,
                hostId = testUserId,
                name = "피자",
                description = "맛있는 피자",
            )
        )

        // When
        val menu = menuService.get(
            MenuGetParam(
                id = created.id.value,
                categoryId = testCategoryId,
                hostId = testUserId,
            )
        )

        // Then
        assertThat(menu.id.value).isEqualTo(created.id.value)
        assertThat(menu.name).isEqualTo("피자")
        assertThat(menu.description).isEqualTo("맛있는 피자")
    }

    @Test
    fun `존재하지 않는 메뉴 조회시 예외 발생 테스트`() {
        // When & Then
        assertThatThrownBy {
            menuService.get(
                MenuGetParam(
                    id = 999999L,
                    categoryId = testCategoryId,
                        hostId = testUserId,
                )
            )
        }.isInstanceOf(HomeOrderException::class.java)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FOUND)
            .hasMessageContaining("메뉴를 찾을 수 없습니다")
    }

    @Test
    fun `메뉴 이름 수정 테스트`() {
        // Given
        val created = menuService.create(
            MenuCreateParam(
                categoryId = testCategoryId,
                hostId = testUserId,
                name = "피자",
            )
        )

        // When
        val updated = menuService.update(
            MenuUpdateParam(
                id = created.id.value,
                categoryId = testCategoryId,
                hostId = testUserId,
                name = "페퍼로니 피자",
            )
        )

        // Then
        assertThat(updated.name).isEqualTo("페퍼로니 피자")
    }

    @Test
    fun `메뉴 설명 수정 테스트`() {
        // Given
        val created = menuService.create(
            MenuCreateParam(
                categoryId = testCategoryId,
                hostId = testUserId,
                name = "피자",
                description = "기본 설명",
            )
        )

        // When
        val updated = menuService.update(
            MenuUpdateParam(
                id = created.id.value,
                categoryId = testCategoryId,
                hostId = testUserId,
                description = "수정된 설명",
            )
        )

        // Then
        assertThat(updated.description).isEqualTo("수정된 설명")
    }

    @Test
    fun `메뉴 추천 상태 수정 테스트`() {
        // Given
        val created = menuService.create(
            MenuCreateParam(
                categoryId = testCategoryId,
                hostId = testUserId,
                name = "피자",
                isRecommended = false,
            )
        )

        // When
        val updated = menuService.update(
            MenuUpdateParam(
                id = created.id.value,
                categoryId = testCategoryId,
                hostId = testUserId,
                isRecommended = true,
            )
        )

        // Then
        assertThat(updated.isRecommended).isTrue()
    }

    @Test
    fun `메뉴 품절 상태 수정 테스트`() {
        // Given
        val created = menuService.create(
            MenuCreateParam(
                categoryId = testCategoryId,
                hostId = testUserId,
                name = "피자",
                isSoldOut = false,
            )
        )

        // When
        val updated = menuService.update(
            MenuUpdateParam(
                id = created.id.value,
                categoryId = testCategoryId,
                hostId = testUserId,
                isSoldOut = true,
            )
        )

        // Then
        assertThat(updated.isSoldOut).isTrue()
    }

    @Test
    fun `같은 이름으로 수정시 검증 생략 테스트`() {
        // Given
        val created = menuService.create(
            MenuCreateParam(
                categoryId = testCategoryId,
                hostId = testUserId,
                name = "피자",
            )
        )

        // When - 같은 이름으로 수정해도 에러 없음
        val updated = menuService.update(
            MenuUpdateParam(
                id = created.id.value,
                categoryId = testCategoryId,
                hostId = testUserId,
                name = "피자",
            )
        )

        // Then
        assertThat(updated.name).isEqualTo("피자")
    }

    @Test
    fun `수정시 중복된 이름으로 변경하면 예외 발생 테스트`() {
        // Given
        menuService.create(
            MenuCreateParam(
                categoryId = testCategoryId,
                hostId = testUserId,
                name = "피자",
            )
        )
        val menu2 = menuService.create(
            MenuCreateParam(
                categoryId = testCategoryId,
                hostId = testUserId,
                name = "파스타",
            )
        )

        // When & Then
        assertThatThrownBy {
            menuService.update(
                MenuUpdateParam(
                    id = menu2.id.value,
                    categoryId = testCategoryId,
                        hostId = testUserId,
                    name = "피자",
                )
            )
        }.isInstanceOf(HomeOrderException::class.java)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.CONFLICT)
    }

    @Test
    fun `존재하지 않는 메뉴 수정시 예외 발생 테스트`() {
        // When & Then
        assertThatThrownBy {
            menuService.update(
                MenuUpdateParam(
                    id = 999999L,
                    categoryId = testCategoryId,
                        hostId = testUserId,
                    name = "피자",
                )
            )
        }.isInstanceOf(HomeOrderException::class.java)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FOUND)
    }

    @Test
    fun `메뉴 삭제 테스트 (Soft Delete)`() {
        // Given
        val created = menuService.create(
            MenuCreateParam(
                categoryId = testCategoryId,
                hostId = testUserId,
                name = "피자",
            )
        )

        // When
        menuService.delete(
            MenuDeleteParam(
                id = created.id.value,
                categoryId = testCategoryId,
                hostId = testUserId,
            )
        )

        // Then - 삭제된 메뉴는 조회되지 않음
        assertThatThrownBy {
            menuService.get(
                MenuGetParam(
                    id = created.id.value,
                    categoryId = testCategoryId,
                        hostId = testUserId,
                )
            )
        }.isInstanceOf(HomeOrderException::class.java)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FOUND)
    }

    @Test
    fun `존재하지 않는 메뉴 삭제시 예외 발생 테스트`() {
        // When & Then
        assertThatThrownBy {
            menuService.delete(
                MenuDeleteParam(
                    id = 999999L,
                    categoryId = testCategoryId,
                        hostId = testUserId,
                )
            )
        }.isInstanceOf(HomeOrderException::class.java)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FOUND)
    }

    @Test
    fun `삭제된 메뉴와 같은 이름으로 새 메뉴 생성 가능 테스트`() {
        // Given
        val menu1 = menuService.create(
            MenuCreateParam(
                categoryId = testCategoryId,
                hostId = testUserId,
                name = "피자",
            )
        )
        menuService.delete(
            MenuDeleteParam(
                id = menu1.id.value,
                categoryId = testCategoryId,
                hostId = testUserId,
            )
        )

        // When
        val menu2 = menuService.create(
            MenuCreateParam(
                categoryId = testCategoryId,
                hostId = testUserId,
                name = "피자",
            )
        )

        // Then
        assertThat(menu2.id.value).isNotEqualTo(menu1.id.value)
        assertThat(menu2.name).isEqualTo("피자")
    }

    @Test
    fun `다른 카테고리에 같은 이름의 메뉴 생성 가능 테스트`() {
        // Given
        menuService.create(
            MenuCreateParam(
                categoryId = testCategoryId,
                hostId = testUserId,
                name = "피자",
            )
        )

        val anotherCategoryId = transaction {
            CategoryEntity.new {
                partyId = testPartyId
                name = "다른 카테고리"
                displayOrder = 1
                createdAt = Instant.now()
                updatedAt = Instant.now()
            }.id.value
        }

        // When
        val menu = menuService.create(
            MenuCreateParam(
                categoryId = anotherCategoryId,
                hostId = testUserId,
                name = "피자",
            )
        )

        // Then
        assertThat(menu.name).isEqualTo("피자")
        assertThat(menu.categoryId).isEqualTo(anotherCategoryId)
    }
}
