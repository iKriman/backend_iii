package cl.duoc.backendiii.semana2.listener;

import cl.duoc.backendiii.semana2.model.TransaccionDiaria;
import cl.duoc.backendiii.semana2.model.CuentaInteres;
import cl.duoc.backendiii.semana2.model.EstadoCuentaEntrada;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.SkipListener;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

@Component
@StepScope
public class BancoErrorSkipListener implements SkipListener<Object, Object> {

    private static final Logger log = LoggerFactory.getLogger(BancoErrorSkipListener.class);
    private static final Path ERROR_FILE = Path.of("output/errores_banco.csv");

    public BancoErrorSkipListener() throws IOException {
        Files.createDirectories(ERROR_FILE.getParent());
        Files.writeString(ERROR_FILE, "etapa;id_registro;motivo\n", 
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    @Override
    public void onSkipInRead(Throwable throwable) {
        escribirError("READ", "DESCONOCIDO", throwable.getMessage());
    }

    @Override
    public void onSkipInProcess(Object item, Throwable throwable) {
        String id = "SIN_ID";
        // Identificamos de qué archivo viene el error
        if (item instanceof TransaccionDiaria td) id = td.id();
        else if (item instanceof CuentaInteres ci) id = ci.cuentaId();
        else if (item instanceof EstadoCuentaEntrada ec) id = ec.cuentaId();

        escribirError("PROCESS", id, throwable.getMessage());
    }

    @Override
    public void onSkipInWrite(Object item, Throwable throwable) {
        escribirError("WRITE", "VARIOS", throwable.getMessage());
    }

    private synchronized void escribirError(String etapa, String id, String motivo) {
        try {
            String linea = etapa + ";" + (id != null ? id.trim() : "") + ";" + (motivo != null ? motivo.replace("\n", " ") : "") + "\n";
            Files.writeString(ERROR_FILE, linea, StandardOpenOption.APPEND);
        } catch (IOException ex) {
            log.error("No se pudo escribir archivo de errores", ex);
        }
    }
}