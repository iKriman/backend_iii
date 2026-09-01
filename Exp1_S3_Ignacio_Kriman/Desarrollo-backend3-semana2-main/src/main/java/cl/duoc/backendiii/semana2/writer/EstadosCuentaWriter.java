package cl.duoc.backendiii.semana2.writer;

import cl.duoc.backendiii.semana2.model.EstadoCuentaProcesado;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.core.io.FileSystemResource;

import java.io.File;

public class EstadosCuentaWriter {


public static FlatFileItemWriter<EstadoCuentaProcesado> build() {

    File outputDir = new File("output");

    if (!outputDir.exists()) {
        outputDir.mkdirs();
    }

    File outputFile = new File(outputDir, "estados_cuenta_procesados.csv");

    return new FlatFileItemWriterBuilder<EstadoCuentaProcesado>()
            .name("estadosCuentaWriter")
            .resource(new FileSystemResource(outputFile))
            .shouldDeleteIfExists(true)
            .headerCallback(writer -> writer.write("cuenta_id;fecha;transaccion;monto;descripcion"))
            .lineAggregator(item -> String.join(";",
                    String.valueOf(item.cuentaId()),
                    String.valueOf(item.fecha()),
                    item.transaccion(),
                    String.valueOf(item.monto()),
                    item.descripcion()
            ))
            .build();
}


}
