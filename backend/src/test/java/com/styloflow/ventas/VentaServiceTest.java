package com.styloflow.ventas;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class VentaServiceTest {

    @Test
    void ivaIncluidoSeExtraeDelTotal() {
        assertThat(VentaService.ivaIncluido(new BigDecimal("113.00"), new BigDecimal("13"))).isEqualByComparingTo("13.00");
        assertThat(VentaService.ivaIncluido(new BigDecimal("185.00"), new BigDecimal("13"))).isEqualByComparingTo("21.28");
        assertThat(VentaService.ivaIncluido(new BigDecimal("50.00"), BigDecimal.ZERO)).isEqualByComparingTo("0.00");
    }
}
