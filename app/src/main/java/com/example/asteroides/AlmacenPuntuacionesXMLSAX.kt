package com.example.asteroides

import android.content.Context
import android.util.Log
import android.util.Xml
import org.xml.sax.Attributes
import org.xml.sax.InputSource
import org.xml.sax.SAXException
import org.xml.sax.helpers.DefaultHandler
import org.xmlpull.v1.XmlSerializer
import java.io.*
import java.util.ArrayList
import java.util.Vector
import javax.xml.parsers.SAXParserFactory

class AlmacenPuntuacionesXMLSAX(private val contexto: Context) : AlmacenPuntuaciones {
    private val FICHERO = "puntuaciones.xml"
    private val lista: ListaPuntuaciones = ListaPuntuaciones()
    private var cargadaLista = false

    init {
        cargadaLista = false
    }

    override fun guardarPuntuacion(puntos: Int, nombre: String, fecha: Long) {
        try {
            // Crear el directorio si no existe
            val dir = File(contexto.filesDir, "")
            if (!dir.exists()) {
                dir.mkdirs()
            }

            // Abre el archivo "puntuaciones.xml" en modo privado
            val outputStream = contexto.openFileOutput(FICHERO, Context.MODE_PRIVATE)
            lista.escribirXML(outputStream)
            outputStream.close()
        } catch (e: Exception) {
            Log.e("Asteroides", "Error al guardar la puntuación: ${e.message}")
            e.printStackTrace()
        }
    }

    override fun listaPuntuaciones(cantidad: Int): Vector<String> {
        try {
            if (!cargadaLista) {
                lista.leerXML(contexto.openFileInput(FICHERO))
            }
        } catch (e: Exception) {
            Log.e("Asteroides", e.message, e)
        }
        return lista.aVectorString()
    }

    private inner class ListaPuntuaciones {
        private inner class Puntuacion {
            var puntos: Int = 0
            var nombre: String = ""
            var fecha: Long = 0
        }

        private var listaPuntuaciones: MutableList<Puntuacion> = ArrayList()

        fun nuevo(puntos: Int, nombre: String, fecha: Long) {
            val puntuacion = Puntuacion()
            puntuacion.puntos = puntos
            puntuacion.nombre = nombre
            puntuacion.fecha = fecha
            listaPuntuaciones.add(puntuacion)
        }

        fun aVectorString(): Vector<String> {
            val result = Vector<String>()
            for (puntuacion in listaPuntuaciones) {
                result.add("${puntuacion.nombre} ${puntuacion.puntos}")
            }
            return result
        }

        @Throws(Exception::class)
        fun leerXML(entrada: InputStream) {
            val fabrica = SAXParserFactory.newInstance()
            val parser = fabrica.newSAXParser()
            val lector = parser.getXMLReader()
            val manejadorXML = ManejadorXML()
            lector.contentHandler = manejadorXML
            lector.parse(InputSource(entrada))
            cargadaLista = true
        }

        fun escribirXML(salida: OutputStream) {
            val serializador: XmlSerializer = Xml.newSerializer()
            try {
                serializador.setOutput(salida, "UTF-8")
                serializador.startDocument("UTF-8", true)
                serializador.startTag("", "lista_puntuaciones")
                for (puntuacion in listaPuntuaciones) {
                    serializador.startTag("", "puntuacion")
                    serializador.attribute("", "fecha", puntuacion.fecha.toString())
                    serializador.startTag("", "nombre")
                    serializador.text(puntuacion.nombre)
                    serializador.endTag("", "nombre")
                    serializador.startTag("", "puntos")
                    serializador.text(puntuacion.puntos.toString())
                    serializador.endTag("", "puntos")
                    serializador.endTag("", "puntuacion")
                }
                serializador.endTag("", "lista_puntuaciones")
                serializador.endDocument()
            } catch (e: Exception) {
                Log.e("Asteroides", e.message, e)
            }
        }

        private inner class ManejadorXML : DefaultHandler() {
            private var cadena: StringBuilder = StringBuilder()
            private var puntuacion: Puntuacion? = null

            @Throws(SAXException::class)
            override fun startDocument() {
                listaPuntuaciones = ArrayList()
                cadena = StringBuilder()
            }

            @Throws(SAXException::class)
            override fun startElement(uri: String?, nombreLocal: String?, nombreCualif: String?, atr: Attributes?) {
                cadena.setLength(0)
                if (nombreLocal.equals("puntuacion")) {
                    puntuacion = Puntuacion()
                    puntuacion!!.fecha = atr!!.getValue("fecha").toLong()
                }
            }

            override fun characters(ch: CharArray?, comienzo: Int, lon: Int) {
                cadena.append(ch, comienzo, lon)
            }

            @Throws(SAXException::class)
            override fun endElement(uri: String?, nombreLocal: String?, nombreCualif: String?) {
                if (nombreLocal.equals("puntos")) {
                    puntuacion!!.puntos = cadena.toString().toInt()
                } else if (nombreLocal.equals("nombre")) {
                    puntuacion!!.nombre = cadena.toString()
                } else if (nombreLocal.equals("puntuacion")) {
                    listaPuntuaciones.add(puntuacion!!)
                }
            }

            @Throws(SAXException::class)
            override fun endDocument() {}
        }
    }
}
