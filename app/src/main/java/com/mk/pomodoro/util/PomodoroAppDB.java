package com.mk.pomodoro.util;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class PomodoroAppDB extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "pomodoro.db";
    private static final int DATABASE_VERSION = 1;

    public PomodoroAppDB(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Crear la tabla "tipo_pomodoro"
        String tablaTipoPomodoro = "CREATE TABLE tipo_pomodoro (" +
                "id_tipo INTEGER PRIMARY KEY," +
                "nombre TEXT NOT NULL," +
                "tiempo_trabajo_establecido INTEGER NOT NULL," +
                "tiempo_descanso_establecido INTEGER NOT NULL" +
                ");";
        db.execSQL(tablaTipoPomodoro);

        // Crear la tabla "intervalo" con fecha y hora
        String tablaIntervalo = "CREATE TABLE intervalo (" +
                "id_intervalo INTEGER PRIMARY KEY," +
                "tipo_id INTEGER," +
                "es_trabajo BOOLEAN NOT NULL," +
                "fecha_inicio TEXT NOT NULL," + // Fecha y hora de inicio en formato ISO-8601
                "fecha_fin TEXT NOT NULL," + // Fecha y hora de fin en formato ISO-8601
                "FOREIGN KEY(tipo_id) REFERENCES TipoPomodoro(id_tipo)" +
                ");";
        db.execSQL(tablaIntervalo);

        // Crear la tabla "objetivo_diario" con solo fecha
        String tablaObjetivoDiario = "CREATE TABLE objetivo_diario (" +
                "id_objetivo INTEGER PRIMARY KEY," +
                "fecha TEXT NOT NULL," + // Solo fecha en formato ISO-8601
                "tiempo_objetivo INTEGER NOT NULL," + // Tiempo objetivo en minutos
                "tiempo_cumplido INTEGER NOT NULL DEFAULT 0" + // Tiempo cumplido en minutos
                ");";
        db.execSQL(tablaObjetivoDiario);

        // Insertar tipos de Pomodoro
        String insertTiposPomodoroSQL = "INSERT INTO tipo_pomodoro (" +
                "nombre, tiempo_trabajo_establecido, tiempo_descanso_establecido" +
                ") VALUES " +
                "('Extendido', 45, 15), " +
                "('Clásico', 25, 5), " +
                "('Corto', 10, 2);";
        db.execSQL(insertTiposPomodoroSQL);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public SQLiteDatabase getWritableDatabase() {
        return super.getWritableDatabase();
    }
}
