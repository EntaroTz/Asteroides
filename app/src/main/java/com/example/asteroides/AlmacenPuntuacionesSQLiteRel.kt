package com.example.asteroides

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class AlmacenPuntuacionesSQLiteRel(context: Context) :
    SQLiteOpenHelper(context, "puntuaciones", null, 2),
    AlmacenPuntuaciones {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE usuarios (" +
                "usu_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "nombre TEXT, correo TEXT)")
        db.execSQL("CREATE TABLE puntuaciones2 (" +
                "pun_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "puntos INTEGER, fecha BIGINT, usuario INTEGER, " +
                "FOREIGN KEY (usuario) REFERENCES usuarios (usu_id))")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion == 1 && newVersion == 2) {
            onCreate(db)
            val cursor = db.rawQuery("SELECT puntos, nombre, fecha " +
                    "FROM puntuaciones", null)
            while (cursor.moveToNext()) {
                guardarPuntuacion(db, cursor.getInt(0), cursor.getString(1),
                    cursor.getInt(2).toLong()) // Convertir Int a Long
            }
            cursor.close()
            db.execSQL("DROP TABLE puntuaciones") // Elimina tabla antigua
        }
    }



    override fun listaPuntuaciones(cantidad: Int): List<String> {
        val result: MutableList<String> = ArrayList()
        val db: SQLiteDatabase = readableDatabase
        val cursor: Cursor = db.rawQuery("SELECT puntos, nombre FROM " +
                "puntuaciones2, usuarios WHERE usuario = usu_id ORDER BY " +
                "puntos DESC LIMIT $cantidad", null)
        while (cursor.moveToNext()) {
            result.add(cursor.getInt(0).toString() + " " + cursor.getString(1))
        }
        cursor.close()
        db.close()
        return result
    }

    override fun guardarPuntuacion(puntos: Int, nombre: String, fecha: Long) {
        val db: SQLiteDatabase = writableDatabase
        guardarPuntuacion(db, puntos, nombre, fecha)
        db.close()
    }

    private fun guardarPuntuacion(db: SQLiteDatabase, puntos: Int, nombre: String, fecha: Long) {
        val usuario = buscaInserta(db, nombre)
        db.execSQL("PRAGMA foreign_keys = ON")
        db.execSQL("INSERT INTO puntuaciones2 VALUES ( null, " +
                "$puntos, $fecha, $usuario)")
    }

    private fun buscaInserta(db: SQLiteDatabase, nombre: String): Int {
        val cursor: Cursor = db.rawQuery("SELECT usu_id FROM usuarios " +
                "WHERE nombre='$nombre'", null)
        return if (cursor.moveToNext()) {
            val result = cursor.getInt(0)
            cursor.close()
            result
        } else {
            cursor.close()
            db.execSQL("INSERT INTO usuarios VALUES (null, '$nombre' , 'correo@dominio.es')")
            buscaInserta(db, nombre)
        }
    }
}
