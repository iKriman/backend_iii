package cl.bancoxyz.bff.dto;

import java.math.BigDecimal;

public record AccountSummaryDto(
        Integer accountId,
        String productType,
        BigDecimal balance
) {
}
