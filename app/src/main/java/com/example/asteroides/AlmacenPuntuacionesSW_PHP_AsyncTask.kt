package com.example.asteroides

import android.content.Context
import android.os.AsyncTask
import android.util.Log
import android.widget.Toast
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.ArrayList
import java.util.concurrent.CancellationException
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

class AlmacenPuntuacionesSW_PHP_AsyncTask(private val context: Context) : AlmacenPuntuaciones {
    override fun listaPuntuaciones(cantidad: Int): List<String> {
        return try {
            val tarea = TareaLista()
            tarea.execute().get(4, TimeUnit.SECONDS)
        } catch (e: TimeoutException) {
            Toast.makeText(context, "Tiempo de espera agotado", Toast.LENGTH_LONG).show()
            ArrayList()
        } catch (e: CancellationException) {
            Toast.makeText(context, "Error al conectar con el servidor", Toast.LENGTH_LONG).show()
            ArrayList()
        } catch (e: Exception) {
            Toast.makeText(context, "Error en la conexión", Toast.LENGTH_LONG).show()
            ArrayList()
        }
    }

    override fun guardarPuntuacion(puntos: Int, nombre: String, fecha: Long) {
        try {
            val url = URL("https://192.168.1.71/puntuaciones/nueva.php?nombre=$nombre&puntos=$puntos&fecha=$fecha")
            val urlConnection = url.openConnection() as HttpURLConnection
            if (urlConnection.responseCode == HttpURLConnection.HTTP_OK) {
                val `in` = BufferedReader(InputStreamReader(urlConnection.inputStream))
                val respuesta = `in`.readLine()
                if (respuesta != "OK") {
                    throw RuntimeException("Error en el servidor: $respuesta")
                }
                `in`.close()
            } else {
                Log.e("Asteroides", "Error en la conexión HTTP: ${urlConnection.responseMessage}")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }    }

    private inner class TareaLista : AsyncTask<String, Void, List<String>>() {
        override fun doInBackground(vararg strings: String): List<String> {
            val result = ArrayList<String>()
            var urlConnection: HttpURLConnection? = null
            try {
                val url = URL("https://192.168.1.71/puntuaciones/lista.php?max=20")
                urlConnection = url.openConnection() as HttpURLConnection
                if (urlConnection.responseCode == HttpURLConnection.HTTP_OK) {
                    val `in` = BufferedReader(InputStreamReader(urlConnection.inputStream))
                    var linea = `in`.readLine()
                    while (linea != null) {
                        result.add(linea)
                        linea = `in`.readLine()
                    }
                    `in`.close()
                } else {
                    Log.e("Asteroides", "Error en la conexión HTTP: ${urlConnection.responseMessage}")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                urlConnection?.disconnect()
            }
            return result
        }
    }
}
