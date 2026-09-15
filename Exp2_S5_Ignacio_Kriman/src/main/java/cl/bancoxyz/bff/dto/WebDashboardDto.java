package cl.bancoxyz.bff.dto;

import java.math.BigDecimal;
import java.util.List;

public record WebDashboardDto(
        String channel,
        String customerId,
        BigDecimal totalBalance,
        int accountsCount,
        List<AccountDetailDto> accounts,
        List<DataQualityDto> dataQuality
) {
}
