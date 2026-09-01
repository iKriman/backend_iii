package cl.duoc.backendiii.semana2.reader;

import cl.duoc.backendiii.semana2.model.EstadoCuentaEntrada;
import org.springframework.batch.item.ItemReader;
import org.springframework.core.io.ClassPathResource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class EstadosCuentaReader implements ItemReader<EstadoCuentaEntrada> {

    private final List<EstadoCuentaEntrada> estados;
    private int indiceActual = 0;

    public EstadosCuentaReader(String resourcePath) throws IOException {
        this.estados = cargarDatos(resourcePath);
    }

    @Override
    public synchronized EstadoCuentaEntrada read() {
        if (indiceActual >= estados.size()) {
            return null;
        }
        return estados.get(indiceActual++);
    }

    private List<EstadoCuentaEntrada> cargarDatos(String resourcePath) throws IOException {
        List<EstadoCuentaEntrada> resultado = new ArrayList<>();
        ClassPathResource resource = new ClassPathResource(resourcePath);

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {

            String linea;
            boolean primeraLinea = true;

            while ((linea = reader.readLine()) != null) {
                if (primeraLinea) {
                    primeraLinea = false;
                    continue;
                }

                String[] campos = linea.split("[,;]", -1);

                if (campos.length >= 5) {
                    resultado.add(new EstadoCuentaEntrada(
                            campos[0],
                            campos[1],
                            campos[2],
                            campos[3],
                            campos[4]
                    ));
                }
            }
        }

        return resultado;
    }
}