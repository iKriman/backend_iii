package cl.bancoxyz.bff.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AtmWithdrawalResponse(
        String operationId,
        Integer accountId,
        BigDecimal withdrawnAmount,
        BigDecimal availableBalance,
        LocalDateTime createdAt
) {
}
