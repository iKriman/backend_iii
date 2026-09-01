package cl.duoc.backendiii.semana2.processor;

import cl.duoc.backendiii.semana2.model.Transaccion;
import cl.duoc.backendiii.semana2.model.TransaccionReporte;
import org.springframework.batch.item.ItemProcessor;

import java.math.BigDecimal;

public class TransaccionProcessor implements ItemProcessor<Transaccion, TransaccionReporte> {

    @Override
    public TransaccionReporte process(Transaccion item) throws Exception {

        if (item == null || item.getMonto() == null || item.getMonto().compareTo(BigDecimal.ZERO) <= 0) {
            return null; 
        }

        return new TransaccionReporte(
            item.getId(),
            item.getCuenta(),
            item.getMonto(),
            item.getTipo(),
            item.getEstado(),
            item.getFecha()
        );
    }
}