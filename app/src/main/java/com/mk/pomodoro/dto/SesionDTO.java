package com.mk.pomodoro.dto;

public class SesionDTO {
    private int numeroSesion;
    private boolean completa;
    private int trabajoDuracionTotal;
    private int descansoDuracionTotal;

    public SesionDTO() {
    }

    public int getNumeroSesion() {
        return numeroSesion;
    }

    public void setNumeroSesion(int numeroSesion) {
        this.numeroSesion = numeroSesion;
    }

    public boolean isCompleta() {
        return completa;
    }

    public void setCompleta(boolean completa) {
        this.completa = completa;
    }

    public int getTrabajoDuracionTotal() {
        return trabajoDuracionTotal;
    }

    public void setTrabajoDuracionTotal(int trabajoDuracionTotal) {
        this.trabajoDuracionTotal = trabajoDuracionTotal;
    }

    public int getDescansoDuracionTotal() {
        return descansoDuracionTotal;
    }

    public void setDescansoDuracionTotal(int descansoDuracionTotal) {
        this.descansoDuracionTotal = descansoDuracionTotal;
    }
}
