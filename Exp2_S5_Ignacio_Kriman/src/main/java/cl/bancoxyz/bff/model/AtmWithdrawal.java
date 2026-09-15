package cl.bancoxyz.bff.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AtmWithdrawal(
        String operationId,
        Integer accountId,
        BigDecimal amount,
        LocalDateTime createdAt
) {
}
