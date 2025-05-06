package com.example.asteroides

import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.view.View
import kotlin.math.hypot

class Grafico(private val view: View, private val drawable: Drawable) {
    var posX: Float = 0.toFloat()
    var posY: Float = 0.toFloat()
    var incX: Float = 0.toFloat()
    var incY: Float = 0.toFloat()
    var angulo: Float = 0.toFloat()
    var rotacion: Int = 0
    var ancho: Int = 0
    var alto: Int = 0
    var radioColision: Int = 0

    companion object {
        const val MAX_VELOCIDAD = 20
    }

    init {
        ancho = drawable.intrinsicWidth
        alto = drawable.intrinsicHeight
        radioColision = (alto + ancho) / 4
    }

    fun dibujaGrafico(canvas: Canvas) {
        canvas.save()
        val x = (posX + ancho / 2).toInt()
        val y = (posY + alto / 2).toInt()
        canvas.rotate(angulo, x.toFloat(), y.toFloat())
        drawable.setBounds(posX.toInt(), posY.toInt(), posX.toInt() + ancho, posY.toInt() + alto)
        drawable.draw(canvas)
        canvas.restore()
        val rInval = hypot(ancho.toDouble(), alto.toDouble()) / 2 + MAX_VELOCIDAD
        view.invalidate((x - rInval).toInt(), (y - rInval).toInt(), (x + rInval).toInt(), (y + rInval).toInt())
    }

    fun incrementaPos(factor: Double) {
        posX += incX * factor.toFloat()
        if (posX < -ancho / 2) posX = (view.width - ancho / 2).toFloat()
        if (posX > view.width - ancho / 2) posX = (-ancho / 2).toFloat()
        posY += incY * factor.toFloat()
        if (posY < -alto / 2) posY = (view.height - alto / 2).toFloat()
        if (posY > view.height - alto / 2) posY = (-alto / 2).toFloat()
        angulo += rotacion * factor.toFloat() //Actualizamos ángulo
    }

    fun distancia(g: Grafico): Double {
        val posXDouble = posX.toDouble()
        val posYDouble = posY.toDouble()
        return hypot(posXDouble - g.posX, posYDouble - g.posY)
    }

    fun verificaColision(g: Grafico): Boolean {
        return distancia(g) < (radioColision + g.radioColision)
    }
    fun getDrawable(): Drawable? {
        return this.drawable
    }
}
