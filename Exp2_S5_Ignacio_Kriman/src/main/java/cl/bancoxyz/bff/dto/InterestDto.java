package cl.bancoxyz.bff.dto;

import java.math.BigDecimal;

public record InterestDto(
        Integer accountId,
        String productType,
        BigDecimal balance,
        BigDecimal annualRate,
        BigDecimal monthlyInterest
) {
}
