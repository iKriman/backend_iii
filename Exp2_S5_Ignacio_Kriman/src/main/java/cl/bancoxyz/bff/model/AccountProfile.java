package cl.bancoxyz.bff.model;

import java.math.BigDecimal;

public record AccountProfile(
        Integer accountId,
        String customerName,
        BigDecimal baseBalance,
        Integer age,
        String productType
) {
}
