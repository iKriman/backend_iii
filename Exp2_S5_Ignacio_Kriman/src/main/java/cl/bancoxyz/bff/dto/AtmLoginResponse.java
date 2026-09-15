package cl.bancoxyz.bff.dto;

import java.time.LocalDateTime;

public record AtmLoginResponse(
        String channel,
        String token,
        LocalDateTime expiresAt
) {
}
