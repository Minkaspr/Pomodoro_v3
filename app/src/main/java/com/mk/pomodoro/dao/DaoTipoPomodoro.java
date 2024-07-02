package com.mk.pomodoro.dao;

import com.mk.pomodoro.model.TipoPomodoro;

public interface DaoTipoPomodoro {

    long insertarTipoPomodoro(TipoPomodoro nuevoTipo);
    int obtenerIdPorNombre(String nombreTipo);
}
