package com.mk.pomodoro.model;

public class SesionDTO {
    private int idSesion;
    private Integer idIntervaloTrabajo;
    private Integer idIntervaloDescanso;
    private String fechaInicioSesion;
    private int duracionTotalSesion;
    private boolean completa;

    public SesionDTO() {
    }

    public int getIdSesion() {
        return idSesion;
    }

    public void setIdSesion(int idSesion) {
        this.idSesion = idSesion;
    }

    public Integer getIdIntervaloTrabajo() {
        return idIntervaloTrabajo;
    }

    public void setIdIntervaloTrabajo(Integer idIntervaloTrabajo) {
        this.idIntervaloTrabajo = idIntervaloTrabajo;
    }

    public Integer getIdIntervaloDescanso() {
        return idIntervaloDescanso;
    }

    public void setIdIntervaloDescanso(Integer idIntervaloDescanso) {
        this.idIntervaloDescanso = idIntervaloDescanso;
    }

    public String getFechaInicioSesion() {
        return fechaInicioSesion;
    }

    public void setFechaInicioSesion(String fechaInicioSesion) {
        this.fechaInicioSesion = fechaInicioSesion;
    }

    public int getDuracionTotalSesion() {
        return duracionTotalSesion;
    }

    public void setDuracionTotalSesion(int duracionTotalSesion) {
        this.duracionTotalSesion = duracionTotalSesion;
    }

    public boolean isCompleta() {
        return completa;
    }

    public void setCompleta(boolean completa) {
        this.completa = completa;
    }
}
