package cl.duoc.backendiii.semana2.reader;

import cl.duoc.backendiii.semana2.model.TransaccionDiaria;
import org.springframework.batch.item.ItemReader;
import org.springframework.core.io.ClassPathResource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class TransaccionesDiariasReader implements ItemReader<TransaccionDiaria> {

    private final List<TransaccionDiaria> transacciones;
    private int indiceActual = 0;

    public TransaccionesDiariasReader(String resourcePath) throws IOException {
        this.transacciones = cargarDatos(resourcePath);
    }

    @Override
    public synchronized TransaccionDiaria read() {
        if (indiceActual >= transacciones.size()) {
            return null;
        }
        return transacciones.get(indiceActual++);
    }

    private List<TransaccionDiaria> cargarDatos(String resourcePath) throws IOException {
        List<TransaccionDiaria> resultado = new ArrayList<>();
        ClassPathResource resource = new ClassPathResource(resourcePath);

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
            String linea;
            boolean primeraLinea = true;
            while ((linea = reader.readLine()) != null) {
                if (primeraLinea) {
                    primeraLinea = false;
                    continue; // Saltamos la cabecera
                }
                // Separamos por coma o punto y coma según el formato del CSV
                String[] campos = linea.split("[,;]", -1); 
                if (campos.length >= 4) {
                    resultado.add(new TransaccionDiaria(campos[0], campos[1], campos[2], campos[3]));
                }
            }
        }
        return resultado;
    }
}