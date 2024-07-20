package com.mk.pomodoro.util;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class PomodoroAppDB extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "PomodoroAppDB.db";
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
                "es_trabajo BOOLEAN NOT NULL," + // 1 (verdadero) si es un intervalo de trabajo, 0 (falso) si es de descanso
                "fecha_inicio TEXT NOT NULL," + // Fecha y hora de inicio en formato ISO-8601
                "fecha_fin TEXT NOT NULL," + // Fecha y hora de fin en formato ISO-8601
                "duracion_total INTEGER NOT NULL," + // Duración total del intervalo en milisegundos
                "FOREIGN KEY(tipo_id) REFERENCES TipoPomodoro(id_tipo)" +
                ");";
        db.execSQL(tablaIntervalo);

        // Crear la tabla "sesion"
        String tablaSesion = "CREATE TABLE sesion (" +
                "id_sesion INTEGER PRIMARY KEY," +
                "id_intervalo_trabajo INTEGER," + // Puede ser nulo
                "id_intervalo_descanso INTEGER," + // Puede ser nulo
                "fecha_inicio_sesion TEXT NOT NULL," + // Fecha y hora de inicio en formato ISO-8601
                "duracion_total_sesion INTEGER NOT NULL," + // Duración total de la sesión en milisegundos
                "completa BOOLEAN NOT NULL" + // 1 si la sesión está completa, 0 si es incompleta
                ");";
        db.execSQL(tablaSesion);

        // Crear la tabla "objetivo_diario" con solo fecha
        String tablaObjetivoDiario = "CREATE TABLE objetivo_diario (" +
                "id_objetivo INTEGER PRIMARY KEY," +
                "fecha TEXT NOT NULL," + // Solo fecha en formato ISO-8601
                "tiempo_objetivo INTEGER NOT NULL," + // Tiempo objetivo en milisegundos
                "tiempo_cumplido INTEGER NOT NULL DEFAULT 0" + // Tiempo cumplido en milisegundos
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
