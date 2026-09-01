package cl.duoc.backendiii.semana2.model;

import java.math.BigDecimal;

public record CuentaInteresProcesada(
        Long cuentaId,
        String nombre,
        BigDecimal saldo,
        Integer edad,
        String tipo,
        BigDecimal interesCalculado 
) {
}