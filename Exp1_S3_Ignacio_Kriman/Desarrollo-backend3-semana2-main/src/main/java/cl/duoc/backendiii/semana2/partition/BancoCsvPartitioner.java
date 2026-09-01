
package cl.duoc.backendiii.semana2.partition;

import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.ExecutionContext;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class BancoCsvPartitioner implements Partitioner {

    @Override
    public Map<String, ExecutionContext> partition(int gridSize) {

        Map<String, ExecutionContext> result = new HashMap<>();

        int totalRecords = contarRegistrosCSV();

        if (totalRecords == 0) {
            return result;
        }

        int partitions = Math.min(gridSize, totalRecords);

        int linesPerPartition = totalRecords / partitions;
        int remainder = totalRecords % partitions;

        int currentLine = 1;

        for (int i = 0; i < partitions; i++) {

            ExecutionContext context = new ExecutionContext();

            int itemCount = linesPerPartition
                    + (i < remainder ? 1 : 0);

            context.putInt("linesToSkip", currentLine);
            context.putInt("maxItemCount", itemCount);

            result.put("partition" + (i + 1), context);

            currentLine += itemCount;
        }

        return result;
    }

    private int contarRegistrosCSV() {

        InputStream inputStream =
                getClass()
                        .getClassLoader()
                        .getResourceAsStream("input/transacciones_diarias.csv");

        if (inputStream == null) {
            throw new IllegalStateException(
                    "No se encontró el archivo input/transacciones_diarias.csv"
            );
        }

        try (BufferedReader reader =
                     new BufferedReader(
                             new InputStreamReader(inputStream))) {

            int lines = 0;

            while (reader.readLine() != null) {
                lines++;
            }

            return Math.max(0, lines - 1);

        } catch (Exception e) {
            throw new IllegalStateException(
                    "Error al contar registros de transacciones_diarias.csv",
                    e
            );
        }
    }
}

