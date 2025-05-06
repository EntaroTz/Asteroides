package com.example.asteroides

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView


class Puntuaciones : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adaptador: MiAdaptador
    private lateinit var almacenPuntuaciones: AlmacenPuntuaciones

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.puntuaciones)

        recyclerView = findViewById(R.id.recyclerView)
        almacenPuntuaciones = AlmacenPuntuacionesPreferencias(this) // Utiliza AlmacenPuntuacionesPreferencias
        adaptador = MiAdaptador(this, almacenPuntuaciones.listaPuntuaciones(10)) {
            val pos: Int = recyclerView.getChildAdapterPosition(it)
            Toast.makeText(this, "pulsado $pos", Toast.LENGTH_LONG).show()
        }

        recyclerView.adapter = adaptador
        recyclerView.layoutManager = LinearLayoutManager(this)
    }
}
