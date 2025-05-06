package com.example.asteroides

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class PreferenciasActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportFragmentManager.beginTransaction()
            .replace(android.R.id.content, PreferenciasFragment())
            .commit()
    }
}
