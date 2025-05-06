package com.example.asteroides

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.toolbox.ImageLoader
import com.android.volley.toolbox.NetworkImageView

class MiAdaptador(private val context: Context, private val lista: List<String>,
                  private val onClick: (View) -> Unit) :
    RecyclerView.Adapter<MiAdaptador.ViewHolder>() {

    private val inflador: LayoutInflater = LayoutInflater.from(context)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v: View = inflador.inflate(R.layout.elemento_lista, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.titulo.text = lista[position]
        when ((Math.random() * 2).toInt()) {
       0 -> holder.icon.setImageResource(R.drawable.asteroid1)
       1 -> holder.icon.setImageResource(R.drawable.asteroid2)
       else -> holder.icon.setImageResource(R.drawable.asteroid3)
       }
        /*MainActivity.lectorImagenes.get(
            "http://mmoviles.upv.es/img/moviles.png",
            ImageLoader.getImageListener(holder.icon, R.drawable.asteroid1, R.drawable.asteroid3)
        )*/
        /*holder.icon.setImageUrl("http://mmoviles.upv.es/img/moviles.png",
            MainActivity.lectorImagenes)*/
        holder.itemView.setOnClickListener { onClick(holder.itemView) }
    }

    override fun getItemCount(): Int {
        return lista.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titulo: TextView = itemView.findViewById(R.id.titulo)
        val subtitulo: TextView = itemView.findViewById(R.id.subtitulo)
        val icon: ImageView = itemView.findViewById(R.id.icono)
    }
}
