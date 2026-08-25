package cl.duoc.backendiii.semana2.writer;

import cl.duoc.backendiii.semana2.model.EstadoCuentaProcesado;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class EstadosCuentaWriter implements ItemWriter<EstadoCuentaProcesado> {

    private static final Path OUTPUT_FILE = Path.of("output/estados_cuenta_procesados.csv");

    public EstadosCuentaWriter() throws IOException {
        Files.createDirectories(OUTPUT_FILE.getParent());
        Files.writeString(OUTPUT_FILE, "cuenta_id;fecha;transaccion;monto;descripcion\n", StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    @Override
    public synchronized void write(Chunk<? extends EstadoCuentaProcesado> chunk) throws Exception {
        StringBuilder sb = new StringBuilder();
        for (EstadoCuentaProcesado item : chunk) {
            sb.append(item.cuentaId()).append(";")
              .append(item.fecha()).append(";")
              .append(item.transaccion()).append(";")
              .append(item.monto()).append(";")
              .append(item.descripcion()).append("\n");
        }
        Files.writeString(OUTPUT_FILE, sb.toString(), StandardOpenOption.APPEND);
    }
}