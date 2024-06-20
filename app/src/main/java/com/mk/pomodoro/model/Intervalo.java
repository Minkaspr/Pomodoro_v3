package com.mk.pomodoro.model;

public class Intervalo {
    private int idIntervalo;
    private int tipoId;
    private boolean esTrabajo;
    private String fechaInicio;
    private String fechaFin;
    private int duracionTotal;

    public Intervalo() {
    }

    public int getIdIntervalo() {
        return idIntervalo;
    }

    public void setIdIntervalo(int idIntervalo) {
        this.idIntervalo = idIntervalo;
    }

    public int getTipoId() {
        return tipoId;
    }

    public void setTipoId(int tipoId) {
        this.tipoId = tipoId;
    }

    public boolean isEsTrabajo() {
        return esTrabajo;
    }

    public void setEsTrabajo(boolean esTrabajo) {
        this.esTrabajo = esTrabajo;
    }

    public String getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(String fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public String getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(String fechaFin) {
        this.fechaFin = fechaFin;
    }

    public int getDuracionTotal() {
        return duracionTotal;
    }

    public void setDuracionTotal(int duracionTotal) {
        this.duracionTotal = duracionTotal;
    }
}
