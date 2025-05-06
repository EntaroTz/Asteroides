package com.example.asteroides

class AlmacenPuntuacionesList : AlmacenPuntuaciones {
    private val puntuaciones: MutableList<String>

    init {
        puntuaciones = mutableListOf()
        puntuaciones.add("123000 Pepito Domingez")
        puntuaciones.add("111000 Pedro Martinez")
        puntuaciones.add("011000 Paco Pérez")
    }

    override fun guardarPuntuacion(puntos: Int, nombre: String, fecha: Long) {
        puntuaciones.add(0, "$puntos $nombre")
    }

    override fun listaPuntuaciones(cantidad: Int): List<String> {
        return puntuaciones.take(cantidad)
    }
}
