package cl.duoc.backendiii.semana2.config;

import cl.duoc.backendiii.semana2.decider.BancoJobDecider;
import cl.duoc.backendiii.semana2.listener.BancoErrorSkipListener;
import cl.duoc.backendiii.semana2.listener.BancoJobListener;
import cl.duoc.backendiii.semana2.listener.BancoStepListener;
import cl.duoc.backendiii.semana2.model.*;
import cl.duoc.backendiii.semana2.policy.BancoSkipPolicy;
import cl.duoc.backendiii.semana2.processor.EstadosCuentaProcessor;
import cl.duoc.backendiii.semana2.processor.InteresesProcessor;
import cl.duoc.backendiii.semana2.processor.TransaccionesDiariasProcessor;
import cl.duoc.backendiii.semana2.reader.EstadosCuentaReader;
import cl.duoc.backendiii.semana2.reader.InteresesMensualesReader;
import cl.duoc.backendiii.semana2.reader.TransaccionesDiariasReader;
import cl.duoc.backendiii.semana2.writer.EstadosCuentaWriter;
import cl.duoc.backendiii.semana2.writer.InteresesMensualesWriter;
import cl.duoc.backendiii.semana2.writer.TransaccionesDiariasWriter;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import java.io.IOException;

@Configuration
public class BancoJobConfig {

    @Bean
    public TaskExecutor bancoTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(3);
        executor.setMaxPoolSize(3);
        executor.setQueueCapacity(25);
        executor.setThreadNamePrefix("BancoXYZ-Thread-");
        executor.setDaemon(true);
        executor.initialize();
        return executor;
    }

    @Bean
    public BancoSkipPolicy bancoSkipPolicy() { return new BancoSkipPolicy(); }

    @Bean
    public BancoJobDecider bancoJobDecider() { return new BancoJobDecider(); }

    @Bean
    public BancoStepListener bancoStepListener() { return new BancoStepListener(); }

    @Bean
    public BancoJobListener bancoJobListener() { return new BancoJobListener(); }

    @Bean
    public TransaccionesDiariasReader transaccionesReader() throws IOException {
        return new TransaccionesDiariasReader("input/transacciones.csv");
    }
    @Bean
    public TransaccionesDiariasProcessor transaccionesProcessor() { return new TransaccionesDiariasProcessor(); }
    @Bean
    public TransaccionesDiariasWriter transaccionesWriter() throws IOException { return new TransaccionesDiariasWriter(); }

    @Bean
    public InteresesMensualesReader interesesReader() throws IOException {
        return new InteresesMensualesReader("input/intereses.csv");
    }
    @Bean
    public InteresesProcessor interesesProcessor() { return new InteresesProcessor(); }
    @Bean
    public InteresesMensualesWriter interesesWriter() throws IOException { return new InteresesMensualesWriter(); }

    @Bean
    public EstadosCuentaReader estadosCuentaReader() throws IOException {
        return new EstadosCuentaReader("input/cuentas_anuales.csv");
    }
    @Bean
    public EstadosCuentaProcessor estadosCuentaProcessor() { return new EstadosCuentaProcessor(); }
    @Bean
    public EstadosCuentaWriter estadosCuentaWriter() throws IOException { return new EstadosCuentaWriter(); }

    @Bean
    public Step transaccionesStep(JobRepository jobRepository, PlatformTransactionManager transactionManager, BancoErrorSkipListener errorListener) throws IOException {
        return new StepBuilder("transaccionesStep", jobRepository)
                .<TransaccionDiaria, TransaccionDiariaProcesada>chunk(5, transactionManager)
                .reader(transaccionesReader())
                .processor(transaccionesProcessor())
                .writer(transaccionesWriter())
                .faultTolerant()
                .skipPolicy(bancoSkipPolicy())
                .listener(errorListener)
                .listener(bancoStepListener())
                .taskExecutor(bancoTaskExecutor())
                .build();
    }

    @Bean
    public Step interesesStep(JobRepository jobRepository, PlatformTransactionManager transactionManager, BancoErrorSkipListener errorListener) throws IOException {
        return new StepBuilder("interesesStep", jobRepository)
                .<CuentaInteres, CuentaInteresProcesada>chunk(5, transactionManager)
                .reader(interesesReader())
                .processor(interesesProcessor())
                .writer(interesesWriter())
                .faultTolerant()
                .skipPolicy(bancoSkipPolicy())
                .listener(errorListener)
                .listener(bancoStepListener())
                .taskExecutor(bancoTaskExecutor())
                .build();
    }

    @Bean
    public Step estadosCuentaStep(JobRepository jobRepository, PlatformTransactionManager transactionManager, BancoErrorSkipListener errorListener) throws IOException {
        return new StepBuilder("estadosCuentaStep", jobRepository)
                .<EstadoCuentaEntrada, EstadoCuentaProcesado>chunk(5, transactionManager)
                .reader(estadosCuentaReader())
                .processor(estadosCuentaProcessor())
                .writer(estadosCuentaWriter())
                .faultTolerant()
                .skipPolicy(bancoSkipPolicy())
                .listener(errorListener)
                .listener(bancoStepListener())
                .taskExecutor(bancoTaskExecutor())
                .build();
    }

@Bean
    public Job bancoXyzJob(JobRepository jobRepository, 
                           Step transaccionesStep, 
                           Step interesesStep, 
                           Step estadosCuentaStep, 
                           BancoJobDecider decider) {
        return new JobBuilder("bancoXyzJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .listener(bancoJobListener())
                .start(transaccionesStep) 
                .next(interesesStep)     
                .next(estadosCuentaStep)  
                .next(decider)           
                .end()
                .build();
    }
}