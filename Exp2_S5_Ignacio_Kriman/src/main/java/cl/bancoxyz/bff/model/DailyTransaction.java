package cl.bancoxyz.bff.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DailyTransaction(
        Integer id,
        LocalDate date,
        BigDecimal amount,
        String type
) {
}
