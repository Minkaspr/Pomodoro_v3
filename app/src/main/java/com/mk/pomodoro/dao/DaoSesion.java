package com.mk.pomodoro.dao;

import com.mk.pomodoro.model.SesionDTO;

import java.util.List;

public interface DaoSesion {

    long insertarSesion(SesionDTO sesion);
    long actualizarSesion(SesionDTO sesion);
    SesionDTO obtenerUltimaSesion();
    Integer obtenerCantidadSesiones(String fecha);
    List<com.mk.pomodoro.dto.SesionDTO> obtenerSesiones(String fecha);
}
