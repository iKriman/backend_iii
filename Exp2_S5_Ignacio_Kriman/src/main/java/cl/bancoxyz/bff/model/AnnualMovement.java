package cl.bancoxyz.bff.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public record AnnualMovement(
        Integer accountId,
        LocalDate date,
        String transactionType,
        BigDecimal amount,
        String description
) {
}
