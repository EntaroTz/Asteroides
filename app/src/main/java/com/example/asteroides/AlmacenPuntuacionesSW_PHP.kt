package com.example.asteroides

import android.util.Log
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.*

class AlmacenPuntuacionesSW_PHP : AlmacenPuntuaciones {

    override fun listaPuntuaciones(cantidad: Int): Vector<String> {
        val result = Vector<String>()
        var conexion: HttpURLConnection? = null
        try {
            val url = URL("http://192.168.1.221/puntuaciones/lista.php?max=20")
            conexion = url.openConnection() as HttpURLConnection
            if (conexion.responseCode == HttpURLConnection.HTTP_OK) {
                val reader = BufferedReader(InputStreamReader(conexion.inputStream))
                var linea: String? = reader.readLine()
                while (linea != null && linea.isNotEmpty()) {
                    result.add(linea)
                    linea = reader.readLine()
                }
                reader.close()
            } else {
                Log.e("Asteroides", conexion.responseMessage)
            }
        } catch (e: Exception) {
            Log.e("Asteroides", e.message, e)
        } finally {
            conexion?.disconnect()
        }
        return result
    }

    override fun guardarPuntuacion(puntos: Int, nombre: String, fecha: Long) {
        var conexion: HttpURLConnection? = null
        try {
            val url = URL("http://192.168.1.221/puntuaciones/nueva.php?" +
                    "puntos=$puntos&nombre=${URLEncoder.encode(nombre, "UTF-8")}&fecha=$fecha")
            conexion = url.openConnection() as HttpURLConnection
            if (conexion.responseCode == HttpURLConnection.HTTP_OK) {
                val reader = BufferedReader(InputStreamReader(conexion.inputStream))
                val linea = reader.readLine()
                if (linea != "OK") {
                    Log.e("Asteroides", "Error en servicio Web nueva")
                }
                reader.close()
            } else {
                Log.e("Asteroides", conexion.responseMessage)
            }
        } catch (e: Exception) {
            Log.e("Asteroides", e.message, e)
        } finally {
            conexion?.disconnect()
        }
    }
}
