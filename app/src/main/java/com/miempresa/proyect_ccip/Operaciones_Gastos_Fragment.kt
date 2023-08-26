package com.miempresa.proyect_ccip

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.os.AsyncTask
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject
import java.util.*


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [Operaciones_Gastos_Fragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class
Operaciones_Gastos_Fragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private val llenarLista = ArrayList<Elementos>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    @SuppressLint("SuspiciousIndentation")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        val view:View = inflater.inflate(R.layout.fragment_operaciones__gastos_, container, false)
        val sp: SharedPreferences = view.context.getSharedPreferences("my_prefs", Context.MODE_PRIVATE)
        val saldouser = view.findViewById<TextView>(R.id.txtsaldouser)
        val movimientos = view.findViewById<RecyclerView>(R.id.movimientos)
        cargarLista(movimientos,sp,saldouser)
//        saldouser.text = sp.getString("saldo","")
        return view
    }
    @SuppressLint("SetTextI18n", "SuspiciousIndentation")
    private fun cargarLista(movimientos: RecyclerView, sp: SharedPreferences, saldouser: TextView){

        val url = getString(R.string.urlAPI) + "/api/saldo"
            AsyncTask.execute {
            movimientos.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
            movimientos.layoutManager = LinearLayoutManager(context)

            val queue = Volley.newRequestQueue(context)
            val json = JSONObject()
                json.put("token",sp.getString("token",""))
                json.put("id",sp.getString("id",""))
                val stringRequest = JsonObjectRequest(
                Request.Method.POST,url,json,
                { response ->
                    if ( response.getString("response") == "1"){
                        saldouser.text = "S/"+response.getString("saldo")
                        val operaciones = response.getJSONArray("operacion")
                        for (i in 0 until operaciones.length()) {
                            val concepto =
                                operaciones.getJSONObject(i).getString("concepto").toString()
                            val fechaoperacion =
                                operaciones.getJSONObject(i).getString("fecha_insercion").toString()
                            val gastos =
                                operaciones.getJSONObject(i).getString("gasto").toString()
                            llenarLista.add(Elementos(concepto, fechaoperacion, "-$gastos"))
                        }
                        val adapter = AdaptadorElementos(llenarLista)
                        movimientos.adapter = adapter
                    }else{
                        Toast.makeText(
                            context,
                            "Error al obtener los datos",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }, {
                    Toast.makeText(
                        context,
                        "Compruebe su Conexion a Intenet",
                        Toast.LENGTH_LONG
                    ).show()
                })
            queue.add(stringRequest)
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment Operaciones_Gastos_Fragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            Operaciones_Gastos_Fragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}