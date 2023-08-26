package com.miempresa.proyect_ccip

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_concepto.view.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ConceptoFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ConceptoFragment : Fragment() {
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
        val view:View = inflater.inflate(R.layout.fragment_concepto, container, false)
        if (arguments != null){
            zona = requireArguments().getString("zona").toString()
        }
            view.btnCombustible.setOnClickListener{
                opciones(CombustibleConceptoFragment())
                //combustible()
            }
            view.btnPeaje.setOnClickListener{
                opciones(PeajeConceptoFragment())
                //peaje()
            }
            view.btnTraslado.setOnClickListener{
                opciones(TrasladoConceptoFragment())
                //traslado()
            }
            view.btnOtros.setOnClickListener {
                opciones(OtrosConceptoFragment())
                //otros()
            }
            view.btnCombustibleGEP.setOnClickListener {
                opciones(CombustibleGPEConceptoFragment())
                //combustible()
            }
        return view
    }
    private fun opciones(op:Fragment) {

        val transaccion = requireActivity().supportFragmentManager.beginTransaction()
        val args = Bundle()
        args.putString("zona",zona)
        op.arguments = args
        transaccion.replace(R.id.contenedor, op)
            .addToBackStack(null)
        transaccion.commit()
    }
//    private fun combustible() {
//        val Combustible_ConceptoFragment = Combustible_ConceptoFragment()
//        val args= Bundle()
//        args.putString("id",id)
//        Combustible_ConceptoFragment.arguments = args
//        val transaccion: FragmentTransaction = requireActivity().supportFragmentManager.beginTransaction()
//        transaccion.replace(R.id.contenedor,Combustible_ConceptoFragment)
//            .addToBackStack(null)
//        transaccion.commit()
//    }
//
//    private fun peaje() {
//        val Peaje_ConceptoFragment = Peaje_ConceptoFragment()
//        val args= Bundle()
//        args.putString("id",id)
//        Peaje_ConceptoFragment.arguments = args
//        val transaccion: FragmentTransaction = requireActivity().supportFragmentManager.beginTransaction()
//        transaccion.replace(R.id.contenedor,Peaje_ConceptoFragment)
//            .addToBackStack(null)
//        transaccion.commit()
//    }
//
//    private fun traslado() {
//        val Traslado_ConceptoFragment = Traslado_ConceptoFragment()
//        val args= Bundle()
//        args.putString("id",id)
//        Traslado_ConceptoFragment.arguments = args
//        val transaccion: FragmentTransaction = requireActivity().supportFragmentManager.beginTransaction()
//        transaccion.replace(R.id.contenedor,Traslado_ConceptoFragment)
//            .addToBackStack(null)
//        transaccion.commit()
//    }
//
//    private fun otros() {
//        val Otros_ConceptoFragment = Otros_ConceptoFragment()
//        val args= Bundle()
//        args.putString("id",id)
//        Otros_ConceptoFragment.arguments = args
//        val transaccion: FragmentTransaction = requireActivity().supportFragmentManager.beginTransaction()
//        transaccion.replace(R.id.contenedor,Otros_ConceptoFragment)
//            .addToBackStack(null)
//        transaccion.commit()
//    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ConceptoFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ConceptoFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}