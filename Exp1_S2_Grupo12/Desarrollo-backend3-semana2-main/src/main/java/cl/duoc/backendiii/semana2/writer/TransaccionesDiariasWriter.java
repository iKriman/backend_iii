package cl.duoc.backendiii.semana2.writer;

import cl.duoc.backendiii.semana2.model.TransaccionDiariaProcesada;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class TransaccionesDiariasWriter implements ItemWriter<TransaccionDiariaProcesada> {

    private static final Path OUTPUT_FILE = Path.of("output/transacciones_procesadas.csv");

    public TransaccionesDiariasWriter() throws IOException {
        Files.createDirectories(OUTPUT_FILE.getParent());
        Files.writeString(OUTPUT_FILE, "id;fecha;monto;tipo\n", StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    @Override
    public synchronized void write(Chunk<? extends TransaccionDiariaProcesada> chunk) throws Exception {
        StringBuilder sb = new StringBuilder();
        for (TransaccionDiariaProcesada item : chunk) {
            sb.append(item.id()).append(";")
              .append(item.fecha()).append(";")
              .append(item.monto()).append(";")
              .append(item.tipo()).append("\n");
        }
        Files.writeString(OUTPUT_FILE, sb.toString(), StandardOpenOption.APPEND);
    }
}