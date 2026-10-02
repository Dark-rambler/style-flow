package com.styloflow.catalog.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.styloflow.shared.domain.exception.BusinessRuleException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@DisplayName("Product stock")
class ProductTest {

    private static Product withStock(int stock) {
        return Product.builder().name("Gel").stock(stock).minStock(2).build();
    }

    @ParameterizedTest(name = "stock {0} adjusted by {1} = {2}")
    @CsvSource({ "5, 3, 8", "5, -5, 0", "0, 0, 0" })
    void adjustStock_should_addOrRemoveUnits(int stock, int delta, int expected) {
        Product product = withStock(stock);

        product.adjustStock(delta);

        assertThat(product.getStock()).isEqualTo(expected);
    }

    @Test
    void decreaseStock_should_throw_when_insufficient() {
        assertThatThrownBy(() -> withStock(2).decreaseStock(3))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("available: 2");
    }

    @ParameterizedTest(name = "stock {0} with min 2 is low: {1}")
    @CsvSource({ "1, true", "2, true", "3, false" })
    void isLowStock_should_compareWithMinStock(int stock, boolean expected) {
        assertThat(withStock(stock).isLowStock()).isEqualTo(expected);
    }
}
