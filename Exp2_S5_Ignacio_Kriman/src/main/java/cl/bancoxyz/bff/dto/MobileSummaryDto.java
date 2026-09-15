package cl.bancoxyz.bff.dto;

import java.math.BigDecimal;
import java.util.List;

public record MobileSummaryDto(
        String channel,
        String customerId,
        BigDecimal totalBalance,
        int accountsCount,
        List<AccountSummaryDto> accounts
) {
}
