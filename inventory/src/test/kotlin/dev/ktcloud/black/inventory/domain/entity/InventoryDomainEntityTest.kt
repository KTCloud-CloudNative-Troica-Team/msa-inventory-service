package dev.ktcloud.black.inventory.domain.entity

import dev.ktcloud.black.inventory.domain.exception.InventoryException
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

/**
 * R-57 (평가 기본 (3)-1): InventoryDomainEntity 단위 테스트.
 *
 * 본 entity 는 재고 도메인의 핵심 비즈니스 로직 (증가 / 차감 / set) 을 캡슐화함.
 * 차감 시 _quantity 부족 검증은 평가 (2)-3 장애 격리 시나리오 의 도메인 base — 충분히
 * 단위 테스트로 cover.
 */
@DisplayName("InventoryDomainEntity - 재고 도메인 entity")
class InventoryDomainEntityTest {

    @Test
    @DisplayName("기본 quantity 가 0 으로 초기화")
    fun `default quantity 는 0`() {
        val inv = InventoryDomainEntity(productId = "P-001", skuCode = "SKU-A")
        assertThat(inv.quantity).isZero()
    }

    @Test
    @DisplayName("increaseQuantity 호출 시 quantity 가 누적됨")
    fun `재고 증가`() {
        val inv = InventoryDomainEntity(productId = "P-001", skuCode = "SKU-A", _quantity = 10)

        inv.increaseQuantity(5)
        assertThat(inv.quantity).isEqualTo(15)

        inv.increaseQuantity(20)
        assertThat(inv.quantity).isEqualTo(35)
    }

    @Test
    @DisplayName("decreaseQuantity 호출 시 quantity 가 감소함")
    fun `재고 차감 — 정상 경로`() {
        val inv = InventoryDomainEntity(productId = "P-001", skuCode = "SKU-A", _quantity = 100)

        inv.decreaseQuantity(30)
        assertThat(inv.quantity).isEqualTo(70)
    }

    @Test
    @DisplayName("decreaseQuantity 요청량이 보유량보다 크면 InventoryNotEnough 예외")
    fun `재고 부족 시 예외`() {
        val inv = InventoryDomainEntity(productId = "P-001", skuCode = "SKU-A", _quantity = 5)

        assertThatThrownBy { inv.decreaseQuantity(10) }
            .isInstanceOf(InventoryException.InventoryNotEnough::class.java)

        // 예외 발생 시 quantity 가 변동되지 않아야 함 (원자성 보장)
        assertThat(inv.quantity).isEqualTo(5)
    }

    @Test
    @DisplayName("정확히 보유량만큼 차감 — 0 이 됨 (boundary)")
    fun `재고 차감 — boundary (전부 차감)`() {
        val inv = InventoryDomainEntity(productId = "P-001", skuCode = "SKU-A", _quantity = 10)

        inv.decreaseQuantity(10)
        assertThat(inv.quantity).isZero()
    }

    @Test
    @DisplayName("setQuantity 로 임의 값 강제 갱신 (배치 projection 용도)")
    fun `setQuantity 강제 갱신`() {
        val inv = InventoryDomainEntity(productId = "P-001", skuCode = "SKU-A", _quantity = 50)

        inv.setQuantity(123)
        assertThat(inv.quantity).isEqualTo(123)

        inv.setQuantity(0)
        assertThat(inv.quantity).isZero()
    }
}
