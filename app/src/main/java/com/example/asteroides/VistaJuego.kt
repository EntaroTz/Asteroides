package com.example.asteroides

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.drawable.Drawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.PathShape
import android.graphics.drawable.shapes.RectShape
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.SoundPool
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.WindowInsets
import androidx.core.content.ContextCompat

class VistaJuego(context: Context, attrs: AttributeSet) : View(context, attrs),
    SensorEventListener {
    private val asteroides: MutableList<Grafico> = mutableListOf()
    private val numAsteroides = 5
    private val numFragmentos = 3
    // NAVE
    private var nave: Grafico? = null
    private var giroNave = 0
    private var aceleracionNave = 0f

    private val PASO_GIRO_NAVE = 5
    private val PASO_ACELERACION_NAVE = 0.5f

    private var anchoView: Int = 0
    private var altoView: Int = 0
    private val drawableAsteroide = arrayOfNulls<Drawable>(3)

    private val PERIODO_PROCESO = 50
    private var ultimoProceso: Long = 0

    private var mX = 0f
    private var mY = 0f
    private var disparo = false

    private var hayValorInicial = false
    private var valorInicial = 0f

    private lateinit var sensorManager: SensorManager
    private lateinit var sensor: Sensor

    // MISIL
    private lateinit var misil: Grafico
    private val PASO_VELOCIDAD_MISIL = 12
    private var misilActivo = false
    private var tiempoMisil: Int = 0
    private lateinit var drawableMisil: Drawable

    private val misiles: MutableList<Grafico> = mutableListOf()

    private lateinit var vistaJuego: VistaJuego
    internal val thread: ThreadJuego = ThreadJuego()

    //Sonido
    private lateinit var soundPool: SoundPool
    private var idDisparo: Int = 0
    private var idExplosion: Int = 0

    private var reproduceEfectosSonido: Boolean = true

    private var puntuacion = 0
    private var padre: Activity? = null

    private fun salir() {
        val bundle = Bundle()
        bundle.putInt("puntuacion", puntuacion)
        val intent = Intent()
        intent.putExtras(bundle)
        padre?.setResult(Activity.RESULT_OK, intent)
        padre?.finish()
    }


    init {
        val pref = PreferenceManager.getDefaultSharedPreferences(context)
        val graficos = pref.getString("graficos", "1")
        val numFragmentosString = pref.getString("num_fragmentos", "3")
        val numFragmentos = numFragmentosString?.toInt() ?: 3

        // Inicializar asteroides
        if (graficos == "0") {
            val pathAsteroide = Path().apply {
                moveTo(0.3f, 0.0f)
                lineTo(0.6f, 0.0f)
                lineTo(0.6f, 0.3f)
                lineTo(0.8f, 0.2f)
                lineTo(1.0f, 0.4f)
                lineTo(0.8f, 0.6f)
                lineTo(0.9f, 0.9f)
                lineTo(0.8f, 1.0f)
                lineTo(0.4f, 1.0f)
                lineTo(0.0f, 0.6f)
                lineTo(0.0f, 0.2f)
                lineTo(0.3f, 0.0f)
            }
            for (i in 0 until 3) {
                val dAsteroide = ShapeDrawable(PathShape(pathAsteroide, 1f, 1f)).apply {
                    paint.color = Color.WHITE
                    paint.style = Paint.Style.STROKE
                    intrinsicWidth = 50 - i * 14
                    intrinsicHeight = 50 - i * 14
                }
                drawableAsteroide[i] = dAsteroide
            }
            setBackgroundColor(Color.BLACK)
        } else {
            drawableAsteroide[0] = ContextCompat.getDrawable(context, R.drawable.asteroid1)!!
            drawableAsteroide[1] = ContextCompat.getDrawable(context, R.drawable.asteroid2)!!
            drawableAsteroide[2] = ContextCompat.getDrawable(context, R.drawable.asteroid3)!!
        }

        // Inicializar misil
        val dMisil = ShapeDrawable(RectShape()).apply {
            paint.color = Color.WHITE
            paint.style = Paint.Style.STROKE
            intrinsicWidth = 15
            intrinsicHeight = 3
        }
        drawableMisil = dMisil

        // Inicializar otros componentes
        val drawableNave: Drawable = ContextCompat.getDrawable(context, R.drawable.spaceship)!!
        nave = Grafico(this, drawableNave)

        val scale = resources.displayMetrics.density
        val altoAsteroide = (50 * scale + 0.5f).toInt()
        val anchoAsteroide = (50 * scale + 0.5f).toInt()

        nave?.alto = altoAsteroide
        nave?.ancho = anchoAsteroide

        sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) ?: error("Sensor not found")

        soundPool = SoundPool(5, AudioManager.STREAM_MUSIC, 0)
        idDisparo = soundPool.load(context, R.raw.disparo, 0)
        idExplosion = soundPool.load(context, R.raw.explosion, 0)

        // Obtener preferencias de efectos de sonido
        reproduceEfectosSonido = pref.getBoolean("efectos_sonido", true)
    }

    override fun onSizeChanged(ancho: Int, alto: Int, ancho_anter: Int, alto_anter: Int) {
        super.onSizeChanged(ancho, alto, ancho_anter, alto_anter)

        // Inicializar la vista
        anchoView = ancho
        altoView = alto
        inicializarAsteroides()
        nave?.posX = (ancho / 2 - nave!!.ancho / 2).toFloat()
        nave?.posY = (alto / 2 - nave!!.alto / 2).toFloat()
        ultimoProceso = System.currentTimeMillis()

        val pref = PreferenceManager.getDefaultSharedPreferences(context)
        val controlTeclado = pref.getBoolean("teclado", true)
        val controlPantallaTactil = pref.getBoolean("pantalla_tactil", true)
        val controlSensores = pref.getBoolean("sensores", true)

        if (!controlTeclado) {
            setOnKeyListener { _, _, _ -> true }
        }

        if (!controlPantallaTactil) {
            setOnTouchListener { _, _ -> true }
        }

        if (!controlSensores) {
            desactivarSensores()
        }

        // Crear y configurar el misil
        val drawableMisilBitmap: Drawable = ContextCompat.getDrawable(context, R.drawable.misil1)!!
        misil = Grafico(this, drawableMisilBitmap)
        misil.alto = 15 // Ajusta según el tamaño de tu imagen de misil
        misil.ancho = 15 // Ajusta según el tamaño de tu imagen de misil

        thread.start()
    }

    private fun inicializarAsteroides() {
        for (i in 0 until numAsteroides) {
            if (i < drawableAsteroide.size && drawableAsteroide[i] != null) {
                val asteroide = Grafico(this, drawableAsteroide[i]!!)
                asteroide.incY = (Math.random() * 4 - 2).toFloat()
                asteroide.incX = (Math.random() * 4 - 2).toFloat()
                asteroide.angulo = (Math.random() * 360).toFloat()
                asteroide.rotacion = (Math.random() * 8 - 4).toInt()

                do {
                    asteroide.posX = (Math.random() * (anchoView - asteroide.ancho)).toFloat()
                    asteroide.posY = (Math.random() * (altoView - asteroide.alto)).toFloat()
                } while (asteroide.distancia(nave!!) < (anchoView + altoView) / 5)

                asteroides.add(asteroide)
            } else {

            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Sincronizamos el acceso a la lista de asteroides
        synchronized(asteroides) {
            for (asteroide in asteroides) {
                asteroide.dibujaGrafico(canvas)
            }
            nave?.dibujaGrafico(canvas)
        }

        // Copiamos la lista de misiles para evitar la modificación concurrente
        val copiaMisiles = ArrayList(misiles)
        for (misil in copiaMisiles) { // Iteramos sobre la copia de la lista de misiles
            misil.dibujaGrafico(canvas)
        }
    }


    private fun destruyeAsteroide(i: Int) {
        val tamañoAsteroide = asteroides[i].getDrawable()?.let { drawableAsteroide.indexOf(it) } ?: -1

        if (tamañoAsteroide == 2) {
            asteroides.removeAt(i)
            return
        }

        val pref = PreferenceManager.getDefaultSharedPreferences(context)
        val numFragmentosString = pref.getString("num_fragmentos", "3")
        val numFragmentos = numFragmentosString?.toInt() ?: 3

        val nuevoTamaño = tamañoAsteroide + 1

        val posX = asteroides[i].posX
        val posY = asteroides[i].posY
        for (j in 0 until numFragmentos) {
            val fragmento = Grafico(this, drawableAsteroide[nuevoTamaño]!!)
            fragmento.posX = posX
            fragmento.posY = posY
            fragmento.incX = (Math.random() * 8 - 4).toFloat()
            fragmento.incY = (Math.random() * 8 - 4).toFloat()
            fragmento.angulo = (Math.random() * 360).toFloat()
            fragmento.rotacion = (Math.random() * 8 - 4).toInt()
            asteroides.add(fragmento)
        }

        asteroides.removeAt(i)

        puntuacion += 1000

        misilActivo = false

        if (reproduceEfectosSonido) {
            soundPool.play(idExplosion, 1f, 1f, 0, 0, 1f)
        }

        // Salimos del juego si no quedan asteroides
        if (asteroides.isEmpty()) {
            salir()
        }
    }


    private fun activaMisil() {
        nave?.let {
            val nuevoMisil = Grafico(this, drawableMisil)
            nuevoMisil.alto = 15
            nuevoMisil.ancho = 15
            nuevoMisil.posX = (it.posX + it.ancho / 2 - nuevoMisil.ancho / 2).toFloat()
            nuevoMisil.posY = (it.posY + it.alto / 2 - nuevoMisil.alto / 2).toFloat()
            nuevoMisil.angulo = it.angulo
            nuevoMisil.incX = (Math.cos(Math.toRadians(nuevoMisil.angulo.toDouble())) * PASO_VELOCIDAD_MISIL).toFloat()
            nuevoMisil.incY = (Math.sin(Math.toRadians(nuevoMisil.angulo.toDouble())) * PASO_VELOCIDAD_MISIL).toFloat()
            tiempoMisil = (Math.min(anchoView / Math.abs(nuevoMisil.incX), altoView / Math.abs(nuevoMisil.incY)) - 2).toInt()
            misiles.add(nuevoMisil)

            if (reproduceEfectosSonido) {
                soundPool.play(idDisparo, 1f, 1f, 1, 0, 1f)
            }
        }
    }

    fun actualizaFisica() {
        val ahora = System.currentTimeMillis()

        if (ahora <= ultimoProceso + PERIODO_PROCESO) {
            return
        }

        val retardo: Double
        synchronized(asteroides) {
            retardo = (ahora - ultimoProceso) / PERIODO_PROCESO.toDouble()
            ultimoProceso = ahora

            nave?.angulo = (nave?.angulo!! + giroNave * retardo).toFloat()
            val nIncX = (nave?.incX!! + aceleracionNave *
                    Math.cos(Math.toRadians(nave?.angulo!!.toDouble())) * retardo).toFloat()
            val nIncY = (nave?.incY!! + aceleracionNave *
                    Math.sin(Math.toRadians(nave?.angulo!!.toDouble())) * retardo).toFloat()

            val MAX_VELOCIDAD_NAVE = Grafico.MAX_VELOCIDAD
            if (Math.hypot(nIncX.toDouble(), nIncY.toDouble()) <= MAX_VELOCIDAD_NAVE) {
                nave?.incX = nIncX
                nave?.incY = nIncY
            }

            nave?.incrementaPos(retardo)
            for (asteroide in asteroides) {
                asteroide.incrementaPos(retardo)
            }

            // Actualizamos la posición de cada misil en la lista y detectamos colisiones
            for (misil in misiles.toList()) { // Usamos toList() para evitar ConcurrentModificationException
                misil.incrementaPos(retardo)
                tiempoMisil -= retardo.toInt()
                if (tiempoMisil < 0) {
                    misiles.remove(misil)
                } else {
                    var i = 0
                    while (i < asteroides.size) {
                        if (misil.verificaColision(asteroides[i])) {
                            destruyeAsteroide(i) // Llamada a destruyeAsteroide cuando hay colisión
                            misiles.remove(misil)
                            break
                        } else {
                            i++
                        }
                    }
                }
                for (asteroide in asteroides) {
                    val distancia = Math.hypot((asteroide.posX.toDouble() - nave!!.posX.toDouble()), (asteroide.posY.toDouble() - nave!!.posY.toDouble()))
                    if (distancia < (asteroide.ancho.toDouble() *1.2 / 2 + nave!!.ancho.toDouble() / 2)) {
                        salir()
                        break
                    }
                }
            }
        }
    }

    override fun onKeyUp(codigoTecla: Int, evento: KeyEvent): Boolean {
        super.onKeyUp(codigoTecla, evento)
        var procesada = true

        when (codigoTecla) {
            KeyEvent.KEYCODE_DPAD_UP -> aceleracionNave = 0f
            KeyEvent.KEYCODE_DPAD_LEFT,
            KeyEvent.KEYCODE_DPAD_RIGHT -> giroNave = 0
            else -> {
                procesada = false
            }
        }
        return procesada
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        super.onTouchEvent(event)

        val x = event.x
        val y = event.y

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                disparo = true
            }
            MotionEvent.ACTION_MOVE -> {
                val dx = Math.abs(x - mX)
                val dy = Math.abs(y - mY)

                if (dy < 10 && dx > 10) {
                    giroNave = Math.round((x - mX) / 10)
                    disparo = false
                } else if (dx < 6 && dy > 6) {
                    aceleracionNave = Math.round(((mY - y) / 25).toDouble()).toFloat()
                    disparo = false
                }
            }
            MotionEvent.ACTION_UP -> {
                giroNave = 0
                if (disparo) {
                    activaMisil()
                }
            }
        }
        mX = x
        mY = y
        return true
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            val gravityX = event.values[0]
            val gravityY = event.values[1]

            val inclinacionX = Math.atan2(gravityX.toDouble(), gravityY.toDouble()) * (180 / Math.PI)

            aceleracionNave = -(inclinacionX / 90).toFloat()

            giroNave = (inclinacionX / 90).toInt()

            val MAX_ACELERACION_NAVE = 2.0f
            if (aceleracionNave > MAX_ACELERACION_NAVE) {
                aceleracionNave = MAX_ACELERACION_NAVE
            } else if (aceleracionNave < -MAX_ACELERACION_NAVE) {
                aceleracionNave = -MAX_ACELERACION_NAVE
            }
        }
    }

    fun activarSensores() {
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_GAME)
    }

    fun desactivarSensores() {
        sensorManager.unregisterListener(this)
    }

    fun setPadre(padre: Activity) {
        this.padre = padre
    }

    inner class ThreadJuego : Thread() {
        private var pausa = false
        private var corriendo = false
        private val lock = Object()

        fun pausar() {
            synchronized(lock) {
                pausa = true
            }
        }

        fun reanudar() {
            synchronized(lock) {
                pausa = false
                lock.notify()
            }
        }

        fun detener() {
            corriendo = false
            if (pausa) reanudar()
        }

        override fun run() {
            corriendo = true
            while (corriendo) {
                actualizaFisica()
                synchronized(lock) {
                    while (pausa) {
                        try {
                            lock.wait()
                        } catch (e: InterruptedException) {
                            // Manejar la interrupción si es necesario
                        }
                    }
                }
            }
        }
    }
}
