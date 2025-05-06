package com.example.asteroides

import android.content.Context
import java.util.Vector

class AlmacenPuntuacionesPreferencias(private val context: Context) : AlmacenPuntuaciones {
    companion object {
        private const val NUM_PUNTUACIONES = 10
        private const val PUNTUACIONES_PREF = "puntuaciones"
    }

    override fun guardarPuntuacion(puntos: Int, nombre: String, fecha: Long) {
        val preferencias = context.getSharedPreferences(PUNTUACIONES_PREF, Context.MODE_PRIVATE)
        val editor = preferencias.edit()
        val historial = mutableListOf<String>()
        for (n in 0 until NUM_PUNTUACIONES) {
            val puntuacion = preferencias.getString("puntuacion$n", "")
            if (!puntuacion.isNullOrEmpty()) {
                historial.add(puntuacion)
            }
        }
        historial.add(0, "$puntos $nombre")
        val puntuacionesGuardadas = historial.take(NUM_PUNTUACIONES)
        for ((index, puntuacion) in puntuacionesGuardadas.withIndex()) {
            editor.putString("puntuacion$index", puntuacion)
        }
        editor.apply()
    }

    override fun listaPuntuaciones(cantidad: Int): Vector<String> {
        val result = Vector<String>()
        val preferencias = context.getSharedPreferences(PUNTUACIONES_PREF, Context.MODE_PRIVATE)
        for (n in 0 until NUM_PUNTUACIONES) {
            val puntuacion = preferencias.getString("puntuacion$n", "")
            if (!puntuacion.isNullOrEmpty()) {
                result.add(puntuacion)
            }
        }
        return result
    }
}
