package com.example.asteroides

import android.Manifest
import android.content.pm.PackageManager
import android.os.Environment
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.app.Activity
import android.content.Context
import android.util.Log
import java.io.BufferedReader
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStreamReader
import java.util.Vector

class AlmacenPuntuacionesFicheroExterno(private val context: Context) : AlmacenPuntuaciones {

    companion object {
        private const val REQUEST_EXTERNAL_STORAGE = 1
        private lateinit var FICHERO: String

        init {
            FICHERO = "${Environment.getExternalStorageDirectory()}/puntuaciones.txt"
        }
    }

    override fun guardarPuntuacion(puntos: Int, nombre: String, fecha: Long) {
        // Verificar el estado de la memoria externa
        val estadoSD = Environment.getExternalStorageState()
        if (!estadoSD.equals(Environment.MEDIA_MOUNTED)) {
            Toast.makeText(context, "No puedo escribir en la memoria externa", Toast.LENGTH_LONG).show()
            return
        }

        // Verificar si se tienen permisos
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // Si no se tienen permisos, solicitarlos al usuario
            ActivityCompat.requestPermissions(context as Activity, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUEST_EXTERNAL_STORAGE)
            // Salir del método, se continuará después de que el usuario responda a la solicitud de permisos
            return
        }

        // Continuar con la escritura del archivo si ya se tienen permisos
        try {
            val f: FileOutputStream = FileOutputStream(FICHERO, true)
            val texto = "$puntos $nombre\n"
            f.write(texto.toByteArray())
            f.close()
        } catch (e: Exception) {
            Log.e("Asteroides", e.message, e)
        }
    }

    override fun listaPuntuaciones(cantidad: Int): Vector<String> {
        // Verificar el estado de la memoria externa
        val estadoSD = Environment.getExternalStorageState()
        if (!estadoSD.equals(Environment.MEDIA_MOUNTED) && !estadoSD.equals(Environment.MEDIA_MOUNTED_READ_ONLY)) {
            Toast.makeText(context, "No puedo leer en la memoria externa", Toast.LENGTH_LONG).show()
            return Vector()
        }

        // Verificar si se tienen permisos
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // Si no se tienen permisos, solicitarlos al usuario
            ActivityCompat.requestPermissions(context as Activity, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), REQUEST_EXTERNAL_STORAGE)
            // Salir del método, se continuará después de que el usuario responda a la solicitud de permisos
            return Vector()
        }

        val result: Vector<String> = Vector()
        try {
            val f: FileInputStream = FileInputStream(FICHERO)
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
