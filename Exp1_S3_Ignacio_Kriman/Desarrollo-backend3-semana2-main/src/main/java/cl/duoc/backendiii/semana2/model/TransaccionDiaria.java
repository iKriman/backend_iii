package cl.duoc.backendiii.semana2.model;

public record TransaccionDiaria(
        String id,
        String fecha,
        String monto,
        String tipo
) {
}