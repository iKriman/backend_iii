package cl.bancoxyz.bff.dto;

import jakarta.validation.constraints.NotBlank;

public record AtmLoginRequest(
        @NotBlank String cardNumber,
        @NotBlank String pin
) {
}
