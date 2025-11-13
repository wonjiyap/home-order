package com.wonjiyap.homeorder.repository

import com.wonjiyap.homeorder.domain.InviteCodeEntity
import com.wonjiyap.homeorder.repository.dto.InviteCodeFetchOneParam
import com.wonjiyap.homeorder.repository.dto.InviteCodeFetchParam
import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.exposed.dao.flushCache
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.Rollback
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@SpringBootTest
@Transactional
@Rollback
class InviteCodeRepositoryTest {

    @Autowired
    private lateinit var inviteCodeRepository: InviteCodeRepository

    @BeforeEach
    fun setUp() {
        inviteCodeRepository = InviteCodeRepository()
    }

    @Test
    fun `초대 코드 생성 및 저장 테스트`() {
        // Given & When
        val inviteCode = transaction {
            InviteCodeEntity.new {
                partyId = 1L
                code = "ABC123"
                isActive = true
                expiresAt = Instant.now().plusSeconds(3600)
            }
        }
        inviteCodeRepository.save(inviteCode)

        // Then
        val param = InviteCodeFetchOneParam(
            id = inviteCode.id.value
        )
        val foundCode = inviteCodeRepository.fetchOne(param)

        assertThat(foundCode).isNotNull()
        assertThat(foundCode?.partyId).isEqualTo(1L)
        assertThat(foundCode?.code).isEqualTo("ABC123")
        assertThat(foundCode?.isActive).isTrue()
    }

    @Test
    fun `파티 ID로 초대 코드 목록 조회 테스트`() {
        // Given
        transaction {
            InviteCodeEntity.new {
                partyId = 1L
                code = "CODE1"
                isActive = true
                expiresAt = Instant.now().plusSeconds(3600)
            }
            InviteCodeEntity.new {
                partyId = 1L
                code = "CODE2"
                isActive = false
                expiresAt = Instant.now().plusSeconds(3600)
            }
            InviteCodeEntity.new {
                partyId = 2L
                code = "CODE3"
                isActive = true
                expiresAt = Instant.now().plusSeconds(3600)
            }
        }

        // When
        val param = InviteCodeFetchParam(
            partyId = 1L
        )
        val codes = inviteCodeRepository.fetch(param)

        // Then
        assertThat(codes).hasSize(2)
        assertThat(codes.all { it.partyId == 1L }).isTrue()
    }

    @Test
    fun `활성 상태로 초대 코드 조회 테스트`() {
        // Given
        transaction {
            InviteCodeEntity.new {
                partyId = 1L
                code = "ACTIVE1"
                isActive = true
                expiresAt = Instant.now().plusSeconds(3600)
            }
            InviteCodeEntity.new {
                partyId = 1L
                code = "INACTIVE1"
                isActive = false
                expiresAt = Instant.now().plusSeconds(3600)
            }
        }

        // When
        val param = InviteCodeFetchParam(
            partyId = 1L,
            isActive = true
        )
        val codes = inviteCodeRepository.fetch(param)

        // Then
        assertThat(codes).hasSize(1)
        assertThat(codes.first().isActive).isTrue()
        assertThat(codes.first().code).isEqualTo("ACTIVE1")
    }

    @Test
    fun `한 파티에 활성 코드 하나와 비활성 코드 하나가 공존 가능한 테스트`() {
        // Given & When
        transaction {
            InviteCodeEntity.new {
                partyId = 1L
                code = "ACTIVE"
                isActive = true
                expiresAt = Instant.now().plusSeconds(3600)
            }
            InviteCodeEntity.new {
                partyId = 1L
                code = "INACTIVE"
                isActive = false
                expiresAt = Instant.now().plusSeconds(3600)
            }
        }

        // Then
        val param = InviteCodeFetchParam(
            partyId = 1L
        )
        val codes = inviteCodeRepository.fetch(param)

        assertThat(codes).hasSize(2)
        assertThat(codes.count { it.isActive }).isEqualTo(1)
        assertThat(codes.count { !it.isActive }).isEqualTo(1)
    }

    @Test
    fun `한 파티에 활성 코드가 두 개 생성 시도하면 예외 발생 테스트`() {
        // Given & When & Then - DB constraint violation 예외 발생
        assertThrows<Exception> {
            transaction {
                // 첫 번째 레코드
                InviteCodeEntity.new {
                    partyId = 1L
                    code = "ACTIVE1"
                    isActive = true
                    expiresAt = Instant.now().plusSeconds(3600)
                }

                // 같은 트랜잭션 내에서 두 번째 레코드
                InviteCodeEntity.new {
                    partyId = 1L
                    code = "ACTIVE2"
                    isActive = true
                    expiresAt = Instant.now().plusSeconds(3600)
                }

                // flush를 호출하여 DB에 즉시 반영
                flushCache()
            }
        }
    }

    @Test
    fun `활성 코드를 비활성화한 후 새로운 활성 코드 생성 가능 테스트`() {
        // Given
        val firstCode = transaction {
            InviteCodeEntity.new {
                partyId = 1L
                code = "FIRST_ACTIVE"
                isActive = true
                expiresAt = Instant.now().plusSeconds(3600)
            }
        }

        // When - 첫 번째 코드를 비활성화
        transaction {
            firstCode.isActive = false
        }
        inviteCodeRepository.save(firstCode)

        // Then - 새로운 활성 코드 생성 가능
        val secondCode = transaction {
            InviteCodeEntity.new {
                partyId = 1L
                code = "SECOND_ACTIVE"
                isActive = true
                expiresAt = Instant.now().plusSeconds(3600)
            }
        }

        val param = InviteCodeFetchParam(
            partyId = 1L,
            isActive = true
        )
        val activeCodes = inviteCodeRepository.fetch(param)

        assertThat(activeCodes).hasSize(1)
        assertThat(activeCodes.first().code).isEqualTo("SECOND_ACTIVE")
    }

    @Test
    fun `활성 코드를 soft delete 한 후 새로운 활성 코드 생성 가능 테스트`() {
        // Given
        val firstCode = transaction {
            InviteCodeEntity.new {
                partyId = 1L
                code = "FIRST_ACTIVE"
                isActive = true
                expiresAt = Instant.now().plusSeconds(3600)
            }
        }

        // When - 첫 번째 코드를 soft delete, isActive도 false로 변경
        transaction {
            firstCode.isActive = false
            firstCode.deletedAt = Instant.now()
        }
        inviteCodeRepository.save(firstCode)

        // Then - 새로운 활성 코드 생성 가능
        val secondCode = transaction {
            InviteCodeEntity.new {
                partyId = 1L
                code = "SECOND_ACTIVE"
                isActive = true
                expiresAt = Instant.now().plusSeconds(3600)
            }
        }

        // 검증
        val param = InviteCodeFetchParam(
            partyId = 1L,
            isActive = true
        )
        val activeCodes = inviteCodeRepository.fetch(param)

        assertThat(activeCodes).hasSize(1)
        assertThat(activeCodes.first().code).isEqualTo("SECOND_ACTIVE")
        assertThat(activeCodes.first().isActive).isTrue()
    }

    @Test
    fun `여러 파티에 각각 활성 코드 하나씩 생성 가능 테스트`() {
        // Given & When
        transaction {
            InviteCodeEntity.new {
                partyId = 1L
                code = "PARTY1_ACTIVE"
                isActive = true
                expiresAt = Instant.now().plusSeconds(3600)
            }
            InviteCodeEntity.new {
                partyId = 2L
                code = "PARTY2_ACTIVE"
                isActive = true
                expiresAt = Instant.now().plusSeconds(3600)
            }
            InviteCodeEntity.new {
                partyId = 3L
                code = "PARTY3_ACTIVE"
                isActive = true
                expiresAt = Instant.now().plusSeconds(3600)
            }
        }

        // Then
        val party1Param = InviteCodeFetchParam(
            partyId = 1L,
            isActive = true
        )
        val party1Codes = inviteCodeRepository.fetch(party1Param)

        val party2Param = InviteCodeFetchParam(
            partyId = 2L,
            isActive = true
        )
        val party2Codes = inviteCodeRepository.fetch(party2Param)

        assertThat(party1Codes).hasSize(1)
        assertThat(party2Codes).hasSize(1)
    }

    @Test
    fun `한 파티에 여러 비활성 코드 생성 가능 테스트`() {
        // Given & When
        transaction {
            InviteCodeEntity.new {
                partyId = 1L
                code = "INACTIVE1"
                isActive = false
                expiresAt = Instant.now().plusSeconds(3600)
            }
            InviteCodeEntity.new {
                partyId = 1L
                code = "INACTIVE2"
                isActive = false
                expiresAt = Instant.now().plusSeconds(3600)
            }
            InviteCodeEntity.new {
                partyId = 1L
                code = "INACTIVE3"
                isActive = false
                expiresAt = Instant.now().plusSeconds(3600)
            }
        }

        // Then
        val param = InviteCodeFetchParam(
            partyId = 1L,
            isActive = false
        )
        val inactiveCodes = inviteCodeRepository.fetch(param)

        assertThat(inactiveCodes).hasSize(3)
    }

    @Test
    fun `코드로 초대 코드 조회 테스트`() {
        // Given
        val inviteCode = transaction {
            InviteCodeEntity.new {
                partyId = 1L
                code = "UNIQUE123"
                isActive = true
                expiresAt = Instant.now().plusSeconds(3600)
            }
        }

        // When
        val param = InviteCodeFetchOneParam(
            code = "UNIQUE123"
        )
        val foundCode = inviteCodeRepository.fetchOne(param)

        // Then
        assertThat(foundCode).isNotNull()
        assertThat(foundCode?.id?.value).isEqualTo(inviteCode.id.value)
        assertThat(foundCode?.code).isEqualTo("UNIQUE123")
    }

    @Test
    fun `만료된 초대 코드 조회 테스트`() {
        // Given
        transaction {
            InviteCodeEntity.new {
                partyId = 1L
                code = "EXPIRED"
                isActive = true
                expiresAt = Instant.now().minusSeconds(3600) // 1시간 전 만료
            }
            InviteCodeEntity.new {
                partyId = 2L
                code = "VALID"
                isActive = true
                expiresAt = Instant.now().plusSeconds(3600) // 1시간 후 만료
            }
        }

        // When
        val expiredParam = InviteCodeFetchParam(
            partyId = 1L,
            isExpired = true
        )
        val expiredCodes = inviteCodeRepository.fetch(expiredParam)

        val validParam = InviteCodeFetchParam(
            partyId = 2L,
            isExpired = false
        )
        val validCodes = inviteCodeRepository.fetch(validParam)

        // Then
        assertThat(expiredCodes).hasSize(1)
        assertThat(expiredCodes.first().code).isEqualTo("EXPIRED")
        assertThat(expiredCodes.first().isExpired()).isTrue()

        assertThat(validCodes).hasSize(1)
        assertThat(validCodes.first().code).isEqualTo("VALID")
        assertThat(validCodes.first().isExpired()).isFalse()
    }

    @Test
    fun `만료 시간이 없는 초대 코드는 만료되지 않은 것으로 조회 테스트`() {
        // Given
        transaction {
            InviteCodeEntity.new {
                partyId = 1L
                code = "NO_EXPIRY"
                isActive = true
                expiresAt = null // 만료 시간 없음
            }
        }

        // When
        val param = InviteCodeFetchParam(
            partyId = 1L,
            isExpired = false
        )
        val codes = inviteCodeRepository.fetch(param)

        // Then
        assertThat(codes).hasSize(1)
        assertThat(codes.first().isExpired()).isFalse()
    }

    @Test
    fun `초대 코드 유효성 검증 테스트`() {
        // Given
        val validCode = transaction {
            InviteCodeEntity.new {
                partyId = 1L
                code = "VALID"
                isActive = true
                expiresAt = Instant.now().plusSeconds(3600)
            }
        }

        val expiredCode = transaction {
            InviteCodeEntity.new {
                partyId = 1L
                code = "EXPIRED"
                isActive = true
                expiresAt = Instant.now().minusSeconds(3600)
            }
        }

        val inactiveCode = transaction {
            InviteCodeEntity.new {
                partyId = 1L
                code = "INACTIVE"
                isActive = false
                expiresAt = Instant.now().plusSeconds(3600)
            }
        }

        // Then
        assertThat(validCode.isValid()).isTrue()
        assertThat(expiredCode.isValid()).isFalse()
        assertThat(inactiveCode.isValid()).isFalse()
    }

    @Test
    fun `초대 코드 soft delete 테스트`() {
        // Given
        val inviteCode = transaction {
            InviteCodeEntity.new {
                partyId = 1L
                code = "DELETE_ME"
                isActive = true
                expiresAt = Instant.now().plusSeconds(3600)
            }
        }
        val codeId = inviteCode.id.value

        // When
        transaction {
            inviteCode.deletedAt = Instant.now()
        }
        inviteCodeRepository.save(inviteCode)

        // Then - withDeleted=false (기본값)
        val param = InviteCodeFetchOneParam(
            id = codeId
        )
        val foundCode = inviteCodeRepository.fetchOne(param)
        assertThat(foundCode).isNull()

        // 실제로는 DB에 존재
        transaction {
            val entity = InviteCodeEntity.findById(codeId)
            assertThat(entity).isNotNull()
            assertThat(entity?.deletedAt).isNotNull()
        }
    }

    @Test
    fun `withDeleted true로 삭제된 코드 포함 조회 테스트`() {
        // Given
        transaction {
            InviteCodeEntity.new {
                partyId = 1L
                code = "ACTIVE"
                isActive = true
                expiresAt = Instant.now().plusSeconds(3600)
            }
            InviteCodeEntity.new {
                partyId = 1L
                code = "DELETED"
                isActive = false
                expiresAt = Instant.now().plusSeconds(3600)
                deletedAt = Instant.now()
            }
        }

        // When - withDeleted=false
        val paramWithoutDeleted = InviteCodeFetchParam(
            partyId = 1L
        )
        val codesWithoutDeleted = inviteCodeRepository.fetch(paramWithoutDeleted)

        // When - withDeleted=true
        val paramWithDeleted = InviteCodeFetchParam(
            partyId = 1L,
            withDeleted = true
        )
        val codesWithDeleted = inviteCodeRepository.fetch(paramWithDeleted)

        // Then
        assertThat(codesWithoutDeleted).hasSize(1)
        assertThat(codesWithDeleted).hasSize(2)
    }

    @Test
    fun `초대 코드 활성화 상태 변경 테스트`() {
        // Given
        val inviteCode = transaction {
            InviteCodeEntity.new {
                partyId = 1L
                code = "TOGGLE"
                isActive = true
                expiresAt = Instant.now().plusSeconds(3600)
            }
        }

        // When
        transaction {
            inviteCode.isActive = false
        }
        inviteCodeRepository.save(inviteCode)

        // Then
        val param = InviteCodeFetchOneParam(
            id = inviteCode.id.value
        )
        val foundCode = inviteCodeRepository.fetchOne(param)

        assertThat(foundCode?.isActive).isFalse()
    }

    @Test
    fun `만료 시간 연장 테스트`() {
        // Given
        val inviteCode = transaction {
            InviteCodeEntity.new {
                partyId = 1L
                code = "EXTEND"
                isActive = true
                expiresAt = Instant.now().plusSeconds(3600)
            }
        }
        val originalExpiry = inviteCode.expiresAt

        // When
        val newExpiry = Instant.now().plusSeconds(7200) // 2시간 후로 연장
        transaction {
            inviteCode.expiresAt = newExpiry
        }
        inviteCodeRepository.save(inviteCode)

        // Then
        val param = InviteCodeFetchOneParam(
            id = inviteCode.id.value
        )
        val foundCode = inviteCodeRepository.fetchOne(param)

        assertThat(foundCode?.expiresAt).isNotEqualTo(originalExpiry)
        assertThat(foundCode?.expiresAt).isEqualTo(newExpiry)
    }

    @Test
    fun `조건 없이 fetch 호출 시 모든 초대 코드 조회 테스트`() {
        // Given
        transaction {
            InviteCodeEntity.new {
                partyId = 1L
                code = "CODE1"
                isActive = true
                expiresAt = Instant.now().plusSeconds(3600)
            }
            InviteCodeEntity.new {
                partyId = 2L
                code = "CODE2"
                isActive = false
                expiresAt = Instant.now().plusSeconds(3600)
            }
        }

        // When
        val param = InviteCodeFetchParam()
        val codes = inviteCodeRepository.fetch(param)

        // Then
        assertThat(codes).hasSize(2)
    }

    @Test
    fun `파티별 활성 초대 코드만 조회 테스트`() {
        // Given
        transaction {
            InviteCodeEntity.new {
                partyId = 1L
                code = "PARTY1_ACTIVE"
                isActive = true
                expiresAt = Instant.now().plusSeconds(3600)
            }
            InviteCodeEntity.new {
                partyId = 1L
                code = "PARTY1_INACTIVE"
                isActive = false
                expiresAt = Instant.now().plusSeconds(3600)
            }
            InviteCodeEntity.new {
                partyId = 2L
                code = "PARTY2_ACTIVE"
                isActive = true
                expiresAt = Instant.now().plusSeconds(3600)
            }
        }

        // When
        val param = InviteCodeFetchParam(
            partyId = 1L,
            isActive = true
        )
        val codes = inviteCodeRepository.fetch(param)

        // Then
        assertThat(codes).hasSize(1)
        assertThat(codes.first().code).isEqualTo("PARTY1_ACTIVE")
    }

    @Test
    fun `삭제된 코드는 isValid false 반환 테스트`() {
        // Given
        val inviteCode = transaction {
            InviteCodeEntity.new {
                partyId = 1L
                code = "DELETED_VALID"
                isActive = true
                expiresAt = Instant.now().plusSeconds(3600)
            }
        }

        // When
        transaction {
            inviteCode.deletedAt = Instant.now()
        }

        // Then
        assertThat(inviteCode.isValid()).isFalse()
    }
}