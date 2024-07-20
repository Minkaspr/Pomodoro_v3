package com.mk.pomodoro.dao;


import com.mk.pomodoro.dto.SesionDTO;
import com.mk.pomodoro.model.Sesion;

import java.util.List;

public interface DaoSesion {

    long insertarSesion(Sesion sesion);
    long actualizarSesion(Sesion sesion);
    Sesion obtenerUltimaSesion();
    Integer obtenerCantidadSesiones(String fecha);
    List<SesionDTO> obtenerSesiones(String fecha, int cantidadMaxima, int filasOmitidas);
}
