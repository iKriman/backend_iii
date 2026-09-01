package cl.duoc.backendiii.semana2.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TransaccionDiariaProcesada(
    Long id,
    LocalDate fecha,
    BigDecimal monto,
    String tipo
) {}