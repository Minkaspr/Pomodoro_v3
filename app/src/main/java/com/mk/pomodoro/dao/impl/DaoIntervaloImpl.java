package com.mk.pomodoro.dao.impl;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.mk.pomodoro.dao.DaoIntervalo;
import com.mk.pomodoro.model.Intervalo;
import com.mk.pomodoro.util.ConexionAppDB;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DaoIntervaloImpl implements DaoIntervalo {

    private final SQLiteDatabase instanciaDb;

    public DaoIntervaloImpl (Context context) {
        this.instanciaDb = ConexionAppDB.conexionPomodoroBD(context);
    }

    @Override
    public long insertarIntervalo(Intervalo intervalo) {
        ContentValues values = new ContentValues();
        values.put("tipo_id", intervalo.getTipoId());
        values.put("es_trabajo", intervalo.isEsTrabajo());
        values.put("fecha_inicio", intervalo.getFechaInicio());
        values.put("fecha_fin", intervalo.getFechaFin());
        values.put("duracion_total", intervalo.getDuracionTotal());

        return instanciaDb.insert("intervalo", null, values);
    }

    @Override
    public Intervalo obtenerUltimoIntervalo() {
        String ordernarPorIdDesc = "id_intervalo DESC"; // Ordenar por ID de intervalo en orden descendente

        try (Cursor resultadoConsulta = instanciaDb.query("intervalo", null, null, null, null, null, ordernarPorIdDesc)) {
            if (resultadoConsulta.moveToFirst()) {
                int indexIdIntervalo = resultadoConsulta.getColumnIndex("id_intervalo");
                int indexTipoId = resultadoConsulta.getColumnIndex("tipo_id");
                int indexEsTrabajo = resultadoConsulta.getColumnIndex("es_trabajo");
                int indexFechaInicio = resultadoConsulta.getColumnIndex("fecha_inicio");
                int indexFechaFin = resultadoConsulta.getColumnIndex("fecha_fin");
                int indexDuracionTotal = resultadoConsulta.getColumnIndex("duracion_total");

                int idIntervalo = resultadoConsulta.getInt(indexIdIntervalo);
                int tipoId = resultadoConsulta.getInt(indexTipoId);
                boolean esTrabajo = resultadoConsulta.getInt(indexEsTrabajo) == 1;
                String fechaInicio = resultadoConsulta.getString(indexFechaInicio);
                String fechaFin = resultadoConsulta.getString(indexFechaFin);
                int duracionTotal = resultadoConsulta.getInt(indexDuracionTotal);

                Intervalo ultimoIntervalo = new Intervalo();
                ultimoIntervalo.setIdIntervalo(idIntervalo);
                ultimoIntervalo.setTipoId(tipoId);
                ultimoIntervalo.setEsTrabajo(esTrabajo);
                ultimoIntervalo.setFechaInicio(fechaInicio);
                ultimoIntervalo.setFechaFin(fechaFin);
                ultimoIntervalo.setDuracionTotal(duracionTotal);

                return ultimoIntervalo;
            } else {
                return null;
            }
        }
    }

    @Override
    public Intervalo obtenerIntervaloPorId(int idIntervalo) {
        String filtro = "id_intervalo = ?";
        String[] argumentosFiltro = { String.valueOf(idIntervalo) };

        try (Cursor resultadoConsulta = instanciaDb.query("intervalo", null, filtro, argumentosFiltro, null, null, null)) {
            if (resultadoConsulta.moveToFirst()) {
                int indexTipoId = resultadoConsulta.getColumnIndex("tipo_id");
                int indexEsTrabajo = resultadoConsulta.getColumnIndex("es_trabajo");
                int indexFechaInicio = resultadoConsulta.getColumnIndex("fecha_inicio");
                int indexFechaFin = resultadoConsulta.getColumnIndex("fecha_fin");
                int indexDuracionTotal = resultadoConsulta.getColumnIndex("duracion_total");

                int tipoId = resultadoConsulta.getInt(indexTipoId);
                boolean esTrabajo = resultadoConsulta.getInt(indexEsTrabajo) == 1;
                String fechaInicio = resultadoConsulta.getString(indexFechaInicio);
                String fechaFin = resultadoConsulta.getString(indexFechaFin);
                int duracionTotal = resultadoConsulta.getInt(indexDuracionTotal);

                Intervalo intervalo = new Intervalo();
                intervalo.setIdIntervalo(idIntervalo);
                intervalo.setTipoId(tipoId);
                intervalo.setEsTrabajo(esTrabajo);
                intervalo.setFechaInicio(fechaInicio);
                intervalo.setFechaFin(fechaFin);
                intervalo.setDuracionTotal(duracionTotal);

                return intervalo;
            } else {
                return null; // No se encontró ningún intervalo con ese ID
            }
        }
    }

    @Override
    public int obtenerTiempoTotalPorFecha(boolean esTrabajo, String fecha) {
        int tiempoTotal = 0;

        // Seleccionar solo los intervalos del tipo (trabajo o descanso) de la fecha especificada
        String filtro = "es_trabajo = ? AND fecha_inicio LIKE ?";
        String[] argumentosFiltro = { esTrabajo ? "1" : "0", fecha + "%" };

        Cursor resultadoConsulta = instanciaDb.query("intervalo", null, filtro, argumentosFiltro, null, null, null);

        while (resultadoConsulta.moveToNext()) {
            int indexDuracionTotal = resultadoConsulta.getColumnIndex("duracion_total");

            if (indexDuracionTotal != -1) {
                int duracionTotalSegundos = resultadoConsulta.getInt(indexDuracionTotal);
                tiempoTotal += duracionTotalSegundos;
            }
        }

        resultadoConsulta.close();

        return tiempoTotal;
    }

}
