package com.example.asteroides

import android.Manifest
import android.app.Activity
import android.content.Context
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.example.asteroides.AlmacenPuntuaciones
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStreamReader

class AlmacenPuntuacionesFicheroExtApl(private val context: Context) : AlmacenPuntuaciones {

    private val nombreDelPaquete: String = context.packageName
    private val estadoSD: String = Environment.getExternalStorageState()
    private val directorio: File = context.getExternalFilesDir(null) ?: File("")

    init {
        crearDirectorio()
        solicitarPermiso()
    }

    private fun crearDirectorio() {
        val ruta = File(directorio, "Android/data/$nombreDelPaquete/files/")
        if (!ruta.exists()) {
            ruta.mkdirs()
        }
    }


    override fun guardarPuntuacion(puntos: Int, nombre: String, fecha: Long) {
        if (!estadoEsAccesible()) {
            mostrarToast("No se puede acceder a la memoria externa")
            return
        }
        try {
            val archivo = File(directorio, "puntuaciones.txt")
            val pw = FileOutputStream(archivo, true).bufferedWriter()
            pw.write("$puntos $nombre $fecha\n")
            pw.close()
        } catch (e: Exception) {
            Log.e("Asteroides", e.message, e)
        }
    }

    override fun listaPuntuaciones(cantidad: Int): List<String> {
        if (!estadoEsAccesible()) {
            mostrarToast("No se puede acceder a la memoria externa")
            return emptyList()
        }
        val result = mutableListOf<String>()
        try {
            val archivo = File(directorio, "puntuaciones.txt")
            val entrada = BufferedReader(InputStreamReader(FileInputStream(archivo)))
            var n = 0
            var linea: String?
            do {
                linea = entrada.readLine()
                if (linea != null) {
                    result.add(linea)
                    n++
                }
            } while (n < cantidad && linea != null)
            entrada.close()
        } catch (e: Exception) {
            Log.e("Asteroides", e.message, e)
        }
        return result
    }

    private fun estadoEsAccesible(): Boolean {
        return estadoSD == Environment.MEDIA_MOUNTED ||
                estadoSD == Environment.MEDIA_MOUNTED_READ_ONLY
    }

    private fun mostrarToast(mensaje: String) {
        Toast.makeText(context, mensaje, Toast.LENGTH_LONG).show()
    }

    private fun solicitarPermiso() {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                context as Activity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        ) {
            mostrarToast("El permiso de escritura en la memoria externa es necesario para guardar las puntuaciones")
        }
        ActivityCompat.requestPermissions(
            context,
            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
            1
        )
    }
}
