package com.mk.pomodoro.dao.impl;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

import com.mk.pomodoro.dao.DaoIntervalo;
import com.mk.pomodoro.dao.DaoSesion;
import com.mk.pomodoro.dto.SesionDTO;
import com.mk.pomodoro.model.Intervalo;
import com.mk.pomodoro.model.Sesion;
import com.mk.pomodoro.util.ConexionAppDB;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DaoSesionImpl implements DaoSesion {

    private final SQLiteDatabase instanciaDb;
    private DaoIntervalo daoIntervalo;

    public DaoSesionImpl(Context context){
        this.instanciaDb = ConexionAppDB.conexionPomodoroBD(context);
        daoIntervalo = new DaoIntervaloImpl(context);
    }

    @Override
    public long insertarSesion(Sesion sesion) {
        ContentValues values = new ContentValues();
        values.put("id_intervalo_trabajo", sesion.getIdIntervaloTrabajo());
        values.put("id_intervalo_descanso", sesion.getIdIntervaloDescanso());
        values.put("fecha_inicio_sesion", sesion.getFechaInicioSesion());
        values.put("duracion_total_sesion", sesion.getDuracionTotalSesion());
        values.put("completa", sesion.isCompleta());

        try {
            return instanciaDb.insertOrThrow("sesion", null, values); // Devuelve el ID del nuevo registro insertado
        } catch (SQLiteException e) {
            Log.e("Pomodoro", "Error al insertar sesión", e);
            return -1;
        }
    }

    @Override
    public long actualizarSesion(Sesion sesion) {
        ContentValues values = new ContentValues();
        values.put("id_intervalo_trabajo", sesion.getIdIntervaloTrabajo());
        values.put("id_intervalo_descanso", sesion.getIdIntervaloDescanso());
        values.put("fecha_inicio_sesion", sesion.getFechaInicioSesion());
        values.put("duracion_total_sesion", sesion.getDuracionTotalSesion());
        values.put("completa", sesion.isCompleta());

        String whereClause = "id_sesion = ?";
        String[] whereArgs = { String.valueOf(sesion.getIdSesion()) };

        try {
            return instanciaDb.update("sesion", values, whereClause, whereArgs); // Devuelve el número de filas actualizadas
        } catch (SQLiteException e) {
            Log.e("Pomodoro", "Error al actualizar sesión", e);
            return -1;
        }
    }

    @Override
    public Sesion obtenerUltimaSesion() {
        String orderBy = "id_sesion DESC"; // Ordenar por ID de sesión en orden descendente

        try (Cursor resultadoConsulta = instanciaDb.query("sesion", null, null, null, null, null, orderBy)) {
            if (resultadoConsulta.moveToFirst()) {
                int indexIdSesion = resultadoConsulta.getColumnIndex("id_sesion");
                int indexIdIntervaloTrabajo = resultadoConsulta.getColumnIndex("id_intervalo_trabajo");
                int indexIdIntervaloDescanso = resultadoConsulta.getColumnIndex("id_intervalo_descanso");
                int indexFechaInicioSesion = resultadoConsulta.getColumnIndex("fecha_inicio_sesion");
                int indexDuracionTotalSesion = resultadoConsulta.getColumnIndex("duracion_total_sesion");
                int indexCompleta = resultadoConsulta.getColumnIndex("completa");

                int idSesion = resultadoConsulta.getInt(indexIdSesion);
                Integer idIntervaloTrabajo = resultadoConsulta.getInt(indexIdIntervaloTrabajo);
                Integer idIntervaloDescanso = resultadoConsulta.getInt(indexIdIntervaloDescanso);
                String fechaInicioSesion = resultadoConsulta.getString(indexFechaInicioSesion);
                int duracionTotalSesion = resultadoConsulta.getInt(indexDuracionTotalSesion);
                boolean completa = resultadoConsulta.getInt(indexCompleta) == 1;

                Sesion ultimaSesion = new Sesion();
                ultimaSesion.setIdSesion(idSesion);
                ultimaSesion.setIdIntervaloTrabajo(idIntervaloTrabajo);
                ultimaSesion.setIdIntervaloDescanso(idIntervaloDescanso);
                ultimaSesion.setFechaInicioSesion(fechaInicioSesion);
                ultimaSesion.setDuracionTotalSesion(duracionTotalSesion);
                ultimaSesion.setCompleta(completa);

                return ultimaSesion;
            } else {
                return null; // No se encontraron sesiones registradas
            }
        } catch (SQLiteException e) {
            Log.e("Pomodoro", "Error al obtener la última sesión", e);
            return null;
        }
    }

    @Override
    public Integer obtenerCantidadSesiones(String fecha) {
        String filtro = "fecha_inicio_sesion LIKE ?";
        String[] argumentosFiltro = { fecha + "%" }; // fechas que inicien con el valor de fechaHoy

        try (Cursor resultadoConsulta = instanciaDb.query("sesion", null, filtro, argumentosFiltro, null, null, null)) {
            return resultadoConsulta.getCount();
        }  catch (SQLiteException e) {
            Log.e("Pomodoro", "Error al obtener la cantidad de sesiones de hoy", e);
            return 0;
        }
    }

    @Override
    public List<SesionDTO> obtenerSesiones(String fecha, int cantidadMaxima, int filasOmitidas) {
        List<SesionDTO> sesiones = new ArrayList<>();
        String query = "SELECT * FROM sesion WHERE fecha_inicio_sesion LIKE ? ORDER BY id_sesion DESC LIMIT ? OFFSET ?";
        String[] argumentosFiltro = { fecha + "%", String.valueOf(cantidadMaxima), String.valueOf(filasOmitidas) };

        try (Cursor cursorSesion = instanciaDb.rawQuery(query, argumentosFiltro)) {
            int totalSesiones = obtenerCantidadSesiones(fecha);
            int numeroSesion = totalSesiones - filasOmitidas;

            while (cursorSesion.moveToNext()) {
                SesionDTO sesionDTO = new SesionDTO();
                sesionDTO.setNumeroSesion(numeroSesion--);

                int completaIndex = cursorSesion.getColumnIndex("completa");
                int idIntervaloTrabajoIndex = cursorSesion.getColumnIndex("id_intervalo_trabajo");
                int idIntervaloDescansoIndex = cursorSesion.getColumnIndex("id_intervalo_descanso");

                if (completaIndex != -1) {
                    sesionDTO.setCompleta(cursorSesion.getInt(completaIndex) == 1);
                }

                if (idIntervaloTrabajoIndex != -1) {
                    int idIntervaloTrabajo = cursorSesion.getInt(idIntervaloTrabajoIndex);
                    if (idIntervaloTrabajo != 0) {
                        Intervalo intervaloTrabajo = daoIntervalo.obtenerIntervaloPorId(idIntervaloTrabajo);
                        sesionDTO.setTrabajoDuracionTotal(intervaloTrabajo.getDuracionTotal());
                    } else {
                        sesionDTO.setTrabajoDuracionTotal(0);
                    }
                }

                if (idIntervaloDescansoIndex != -1) {
                    int idIntervaloDescanso = cursorSesion.getInt(idIntervaloDescansoIndex);
                    if (idIntervaloDescanso != 0) {
                        Intervalo intervaloDescanso = daoIntervalo.obtenerIntervaloPorId(idIntervaloDescanso);
                        sesionDTO.setDescansoDuracionTotal(intervaloDescanso.getDuracionTotal());
                    } else {
                        sesionDTO.setDescansoDuracionTotal(0);
                    }
                }
                sesiones.add(sesionDTO);
            }
        } catch (SQLiteException e) {
            Log.e("Pomodoro", "Error al obtener las sesiones de hoy", e);
        }
        return sesiones;
    }
}
