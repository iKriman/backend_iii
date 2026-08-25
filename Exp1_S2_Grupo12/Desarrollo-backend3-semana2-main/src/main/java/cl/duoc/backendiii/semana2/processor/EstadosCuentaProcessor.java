package cl.duoc.backendiii.semana2.processor;

import cl.duoc.backendiii.semana2.exception.DatoBancarioInvalidoException;
import cl.duoc.backendiii.semana2.model.EstadoCuentaEntrada;
import cl.duoc.backendiii.semana2.model.EstadoCuentaProcesado;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;

import java.math.BigDecimal;
import java.time.LocalDate;

public class EstadosCuentaProcessor implements ItemProcessor<EstadoCuentaEntrada, EstadoCuentaProcesado> {

    private static final Logger log = LoggerFactory.getLogger(EstadosCuentaProcessor.class);

    @Override
    public EstadoCuentaProcesado process(EstadoCuentaEntrada item) {
        String idStr = limpiar(item.cuentaId());

        if (idStr.isBlank()) {
            throw new DatoBancarioInvalidoException("ID de cuenta vacío en estado de cuenta.");
        }

        try {
            Long id = Long.parseLong(idStr);
            LocalDate fecha = LocalDate.parse(limpiar(item.fecha()));
            String transaccion = limpiar(item.transaccion());
            BigDecimal monto = new BigDecimal(limpiar(item.monto()));
            String descripcion = limpiar(item.descripcion());

            log.info("Procesando estado de cuenta ID: {} en hilo: {}", id, Thread.currentThread().getName());

            return new EstadoCuentaProcesado(id, fecha, transaccion, monto, descripcion);
        } catch (Exception e) {
            throw new DatoBancarioInvalidoException("Error de formato en estado de cuenta ID: " + idStr);
        }
    }

    private String limpiar(String value) {
        return value == null ? "" : value.trim();
    }
}