package cl.duoc.backendiii.semana2.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;

public class BancoJobListener implements JobExecutionListener {

    private static final Logger log = LoggerFactory.getLogger(BancoJobListener.class);

    @Override
    public void beforeJob(JobExecution jobExecution) {
        log.info("=== INICIANDO JOB BANCO XYZ ===");
        log.info("Procesando Transacciones, Intereses y Estados de Cuenta...");
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        log.info("=== JOB BANCO XYZ TERMINADO ===");
        log.info("Estado final: {}", jobExecution.getStatus());
        log.info("Revisar carpeta /output para ver los archivos procesados y los errores.");
    }
}