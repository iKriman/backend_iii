package cl.duoc.backendiii.semana2.decider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.job.flow.FlowExecutionStatus;
import org.springframework.batch.core.job.flow.JobExecutionDecider;

public class BancoJobDecider implements JobExecutionDecider {

    private static final Logger log = LoggerFactory.getLogger(BancoJobDecider.class);

    @Override
    public FlowExecutionStatus decide(JobExecution jobExecution, StepExecution stepExecution) {
        if (stepExecution != null && stepExecution.getSkipCount() > 0) {
            log.warn("Decider: El Step '{}' terminó con advertencias (registros omitidos).", stepExecution.getStepName());
        } else {
            log.info("Decider: El Step '{}' terminó limpio, sin omisiones.", stepExecution != null ? stepExecution.getStepName() : "Desconocido");
        }
        return FlowExecutionStatus.COMPLETED;
    }
}