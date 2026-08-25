package cl.duoc.backendiii.semana2.policy;

import cl.duoc.backendiii.semana2.exception.DatoBancarioInvalidoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.step.skip.SkipLimitExceededException;
import org.springframework.batch.core.step.skip.SkipPolicy;
import org.springframework.batch.item.file.FlatFileParseException;

public class BancoSkipPolicy implements SkipPolicy {

    private static final Logger log = LoggerFactory.getLogger(BancoSkipPolicy.class);
    private static final int LIMITE_OMISIONES = 10; // Límite máximo de errores permitidos por archivo

    @Override
    public boolean shouldSkip(Throwable throwable, long skipCount) throws SkipLimitExceededException {
        boolean errorControlado = throwable instanceof DatoBancarioInvalidoException
                || throwable instanceof FlatFileParseException;

        if (errorControlado && skipCount < LIMITE_OMISIONES) {
            log.warn("Se omitirá registro inválido. Omisiones actuales: {}. Motivo: {}", skipCount, throwable.getMessage());
            return true;
        }

        return false;
    }
}