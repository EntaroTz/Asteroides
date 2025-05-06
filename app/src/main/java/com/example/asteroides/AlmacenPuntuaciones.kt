package com.example.asteroides

interface AlmacenPuntuaciones {
    fun guardarPuntuacion(puntos: Int, nombre: String, fecha: Long)
    fun listaPuntuaciones(cantidad: Int): List<String>
}
