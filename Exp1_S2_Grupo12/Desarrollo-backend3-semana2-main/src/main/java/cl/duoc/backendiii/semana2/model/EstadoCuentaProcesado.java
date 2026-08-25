package cl.duoc.backendiii.semana2.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public record EstadoCuentaProcesado(
        Long cuentaId,
        LocalDate fecha,
        String transaccion,
        BigDecimal monto,
        String descripcion
) {
}