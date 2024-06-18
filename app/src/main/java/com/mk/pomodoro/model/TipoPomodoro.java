package com.mk.pomodoro.model;

public class TipoPomodoro {
    private int idTipo;
    private String nombre;
    private int tiempoTrabajoEstablecido;
    private int tiempoDescansoEstablecido;

    public TipoPomodoro() {
    }

    public int getIdTipo() {
        return idTipo;
    }

    public void setIdTipo(int idTipo) {
        this.idTipo = idTipo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public int getTiempoTrabajoEstablecido() {
        return tiempoTrabajoEstablecido;
    }

    public void setTiempoTrabajoEstablecido(int tiempoTrabajoEstablecido) {
        this.tiempoTrabajoEstablecido = tiempoTrabajoEstablecido;
    }

    public int getTiempoDescansoEstablecido() {
        return tiempoDescansoEstablecido;
    }

    public void setTiempoDescansoEstablecido(int tiempoDescansoEstablecido) {
        this.tiempoDescansoEstablecido = tiempoDescansoEstablecido;
    }
}
