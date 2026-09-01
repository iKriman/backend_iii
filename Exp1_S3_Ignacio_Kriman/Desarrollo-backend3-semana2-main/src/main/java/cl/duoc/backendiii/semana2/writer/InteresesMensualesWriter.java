package cl.duoc.backendiii.semana2.writer;

import cl.duoc.backendiii.semana2.model.CuentaInteresProcesada;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.core.io.FileSystemResource;

import java.io.File;

public class InteresesMensualesWriter {


public static FlatFileItemWriter<CuentaInteresProcesada> build() {

    File outputDir = new File("output");

    if (!outputDir.exists()) {
        outputDir.mkdirs();
    }

    File outputFile = new File(outputDir, "intereses_procesados.csv");

    return new FlatFileItemWriterBuilder<CuentaInteresProcesada>()
            .name("interesesMensualesWriter")
            .resource(new FileSystemResource(outputFile))
            .shouldDeleteIfExists(true)
            .headerCallback(writer -> writer.write("cuenta_id;nombre;saldo;edad;tipo;interes_calculado"))
            .lineAggregator(item -> String.join(";",
                    String.valueOf(item.cuentaId()),
                    item.nombre(),
                    String.valueOf(item.saldo()),
                    String.valueOf(item.edad()),
                    item.tipo(),
                    String.valueOf(item.interesCalculado())
            ))
            .build();
}


}
