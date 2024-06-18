package com.mk.pomodoro.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.mk.pomodoro.model.Intervalo;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DaoIntervalo {

    private final SQLiteDatabase db;

    public DaoIntervalo(SQLiteDatabase db) {
        this.db = db;
    }

    public long insertarIntervalo(Intervalo intervalo) {
        ContentValues values = new ContentValues();
        values.put("tipo_id", intervalo.getTipoId());
        values.put("es_trabajo", intervalo.isEsTrabajo());
        values.put("fecha_inicio", intervalo.getFechaInicio());
        values.put("fecha_fin", intervalo.getFechaFin());

        return db.insert("intervalo", null, values);
    }

    public long obtenerTiempoTotalTrabajoDelDia() {
        long tiempoTotal = 0;
        String fechaHoy = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        // Seleccionar solo los intervalos de trabajo del día actual
        String selection = "es_trabajo = ? AND fecha_inicio LIKE ?";
        String[] selectionArgs = { "1", fechaHoy + "%" };

        Cursor cursor = db.query("intervalo", null, selection, selectionArgs, null, null, null);

        while (cursor.moveToNext()) {


            int indexFechaInicio = cursor.getColumnIndex("fecha_inicio");
            int indexFechaFin = cursor.getColumnIndex("fecha_fin");

            if (indexFechaInicio != -1 && indexFechaFin != -1) {
                String fechaInicio = cursor.getString(indexFechaInicio);
                String fechaFin = cursor.getString(indexFechaFin);

                // Convertir las fechas a milisegundos
                long inicioMillis = convertirFechaAMillis(fechaInicio);
                long finMillis = convertirFechaAMillis(fechaFin);

                // Calcular la duración del intervalo y sumarla al tiempo total
                tiempoTotal += (finMillis - inicioMillis);
            }
        }

        cursor.close();

        return tiempoTotal;
    }

    private long convertirFechaAMillis(String fecha) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            Date date = sdf.parse(fecha);
            return date.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
            return 0;
        }
    }

}
