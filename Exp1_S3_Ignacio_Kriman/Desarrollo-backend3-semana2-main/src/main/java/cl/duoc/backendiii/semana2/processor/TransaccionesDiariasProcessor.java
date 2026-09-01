package cl.duoc.backendiii.semana2.processor;

import cl.duoc.backendiii.semana2.exception.DatoBancarioInvalidoException;
import cl.duoc.backendiii.semana2.model.TransaccionDiaria;
import cl.duoc.backendiii.semana2.model.TransaccionDiariaProcesada;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class TransaccionesDiariasProcessor implements ItemProcessor<TransaccionDiaria, TransaccionDiariaProcesada> {

    private static final Logger log = LoggerFactory.getLogger(TransaccionesDiariasProcessor.class);

    private static final DateTimeFormatter FORMATO_FECHA =
            DateTimeFormatter.ofPattern("dd-MM-yyyy");

    @Override
    public TransaccionDiariaProcesada process(TransaccionDiaria item) {

        String idStr = limpiar(item.id());
        String tipo = limpiar(item.tipo());

        if (idStr.isBlank() || tipo.isBlank()) {
            throw new DatoBancarioInvalidoException(
                    "ID o Tipo de transacción están vacíos."
            );
        }

        try {
            Long id = Long.parseLong(idStr);
            LocalDate fecha = LocalDate.parse(limpiar(item.fecha()), FORMATO_FECHA);
            BigDecimal monto = new BigDecimal(limpiar(item.monto()));

            log.info(
                    "Procesando transacción diaria ID: {} en hilo: {}",
                    id,
                    Thread.currentThread().getName()
            );

            return new TransaccionDiariaProcesada(
                    id,
                    fecha,
                    monto,
                    tipo
            );

        } catch (Exception e) {
            throw new DatoBancarioInvalidoException(
                    "Error de formato en transacción diaria ID: " + idStr
            );
        }
    }

    private String limpiar(String value) {
        return value == null ? "" : value.trim();
    }
}