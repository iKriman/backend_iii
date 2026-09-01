package cl.duoc.backendiii.semana2.config;

import cl.duoc.backendiii.semana2.model.Transaccion;
import cl.duoc.backendiii.semana2.model.TransaccionReporte;
import cl.duoc.backendiii.semana2.partition.BancoCsvPartitioner;
import cl.duoc.backendiii.semana2.policy.BancoSkipPolicy;
import cl.duoc.backendiii.semana2.processor.TransaccionProcessor;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.partition.support.TaskExecutorPartitionHandler;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.task.TaskExecutor;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.time.LocalDate;

@Configuration
public class TransaccionesJobConfig {

    @Bean
    @StepScope
    public FlatFileItemReader<Transaccion> transaccionReader(
            @Value("#{stepExecutionContext['linesToSkip']}") Integer linesToSkip,
            @Value("#{stepExecutionContext['maxItemCount']}") Integer maxItemCount) {

        return new FlatFileItemReaderBuilder<Transaccion>()
                .name("transaccionReader")
                .resource(new ClassPathResource("input/transacciones.csv"))
                .linesToSkip(linesToSkip != null ? linesToSkip : 1)
                .maxItemCount(maxItemCount != null ? maxItemCount : Integer.MAX_VALUE)
                .delimited()
                .names("id", "fecha", "monto", "tipo")
                .fieldSetMapper(fieldSet -> {
                    Transaccion t = new Transaccion();
                    t.setId(fieldSet.readString("id"));

                    String fechaStr = fieldSet.readString("fecha");
                    if (fechaStr != null && !fechaStr.isBlank()) {
                        try {
                            t.setFecha(LocalDate.parse(fechaStr.replace("/", "-").trim()));
                        } catch (Exception e) {
                            t.setFecha(null);
                        }
                    }

                    String montoStr = fieldSet.readString("monto");
                    if (montoStr != null && !montoStr.isBlank()) {
                        try {
                            t.setMonto(new BigDecimal(montoStr.trim()));
                        } catch (Exception e) {
                            t.setMonto(null);
                        }
                    }

                    String tipoStr = fieldSet.readString("tipo");
                    t.setTipo(tipoStr != null ? tipoStr.trim() : null);

                    return t;
                })
                .build();
    }

    @Bean
    public JdbcBatchItemWriter<TransaccionReporte> transaccionWriter(DataSource dataSource) {
        return new JdbcBatchItemWriterBuilder<TransaccionReporte>()
                .sql("MERGE INTO daily_transaction_report (id, cuenta, monto, tipo, estado, fecha) " +
                     "KEY(id) VALUES (:id, :cuenta, :monto, :tipo, :estado, :fecha)")
                .dataSource(dataSource)
                .beanMapped()
                .build();
    }

    @Bean
    public TaskExecutor transaccionTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(3);
        executor.setMaxPoolSize(3);
        executor.setThreadNamePrefix("Worker-");
        executor.initialize();
        return executor;
    }

    @Bean
    public TaskExecutorPartitionHandler transaccionPartitionHandler(Step minionStep) {
        TaskExecutorPartitionHandler handler = new TaskExecutorPartitionHandler();
        handler.setGridSize(3);
        handler.setTaskExecutor(transaccionTaskExecutor());
        handler.setStep(minionStep);
        return handler;
    }

    @Bean
    public Step minionStep(
            JobRepository jobRepository, 
            PlatformTransactionManager transactionManager, 
            DataSource dataSource,
            FlatFileItemReader<Transaccion> transaccionReader) {
        return new StepBuilder("minionStep", jobRepository)
                .<Transaccion, TransaccionReporte>chunk(10, transactionManager)
                .reader(transaccionReader)
                .processor(new TransaccionProcessor())
                .writer(transaccionWriter(dataSource))
                .faultTolerant()
                .skipPolicy(new BancoSkipPolicy())
                .retry(TransientDataAccessException.class)
                .retryLimit(3)
                .build();
    }

    @Bean
    public Step masterStep(JobRepository jobRepository, TaskExecutorPartitionHandler transaccionPartitionHandler) {
        return new StepBuilder("masterStep", jobRepository)
                .partitioner("minionStep", new BancoCsvPartitioner())
                .partitionHandler(transaccionPartitionHandler)
                .build();
    }

    @Bean
    public JobExecutionListener jobListener(JdbcTemplate jdbcTemplate) {
        return new JobExecutionListener() {
            @Override
            public void afterJob(JobExecution jobExecution) {
                Integer total = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM daily_transaction_report", Integer.class);
                System.out.println("==========================================");
                System.out.println("REGISTROS TOTALES PROCESADOS EN BD: " + total);
                System.out.println("==========================================");
            }
        };
    }

    @Bean
    public Job reporteTransaccionesJob(JobRepository jobRepository, Step masterStep, JobExecutionListener jobListener) {
        return new JobBuilder("reporteTransaccionesJob", jobRepository)
                .start(masterStep)
                .listener(jobListener)
                .build();
    }
}