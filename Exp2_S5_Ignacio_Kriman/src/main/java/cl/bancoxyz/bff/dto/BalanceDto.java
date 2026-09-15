package cl.bancoxyz.bff.dto;

import java.math.BigDecimal;

public record BalanceDto(
        Integer accountId,
        BigDecimal availableBalance
) {
}
