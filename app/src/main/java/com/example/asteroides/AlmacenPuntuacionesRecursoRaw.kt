package com.example.asteroides

import android.content.Context
import android.util.Log
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.util.*

class AlmacenPuntuacionesRecursoRaw(private val context: Context) : AlmacenPuntuaciones {

    override fun guardarPuntuacion(puntos: Int, nombre: String, fecha: Long) {
    }

    override fun listaPuntuaciones(cantidad: Int): Vector<String> {
        val result: Vector<String> = Vector()
        try {
            val f: InputStream = context.resources.openRawResource(R.raw.puntuaciones)
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
