package cl.duoc.backendiii.semana2.exception;

public class DatoBancarioInvalidoException extends RuntimeException {
    public DatoBancarioInvalidoException(String message) {
        super(message);
    }
}