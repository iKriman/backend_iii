package cl.duoc.backendiii.semana2.model;

public record EstadoCuentaEntrada(
        String cuentaId,
        String fecha,
        String transaccion,
        String monto,
        String descripcion
) {
}