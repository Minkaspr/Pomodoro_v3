package com.mk.pomodoro.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.mk.pomodoro.model.TipoPomodoro;

public class DaoTipoPomodoro {

    private final SQLiteDatabase db;

    public DaoTipoPomodoro(SQLiteDatabase db) {
        this.db = db;
    }

    public int obtenerIdPorNombre(String nombreTipo) {
        int idTipo = -1;
        Cursor cursor = db.query("tipo_pomodoro", new String[]{"id_tipo"}, "nombre = ?", new String[]{nombreTipo}, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            int columnIndex = cursor.getColumnIndex("id_tipo");
            if (columnIndex != -1) {
                idTipo = cursor.getInt(columnIndex);
            }
            cursor.close();
        }
        return idTipo;
    }

    public long crearNuevoTipoPomodoro(TipoPomodoro nuevoTipo) {
        ContentValues valores = new ContentValues();
        valores.put("nombre", nuevoTipo.getNombre());
        valores.put("tiempo_trabajo_establecido", nuevoTipo.getTiempoTrabajoEstablecido());
        valores.put("tiempo_descanso_establecido", nuevoTipo.getTiempoDescansoEstablecido());

        return db.insert("tipo_pomodoro", null, valores);
    }
}
