package com.mk.pomodoro.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.mk.pomodoro.model.Intervalo;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public interface DaoIntervalo {

    long insertarIntervalo(Intervalo intervalo);
    Intervalo obtenerUltimoIntervalo();
    Intervalo obtenerIntervaloPorId(int idIntervalo);
    int obtenerTiempoTotalPorFecha(boolean esTrabajo, String fecha);
}
