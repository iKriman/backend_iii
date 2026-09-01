package cl.duoc.backendiii.semana2.writer;

import cl.duoc.backendiii.semana2.model.TransaccionDiariaProcesada;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.core.io.FileSystemResource;

import java.io.File;

public class TransaccionesDiariasWriter {
    public static FlatFileItemWriter<TransaccionDiariaProcesada> build() {
        File outputFile = new File("output/transacciones_procesadas.csv");
        File parentDir = outputFile.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }

        return new FlatFileItemWriterBuilder<TransaccionDiariaProcesada>()
                .name("transaccionesDiariasWriter")
                .resource(new FileSystemResource(outputFile))
                .delimited()
                .delimiter(",")
                .names("id", "fecha", "monto", "estado")
                .shouldDeleteIfExists(true)
                .build();
    }
}