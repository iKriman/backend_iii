package cl.duoc.backendiii.semana2.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;

public class BancoStepListener implements StepExecutionListener {

    private static final Logger log = LoggerFactory.getLogger(BancoStepListener.class);

    @Override
    public void beforeStep(StepExecution stepExecution) {
        log.info("-> Iniciando Step: {}", stepExecution.getStepName());
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        log.info("<- Step {} finalizado. Leídos: {}, Escritos: {}, Omitidos por error: {}",
                stepExecution.getStepName(),
                stepExecution.getReadCount(),
                stepExecution.getWriteCount(),
                stepExecution.getSkipCount());

        if (stepExecution.getSkipCount() > 0) {
            log.warn("   Hubo registros omitidos en {}. Revisar errores_banco.csv", stepExecution.getStepName());
        }

        return ExitStatus.COMPLETED;
    }
}