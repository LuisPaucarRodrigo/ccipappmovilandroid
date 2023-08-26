package com.miempresa.proyect_ccip

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.FragmentTransaction
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import kotlinx.android.synthetic.main.fragment_traslado__concepto.*
import kotlinx.android.synthetic.main.fragment_traslado__concepto.view.*
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [TrasladoConceptoFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class TrasladoConceptoFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private var zona:String = ""
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.getDefault())
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

        val view:View = inflater.inflate(R.layout.fragment_traslado__concepto, container, false)
        val sp: SharedPreferences = view.context.getSharedPreferences("my_prefs", Context.MODE_PRIVATE)
        if (arguments != null){
            zona = requireArguments().getString("zona").toString()
        }
        val spinnerIncidencias = view.findViewById<Spinner>(R.id.spinnerTrasladoNroIncCrq)
        val incidencias = arrayOf("Incidencias","Mantenimiento","Corte Programado")
        spinnerIncidencias.adapter = ArrayAdapter(
            view.context,R.layout.styles_spinner_otros,incidencias
        )
        view.btn_traslado_Enviar.setOnClickListener{
                val sitioAtendido = view.findViewById<EditText>(R.id.txt_traslado_sitio_atendido).text.toString()
                val comentarios = view.findViewById<EditText>(R.id.txt_traslado_comentarios).text.toString()
                val nroIncaOCrq = spinnerTrasladoNroIncCrq.selectedItem.toString()
                val nroOper = view.findViewById<EditText>(R.id.txt_traslado_operacion).text.toString()
                hideKeyboard()
                traslado(sitioAtendido,comentarios,nroIncaOCrq,nroOper,sp)
            }
        return view
    }

    private fun traslado(
        sitio_atendido: String,
        comentarios: String,
        nro_inca_o_crq: String,
        nroOper:String,
        sp: SharedPreferences
    ) {
        if (sitio_atendido.isNotBlank() && comentarios.isNotBlank() && nro_inca_o_crq.isNotBlank()
            && nroOper.isNotBlank() && zona.isNotEmpty()){
            btn_traslado_Enviar.isEnabled = false
            val queue = Volley.newRequestQueue(context)
            val url = getString(R.string.urlAPI)+"/api/traslado"
            val json = JSONObject().apply {
                put("usuario_id",sp.getString("id",""))
                put("cuadrilla",zona)
                put("sitio_atendido",sitio_atendido)
                put("comentarios",comentarios)
                put("Oper_Inc_Crq",nro_inca_o_crq)
                put("Nro_Oper",nroOper)
                put("token",sp.getString("token",""))
                put("fecha_insercion",dateFormat.format(Date()))
            }
            val stringRequest = JsonObjectRequest(
                Request.Method.POST,url,json,
                {response->
                    if (response.getString("response") == "1"){

                    }else if (response.getString("response") == "2" ){
                        Toast.makeText(context, response.getString("message"), Toast.LENGTH_LONG).show()
                    }else{
                        with(sp.edit()){
                            putString("id","")
                            putString("token","")
                            putString("active","false")
                            apply()
                        }
                        Toast.makeText(context, "Ingrese Nuevamente", Toast.LENGTH_LONG).show()
                        val authActivity = Intent(context,AuthActivity::class.java)
                        startActivity(authActivity)
                    }

                },{
                    Toast.makeText(context, "Compruebe su Conexion a Intenet", Toast.LENGTH_LONG).show()
                })
            queue.add(stringRequest)
        }else{
            Toast.makeText(context, "Complete todos los campos", Toast.LENGTH_LONG).show()
        }
    }


    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment Traslado_ConceptoFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            TrasladoConceptoFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}