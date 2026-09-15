package cl.bancoxyz.bff.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record MovementDto(
        LocalDate date,
        String type,
        BigDecimal amount,
        String description
) {
}
