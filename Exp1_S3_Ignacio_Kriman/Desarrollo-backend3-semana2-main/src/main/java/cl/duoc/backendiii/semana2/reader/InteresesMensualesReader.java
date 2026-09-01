package cl.duoc.backendiii.semana2.reader;

import cl.duoc.backendiii.semana2.model.CuentaInteres;
import org.springframework.batch.item.ItemReader;
import org.springframework.core.io.ClassPathResource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class InteresesMensualesReader implements ItemReader<CuentaInteres> {

    private final List<CuentaInteres> cuentas;
    private int indiceActual = 0;

    public InteresesMensualesReader(String resourcePath) throws IOException {
        this.cuentas = cargarDatos(resourcePath);
    }

    @Override
    public synchronized CuentaInteres read() {
        if (indiceActual >= cuentas.size()) {
            return null;
        }
        return cuentas.get(indiceActual++);
    }

    private List<CuentaInteres> cargarDatos(String resourcePath) throws IOException {
        List<CuentaInteres> resultado = new ArrayList<>();
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
                    resultado.add(new CuentaInteres(
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