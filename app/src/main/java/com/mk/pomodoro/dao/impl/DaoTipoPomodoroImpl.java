package com.mk.pomodoro.dao.impl;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.mk.pomodoro.dao.DaoTipoPomodoro;
import com.mk.pomodoro.model.TipoPomodoro;
import com.mk.pomodoro.util.ConexionAppDB;

public class DaoTipoPomodoroImpl implements DaoTipoPomodoro {

    private final SQLiteDatabase instanciaDb;

    public DaoTipoPomodoroImpl(Context context) {
        this.instanciaDb = ConexionAppDB.conexionPomodoroBD(context);
    }

    @Override
    public long insertarTipoPomodoro(TipoPomodoro nuevoTipo) {
        ContentValues valores = new ContentValues();
        valores.put("nombre", nuevoTipo.getNombre());
        valores.put("tiempo_trabajo_establecido", nuevoTipo.getTiempoTrabajoEstablecido());
        valores.put("tiempo_descanso_establecido", nuevoTipo.getTiempoDescansoEstablecido());

        return instanciaDb.insert("tipo_pomodoro", null, valores);
    }

    @Override
    public int obtenerIdPorNombre(String nombreTipo) {
        int idTipo = -1;
        Cursor cursor = instanciaDb.query("tipo_pomodoro", new String[]{"id_tipo"}, "nombre = ?", new String[]{nombreTipo}, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            int columnIndex = cursor.getColumnIndex("id_tipo");
            if (columnIndex != -1) {
                idTipo = cursor.getInt(columnIndex);
            }
            cursor.close();
        }
        return idTipo;
    }
}
