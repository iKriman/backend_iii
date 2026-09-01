package cl.duoc.backendiii.semana2.config;

import cl.duoc.backendiii.semana2.listener.BancoErrorSkipListener;
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
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.transaction.PlatformTransactionManager;

import java.io.IOException;

@Configuration
public class BancoJobConfig {

    @Bean
    public TransaccionesDiariasReader transaccionesReader() throws IOException {
        return new TransaccionesDiariasReader("input/transacciones_diarias.csv");
    }

    @Bean
    public InteresesMensualesReader interesesReader() throws IOException {
        return new InteresesMensualesReader("input/intereses_mensuales.csv");
    }

    @Bean
    public EstadosCuentaReader estadosCuentaReader() throws IOException {
        return new EstadosCuentaReader("input/estados_cuenta.csv");
    }

    @Bean
    public TransaccionesDiariasProcessor transaccionesProcessor() {
        return new TransaccionesDiariasProcessor();
    }

    @Bean
    public InteresesProcessor interesesProcessor() {
        return new InteresesProcessor();
    }

    @Bean
    public EstadosCuentaProcessor estadosCuentaProcessor() {
        return new EstadosCuentaProcessor();
    }

    @Bean
    @StepScope
    public FlatFileItemWriter<TransaccionDiariaProcesada> transaccionesWriter() {
        return TransaccionesDiariasWriter.build();
    }

    @Bean
    @StepScope
    public FlatFileItemWriter<CuentaInteresProcesada> interesesWriter() {
        return InteresesMensualesWriter.build();
    }

    @Bean
    @StepScope
    public FlatFileItemWriter<EstadoCuentaProcesado> estadosCuentaWriter() {
        return EstadosCuentaWriter.build();
    }

    @Bean
    public BancoSkipPolicy bancoSkipPolicy() {
        return new BancoSkipPolicy();
    }

    @Bean
    public BancoStepListener bancoStepListener() {
        return new BancoStepListener();
    }

    @Bean
    public Step transaccionesStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            BancoErrorSkipListener errorListener,
            TransaccionesDiariasReader transaccionesReader,
            TransaccionesDiariasProcessor transaccionesProcessor,
            FlatFileItemWriter<TransaccionDiariaProcesada> transaccionesWriter,
            BancoSkipPolicy bancoSkipPolicy,
            BancoStepListener bancoStepListener) {

        return new StepBuilder("transaccionesStep", jobRepository)
                .<TransaccionDiaria, TransaccionDiariaProcesada>chunk(5, transactionManager)
                .reader(transaccionesReader)
                .processor(transaccionesProcessor)
                .writer(transaccionesWriter)
                .faultTolerant()
                .skipPolicy(bancoSkipPolicy)
                .listener(errorListener)
                .listener(bancoStepListener)
                .build();
    }

    @Bean
    public Step interesesStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            BancoErrorSkipListener errorListener,
            InteresesMensualesReader interesesReader,
            InteresesProcessor interesesProcessor,
            FlatFileItemWriter<CuentaInteresProcesada> interesesWriter,
            BancoSkipPolicy bancoSkipPolicy,
            BancoStepListener bancoStepListener) {

        return new StepBuilder("interesesStep", jobRepository)
                .<CuentaInteres, CuentaInteresProcesada>chunk(5, transactionManager)
                .reader(interesesReader)
                .processor(interesesProcessor)
                .writer(interesesWriter)
                .faultTolerant()
                .skipPolicy(bancoSkipPolicy)
                .listener(errorListener)
                .listener(bancoStepListener)
                .build();
    }

    @Bean
    public Step estadosCuentaStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            BancoErrorSkipListener errorListener,
            EstadosCuentaReader estadosCuentaReader,
            EstadosCuentaProcessor estadosCuentaProcessor,
            FlatFileItemWriter<EstadoCuentaProcesado> estadosCuentaWriter,
            BancoSkipPolicy bancoSkipPolicy,
            BancoStepListener bancoStepListener) {

        return new StepBuilder("estadosCuentaStep", jobRepository)
                .<EstadoCuentaEntrada, EstadoCuentaProcesado>chunk(5, transactionManager)
                .reader(estadosCuentaReader)
                .processor(estadosCuentaProcessor)
                .writer(estadosCuentaWriter)
                .faultTolerant()
                .skipPolicy(bancoSkipPolicy)
                .listener(errorListener)
                .listener(bancoStepListener)
                .build();
    }

    @Bean
    public Job bancoJob(
            JobRepository jobRepository,
            Step transaccionesStep,
            Step interesesStep,
            Step estadosCuentaStep) {

        return new JobBuilder("bancoJob", jobRepository)
                .start(transaccionesStep)
                .next(interesesStep)
                .next(estadosCuentaStep)
                .build();
    }
}