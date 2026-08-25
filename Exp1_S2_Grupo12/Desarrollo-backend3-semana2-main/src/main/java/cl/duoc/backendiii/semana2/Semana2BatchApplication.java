package cl.duoc.backendiii.semana2;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Punto de entrada de la aplicación.
 *
 * Comentario para clase:
 * A) Al levantar Spring Boot, también se levanta Spring Batch.
 * B) Spring Batch detecta el Job configurado y lo ejecuta automáticamente.
 * C) En esta demo, el Job consolida ventas diarias de varias sucursales.
 */
@SpringBootApplication
public class Semana2BatchApplication {

    public static void main(String[] args) {
        SpringApplication.run(Semana2BatchApplication.class, args);
    }
}
