package com.miempresa.proyect_ccip
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class AdaptadorElementos (var ListaElementos:ArrayList<Elementos>): RecyclerView.Adapter<AdaptadorElementos.ViewHolder>(){

    class ViewHolder (itemView : View): RecyclerView.ViewHolder(itemView) {
        val felemento_concepto = itemView.findViewById<TextView>(R.id.elemento_concepto)
        val felemento_fecha = itemView.findViewById<TextView>(R.id.elemento_fecha)
        val felemento_gasto = itemView.findViewById<TextView>(R.id.elemento_gasto)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.elementos_movimientos, parent, false)
        return ViewHolder(v)
    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.felemento_concepto?.text= ListaElementos[position].concepto
        holder.felemento_fecha?.text= ListaElementos[position].fecha_operacion
        holder.felemento_gasto?.text= "S/"+ListaElementos[position].gastos.toDouble()
    }
    override fun getItemCount(): Int {
        return ListaElementos.size
    }
}