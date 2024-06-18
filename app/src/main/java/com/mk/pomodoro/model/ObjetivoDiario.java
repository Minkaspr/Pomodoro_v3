package com.mk.pomodoro.model;

public class ObjetivoDiario {
    private int idObjetivo;
    private String fecha;
    private int tiempoObjetivo;
    private int tiempoCumplido;

    public ObjetivoDiario() {
    }

    public int getIdObjetivo() {
        return idObjetivo;
    }

    public void setIdObjetivo(int idObjetivo) {
        this.idObjetivo = idObjetivo;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public int getTiempoObjetivo() {
        return tiempoObjetivo;
    }

    public void setTiempoObjetivo(int tiempoObjetivo) {
        this.tiempoObjetivo = tiempoObjetivo;
    }

    public int getTiempoCumplido() {
        return tiempoCumplido;
    }

    public void setTiempoCumplido(int tiempoCumplido) {
        this.tiempoCumplido = tiempoCumplido;
    }
}
