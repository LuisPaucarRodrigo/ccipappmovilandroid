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
import android.widget.EditText
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

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [Traslado_ConceptoFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class Traslado_ConceptoFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private var zona:String = ""
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
        view.btn_traslado_Enviar.setOnClickListener{
                val sitio_atendido = view.findViewById<EditText>(R.id.txt_traslado_sitio_atendido).text.toString()
                val comentarios = view.findViewById<EditText>(R.id.txt_traslado_comentarios).text.toString()
                val nro_inca_o_crq = view.findViewById<EditText>(R.id.txt_traslado_nro_inc_o_crq).text.toString()
                hideKeyboard()
                traslado(sitio_atendido,comentarios,nro_inca_o_crq,sp)
            }
        return view
    }

    private fun traslado(
        sitio_atendido: String,
        comentarios: String,
        nro_inca_o_crq: String,
        sp: SharedPreferences
    ) {
        if (sitio_atendido.isNotEmpty() && comentarios.isNotEmpty() && nro_inca_o_crq.isNotEmpty()
            && zona.isNotEmpty()){
            btn_traslado_Enviar.isEnabled = false
            val queue = Volley.newRequestQueue(context)
            val url = getString(R.string.urlAPI)+"/api/traslado"
            val json = JSONObject()
            json.put("usuario_id",sp.getString("id",""))
            json.put("cuadrilla",zona)
            json.put("site_atendido",sitio_atendido)
            json.put("comentarios",comentarios)
            json.put("Nro_Inc_Crq",nro_inca_o_crq)
            json.put("token",sp.getString("token",""))
            json.put("fecha_traslado",SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(Date()))
            val stringRequest = JsonObjectRequest(
                Request.Method.POST,url,json,
                {response->
                    if (response.getString("response") == "1"){
                        val transaccion: FragmentTransaction = requireActivity().supportFragmentManager.beginTransaction()
                        transaccion.replace(R.id.contenedor,EnvioCorrectoFragment())
                        transaccion.commit()
                    }else{
                        with(sp.edit()){
                            putString("id","")
                            putString("token","")
                            putString("active","false")
                            apply()
                        }
                        Toast.makeText(context, "Ingrese Nuevamente", Toast.LENGTH_LONG).show()
                        val AuthActivity = Intent(context,AuthActivity::class.java)
                        startActivity(AuthActivity)
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
            Traslado_ConceptoFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}