package com.styloflow.sales.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.styloflow.cash.domain.model.Cash;
import com.styloflow.shared.domain.exception.BusinessRuleException;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@DisplayName("Sale domain rules")
class SaleTest {

    private static final Instant NOW = Instant.parse("2026-10-02T15:00:00Z");
    private static final BigDecimal TAX_13 = new BigDecimal("13");

    private static Cash openRegister() {
        return Cash.open(null, NOW, new BigDecimal("100.00"), null);
    }

    private static SaleItem service(String price, String discount) {
        return SaleItem.create(ItemType.SERVICE, 1L, "Cut", 1, new BigDecimal(price),
                discount == null ? null : new BigDecimal(discount), null);
    }

    private static Sale register(List<SaleItem> items, String discount, PaymentMethod method, String received) {
        return Sale.register(NOW, openRegister(), null, null, items, discount == null ? null : new BigDecimal(discount),
                method, received == null ? null : new BigDecimal(received), TAX_13, "  ");
    }

    @Nested
    @DisplayName("SaleItem.create")
    class CreateItem {

        @Test
        void create_should_computeNetSubtotal_when_lineHasDiscount() {
            SaleItem item = SaleItem.create(ItemType.PRODUCT, 7L, "Shampoo", 3, new BigDecimal("20"),
                    new BigDecimal("5"), null);

            assertAll(
                    () -> assertThat(item.getUnitPrice()).isEqualTo("20.00"),
                    () -> assertThat(item.getDiscount()).isEqualTo("5.00"),
                    () -> assertThat(item.getSubtotal()).isEqualTo("55.00"));
        }

        @Test
        void create_should_allowCourtesy_when_discountEqualsGross() {
            assertThat(service("50", "50").getSubtotal()).isEqualTo("0.00");
        }

        @Test
        void create_should_throw_when_discountExceedsGross() {
            assertThatThrownBy(() -> service("50", "50.01"))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("Cut");
        }
    }

    @Nested
    @DisplayName("Sale.register")
    class Register {

        @Test
        void register_should_computeTotalsTaxAndChange_when_paidInCash() {
            Sale sale = register(List.of(service("100", "10"), service("50", null)), "40", PaymentMethod.CASH, "200");

            assertAll(
                    () -> assertThat(sale.getSubtotal()).isEqualTo("140.00"),
                    () -> assertThat(sale.getDiscount()).isEqualTo("40.00"),
                    () -> assertThat(sale.getTotal()).isEqualTo("100.00"),
                    () -> assertThat(sale.getTax()).isEqualTo("11.50"),
                    () -> assertThat(sale.getAmountReceived()).isEqualTo("200.00"),
                    () -> assertThat(sale.getChange()).isEqualTo("100.00"),
                    () -> assertThat(sale.getStatus()).isEqualTo(SaleStatus.COMPLETED),
                    () -> assertThat(sale.getNotes()).isNull());
        }

        @Test
        void register_should_receiveExactTotal_when_cashAmountMissing() {
            Sale sale = register(List.of(service("80", null)), null, PaymentMethod.CASH, null);

            assertThat(sale.getAmountReceived()).isEqualTo("80.00");
            assertThat(sale.getChange()).isEqualTo("0.00");
        }

        @Test
        void register_should_ignoreAmountReceived_when_notCash() {
            Sale sale = register(List.of(service("80", null)), null, PaymentMethod.QR, "500");

            assertThat(sale.getAmountReceived()).isEqualTo("80.00");
            assertThat(sale.getChange()).isEqualTo("0.00");
        }

        @Test
        void register_should_throw_when_globalDiscountExceedsSubtotal() {
            assertThatThrownBy(() -> register(List.of(service("80", null)), "80.01", PaymentMethod.CARD, null))
                    .isInstanceOf(BusinessRuleException.class);
        }

        @Test
        void register_should_throw_when_cashReceivedIsLessThanTotal() {
            assertThatThrownBy(() -> register(List.of(service("80", null)), null, PaymentMethod.CASH, "79.99"))
                    .isInstanceOf(BusinessRuleException.class);
        }
    }

    @Nested
    @DisplayName("Sale.voidSale")
    class VoidSale {

        @Test
        void voidSale_should_markVoided_when_registerIsOpen() {
            Sale sale = register(List.of(service("80", null)), null, PaymentMethod.CARD, null);

            sale.voidSale(null, "  wrong item ", NOW);

            assertThat(sale.getStatus()).isEqualTo(SaleStatus.VOIDED);
            assertThat(sale.getVoidReason()).isEqualTo("wrong item");
        }

        @Test
        void voidSale_should_throw_when_alreadyVoided() {
            Sale sale = register(List.of(service("80", null)), null, PaymentMethod.CARD, null);
            sale.voidSale(null, "x", NOW);

            assertThatThrownBy(() -> sale.voidSale(null, "x", NOW)).isInstanceOf(BusinessRuleException.class);
        }

        @Test
        void voidSale_should_throw_when_registerIsClosed() {
            Sale sale = register(List.of(service("80", null)), null, PaymentMethod.CARD, null);
            sale.getCashRegister().close(null, NOW, new BigDecimal("100.00"), null, List.of());

            assertThatThrownBy(() -> sale.voidSale(null, "x", NOW)).isInstanceOf(BusinessRuleException.class);
        }
    }

    @ParameterizedTest(name = "total {0} at {1}% contains {2} of tax")
    @CsvSource({ "113.00, 13, 13.00", "100.00, 13, 11.50", "100.00, 0, 0.00", "0.00, 13, 0.00" })
    void includedTax_should_extractTaxFromGrossTotal(BigDecimal total, BigDecimal rate, BigDecimal expected) {
        assertThat(Sale.includedTax(total, rate)).isEqualTo(expected);
    }
}
