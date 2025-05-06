package com.example.asteroides

import android.content.Context
import android.util.Log
import java.io.BufferedReader
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStreamReader
import java.util.*

class AlmacenPuntuacionesFicheroInterno(private val context: Context) : AlmacenPuntuaciones {
    companion object {
        private const val FICHERO = "puntuaciones.txt"
    }

    override fun guardarPuntuacion(puntos: Int, nombre: String, fecha: Long) {
        try {
            val f: FileOutputStream = context.openFileOutput(FICHERO, Context.MODE_APPEND)
            val texto = "$puntos $nombre\n"
            f.write(texto.toByteArray())
            f.close()
        } catch (e: Exception) {
            Log.e("Asteroides", e.message, e)
        }
    }

    override fun listaPuntuaciones(cantidad: Int): Vector<String> {
        val result: Vector<String> = Vector()
        try {
            val f: FileInputStream = context.openFileInput(FICHERO)
            val entrada = BufferedReader(InputStreamReader(f))
            var n = 0
            var linea: String?
            do {
                linea = entrada.readLine()
                if (linea != null) {
                    result.add(linea)
                    n++
                }
            } while (n < cantidad && linea != null)
            f.close()
        } catch (e: Exception) {
            Log.e("Asteroides", e.message, e)
        }
        return result
    }
}
