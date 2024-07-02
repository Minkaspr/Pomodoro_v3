package com.mk.pomodoro.util;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

public class ConexionAppDB {
    private static PomodoroAppDB pomodoroAppDB;
    private static SQLiteDatabase instanciaDb;

    public static synchronized SQLiteDatabase conexionPomodoroBD(Context context) {
        if (pomodoroAppDB == null) {
            pomodoroAppDB = new PomodoroAppDB(context.getApplicationContext());
        }
        if (instanciaDb == null || !instanciaDb.isOpen()) {
            instanciaDb = pomodoroAppDB.getWritableDatabase();
        }
        return instanciaDb;
    }

    public static synchronized void cerrarConexionBD() {
        if (instanciaDb != null && instanciaDb.isOpen()) {
            instanciaDb.close();
            instanciaDb = null;
        }
        if (pomodoroAppDB != null) {
            pomodoroAppDB.close();
            pomodoroAppDB = null;
        }
    }
}
