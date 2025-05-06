package com.example.asteroides

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.util.Vector

class AlmacenPuntuacionesSQLite(context: Context) :
    SQLiteOpenHelper(context, "puntuaciones", null, 1),
    AlmacenPuntuaciones {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE puntuaciones (" +
                "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "puntos INTEGER, nombre TEXT, fecha BIGINT)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // En caso de una nueva versión habría que actualizar las tablas
    }

    override fun guardarPuntuacion(puntos: Int, nombre: String, fecha: Long) {
        val db = writableDatabase
        db.execSQL("INSERT INTO puntuaciones VALUES ( null, " +
                "$puntos, '$nombre', $fecha)")
        db.close()
    }

    override fun listaPuntuaciones(cantidad: Int): Vector<String> {
        val result = Vector<String>()
        val db = readableDatabase
        val campos = arrayOf("puntos", "nombre")
        val cursor: Cursor = db.query("puntuaciones", campos, null, null,
            null, null, "puntos DESC", cantidad.toString())
        while (cursor.moveToNext()) {
            result.add("${cursor.getInt(0)} ${cursor.getString(1)}")
        }
        cursor.close()
        db.close()
        return result
    }
}
