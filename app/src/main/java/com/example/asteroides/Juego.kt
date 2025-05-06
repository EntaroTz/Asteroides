package com.example.asteroides

import android.app.Activity
import android.os.Bundle

class Juego : Activity() {

    private lateinit var vistaJuego: VistaJuego

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.juego)

        vistaJuego = findViewById(R.id.VistaJuego)
        vistaJuego.setPadre(this)
        vistaJuego.activarSensores()
    }

    override fun onPause() {
        super.onPause()
        vistaJuego.thread.pausar()
        vistaJuego.desactivarSensores()
    }

    override fun onResume() {
        super.onResume()
        vistaJuego.thread.reanudar()
        vistaJuego.activarSensores()
    }

    override fun onDestroy() {
        vistaJuego.thread.detener()
        super.onDestroy()
    }
}

