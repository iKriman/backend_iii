package cl.duoc.backendiii.semana2.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Transaccion {
    private String id;
    private String cuenta;
    private BigDecimal monto;
    private String tipo;
    private String estado;
    private LocalDate fecha;

    public Transaccion() {}

    public Transaccion(String id, String cuenta, BigDecimal monto, String tipo, String estado, LocalDate fecha) {
        this.id = id;
        this.cuenta = cuenta;
        this.monto = monto;
        this.tipo = tipo;
        this.estado = estado;
        this.fecha = fecha;
    }

    // Getters y Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getCuenta() { return cuenta; }
    public void setCuenta(String cuenta) { this.cuenta = cuenta; }

    public BigDecimal getMonto() { return monto; }
    public void setMonto(BigDecimal monto) { this.monto = monto; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }
}