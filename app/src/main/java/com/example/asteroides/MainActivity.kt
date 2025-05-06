package com.example.asteroides

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.MenuItem
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.RequestQueue
import com.android.volley.toolbox.ImageLoader
import com.android.volley.toolbox.Volley
import android.os.StrictMode



class MainActivity : AppCompatActivity() {

    private lateinit var almacenPuntuaciones: AlmacenPuntuaciones
    private lateinit var mp: MediaPlayer
    private var musicPosition: Int = 0
    private var reproduceMusicaFondo: Boolean = true
    private val REQUEST_CODE = 1234

    /*companion object {
        lateinit var colaPeticiones: RequestQueue
        lateinit var lectorImagenes: ImageLoader
    }*/

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        StrictMode.setThreadPolicy(
            StrictMode.ThreadPolicy.Builder()
                .permitNetwork()
                .build()
        )

        /*colaPeticiones = Volley.newRequestQueue(applicationContext)
        lectorImagenes = ImageLoader(colaPeticiones, null)*/

        val pref = PreferenceManager.getDefaultSharedPreferences(this)
        reproduceMusicaFondo = pref.getBoolean("musica_fondo", true)
        val numFragmentosString = pref.getString("num_fragmentos", "3")
        val numFragmentos = intent.getIntExtra("num_fragmentos", 3)
        val metodoAlmacenamiento = pref.getString("metodo_almacenamiento", "fichero_interno")
        val metodoAlmacenamientoFichero = pref.getString("metodo_almacenamiento_fichero", "fichero")
        val almacen = AlmacenPuntuacionesSQLiteRel(this)

        almacenPuntuaciones = when (metodoAlmacenamiento) {
            "array" -> AlmacenPuntuacionesList()
            "preferencias" -> AlmacenPuntuacionesPreferencias(this)
            "raw" -> AlmacenPuntuacionesRecursoRaw(this)
            "assets" -> AlmacenPuntuacionesRecursoAssets(this)
            "sax" -> AlmacenPuntuacionesXMLSAX(this)
            "memoria_externa" -> {
                when (metodoAlmacenamientoFichero) {
                    "fichero" -> AlmacenPuntuacionesFicheroExterno(this)
                    "carpeta" -> AlmacenPuntuacionesFicheroExtApl(this)
                    else -> AlmacenPuntuacionesFicheroExterno(this) // Por defecto
                }
            }
            "gson" -> AlmacenPuntuacionesGSon(this)
            "json" -> AlmacenPuntuacionesJSon(this)
            "sqlite" -> AlmacenPuntuacionesSQLite(this)
            "sqliterel" -> AlmacenPuntuacionesSQLiteRel(this)
            "socket" -> AlmacenPuntuacionesSocket()
            "swphp" -> AlmacenPuntuacionesSW_PHP()
            "swphpasync" ->AlmacenPuntuacionesSW_PHP_AsyncTask(this)
            else -> AlmacenPuntuacionesFicheroInterno(this)
        }

        mp = if (reproduceMusicaFondo) {
            startService(Intent(this, ServicioMusica::class.java))

            MediaPlayer.create(this, R.raw.audio)
        } else {
            MediaPlayer()
        }

        if (savedInstanceState != null) {
            musicPosition = savedInstanceState.getInt("musicPosition")
            mp.seekTo(musicPosition)
        } else {
            mp.start()
        }

        val button02: Button? = findViewById(R.id.Button02)
        val button03: Button? = findViewById(R.id.Button03)
        val button04: Button? = findViewById(R.id.Button04)
        val buttonJugar: Button? = findViewById(R.id.Button01)

        button02?.setOnClickListener {
            lanzarPreferencias()
        }

        button04?.setOnClickListener {
            lanzarPuntuaciones()
        }

        buttonJugar?.setOnClickListener {
            lanzarJuego()
        }

        val tituloTextView: TextView? = findViewById(R.id.tituloTextView)

        tituloTextView?.let {
            val animacionTitulo = AnimationUtils.loadAnimation(this, R.anim.giro_con_zoom)
            it.startAnimation(animacionTitulo)
        }

        buttonJugar?.let {
            val animacionBotonJugar = AnimationUtils.loadAnimation(this, R.anim.giro_con_zoom)
            it.startAnimation(animacionBotonJugar)

            animacionBotonJugar.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation?) {}
                override fun onAnimationRepeat(animation: Animation?) {}

                override fun onAnimationEnd(animation: Animation?) {
                    val animacionBotonConfigurar = AnimationUtils.loadAnimation(
                        applicationContext,
                        R.anim.desplazamiento_derecha
                    )
                    button02?.startAnimation(animacionBotonConfigurar)
                }
            })
        }

        button03?.setOnClickListener {
            val animacionBotonAcercaDe = AnimationUtils.loadAnimation(this, R.anim.giro_con_zoom)
            it.startAnimation(animacionBotonAcercaDe)
            lanzarAcercaDe()
        }
        Toast.makeText(this, "onCreate", Toast.LENGTH_SHORT).show()
    }

    override fun onStart() {
        super.onStart()
        Toast.makeText(this, "onStart", Toast.LENGTH_SHORT).show()
    }

    override fun onResume() {
        super.onResume()
        Toast.makeText(this, "onResume", Toast.LENGTH_SHORT).show()

        val pref = PreferenceManager.getDefaultSharedPreferences(this)
        val metodoAlmacenamiento = pref.getString("metodo_almacenamiento", "fichero_interno")

        almacenPuntuaciones = when (metodoAlmacenamiento) {
            "array" -> AlmacenPuntuacionesList()
            "preferencias" -> AlmacenPuntuacionesPreferencias(this)
            else -> AlmacenPuntuacionesFicheroInterno(this)
        }

        val nuevaReproduceMusicaFondo = pref.getBoolean("musica_fondo", true)
        if (nuevaReproduceMusicaFondo != reproduceMusicaFondo) {
            reproduceMusicaFondo = nuevaReproduceMusicaFondo
            if (reproduceMusicaFondo) {
                mp = MediaPlayer.create(this, R.raw.audio)
                mp.start()
            } else {
                mp.stop()
                mp.release()
            }
        } else if (reproduceMusicaFondo && !mp.isPlaying) {
            mp.start()
        }
    }


    override fun onPause() {
        Toast.makeText(this, "onPause", Toast.LENGTH_SHORT).show()
        super.onPause()
        mp.pause()
    }

    override fun onStop() {
        Toast.makeText(this, "onStop", Toast.LENGTH_SHORT).show()
        super.onStop()
    }

    override fun onRestart() {
        super.onRestart()
        Toast.makeText(this, "onRestart", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        Toast.makeText(this, "onDestroy", Toast.LENGTH_SHORT).show()
        mp.release()
        super.onDestroy()
        stopService(Intent(this, ServicioMusica::class.java))
    }

    private fun lanzarPreferencias() {
        val intent = Intent(this, PreferenciasActivity::class.java)
        startActivity(intent)
    }

    private fun lanzarPuntuaciones() {
        val intent = Intent(this, Puntuaciones::class.java)
        startActivity(intent)
    }

    private fun lanzarJuego() {
        val pref = PreferenceManager.getDefaultSharedPreferences(this)
        val numFragmentosString = pref.getString("num_fragmentos", "3")

        // Verificar si el valor almacenado es una cadena numérica
        val numFragmentos = numFragmentosString?.toIntOrNull() ?: 3

        val i = Intent(this, Juego::class.java)
        i.putExtra("num_fragmentos", numFragmentos)
        startActivityForResult(i, REQUEST_CODE)
    }

    @Suppress("DEPRECATION")
    @Deprecated("Deprecated in java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            val puntuacion = data.getIntExtra("puntuacion", 0)
            val nombre = "Yo"
            almacenPuntuaciones.guardarPuntuacion(puntuacion, nombre, System.currentTimeMillis())
            lanzarPuntuaciones()

            val pref = PreferenceManager.getDefaultSharedPreferences(this)
            val nuevoNumFragmentosString = pref.getString("num_fragmentos", "3")

            val numFragmentos = nuevoNumFragmentosString?.toIntOrNull() ?: 3
            intent.putExtra("num_fragmentos", numFragmentos)
        }
    }

    fun mostrarPreferencias(view: View) {
        val pref: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val s = ("música: " + pref.getBoolean("musica", true)
                + ", gráficos: " + pref.getString("graficos", "?"))
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show()
    }

    fun lanzarAcercaDe(view: View? = null) {
        val i = Intent(this, AcercaDeActivity::class.java)
        startActivity(i)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.acercaDe -> {
                lanzarAcercaDe()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt("musicPosition", mp.currentPosition)
        super.onSaveInstanceState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        if (mp != null) {
            val pos = savedInstanceState.getInt("musicPosition")
            mp.seekTo(pos)
        }
    }
}
