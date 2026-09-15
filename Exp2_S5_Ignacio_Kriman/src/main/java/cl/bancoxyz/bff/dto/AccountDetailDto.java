package cl.bancoxyz.bff.dto;

import java.math.BigDecimal;
import java.util.List;

public record AccountDetailDto(
        Integer accountId,
        String customerName,
        String productType,
        BigDecimal balance,
        BigDecimal monthlyInterest,
        List<MovementDto> recentMovements
) {
}
