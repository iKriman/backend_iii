package cl.duoc.backendiii.semana2.writer;

import cl.duoc.backendiii.semana2.model.CuentaInteresProcesada;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class InteresesMensualesWriter implements ItemWriter<CuentaInteresProcesada> {

    private static final Path OUTPUT_FILE = Path.of("output/intereses_procesados.csv");

    public InteresesMensualesWriter() throws IOException {
        Files.createDirectories(OUTPUT_FILE.getParent());
        Files.writeString(OUTPUT_FILE, "cuenta_id;nombre;saldo;edad;tipo;interes_calculado\n", StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    @Override
    public synchronized void write(Chunk<? extends CuentaInteresProcesada> chunk) throws Exception {
        StringBuilder sb = new StringBuilder();
        for (CuentaInteresProcesada item : chunk) {
            sb.append(item.cuentaId()).append(";")
              .append(item.nombre()).append(";")
              .append(item.saldo()).append(";")
              .append(item.edad()).append(";")
              .append(item.tipo()).append(";")
              .append(item.interesCalculado()).append("\n");
        }
        Files.writeString(OUTPUT_FILE, sb.toString(), StandardOpenOption.APPEND);
    }
}