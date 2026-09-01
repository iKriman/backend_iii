package cl.duoc.backendiii.semana2.processor;

import cl.duoc.backendiii.semana2.exception.DatoBancarioInvalidoException;
import cl.duoc.backendiii.semana2.model.CuentaInteres;
import cl.duoc.backendiii.semana2.model.CuentaInteresProcesada;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;

import java.math.BigDecimal;

public class InteresesProcessor implements ItemProcessor<CuentaInteres, CuentaInteresProcesada> {

    private static final Logger log = LoggerFactory.getLogger(InteresesProcessor.class);

    @Override
    public CuentaInteresProcesada process(CuentaInteres item) {
        String idStr = limpiar(item.cuentaId());
        
        if (idStr.isBlank() || limpiar(item.nombre()).isBlank()) {
            throw new DatoBancarioInvalidoException("ID o Nombre están vacíos en intereses.");
        }

        try {
            Long id = Long.parseLong(idStr);
            String nombre = limpiar(item.nombre());
            BigDecimal saldo = new BigDecimal(limpiar(item.saldo()));
            Integer edad = Integer.parseInt(limpiar(item.edad()));
            String tipo = limpiar(item.tipo());

            // Regla de Negocio: Calcular interés
            BigDecimal tasa = tipo.equalsIgnoreCase("ahorro") ? new BigDecimal("0.05") : new BigDecimal("0.02");
            BigDecimal interesCalculado = saldo.multiply(tasa);

            log.info("Procesando intereses cuenta ID: {} en hilo: {}", id, Thread.currentThread().getName());

            return new CuentaInteresProcesada(id, nombre, saldo, edad, tipo, interesCalculado);
        } catch (Exception e) {
            throw new DatoBancarioInvalidoException("Error de formato en intereses de cuenta ID: " + idStr);
        }
    }

    private String limpiar(String value) {
        return value == null ? "" : value.trim();
    }
}