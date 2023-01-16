package com.miempresa.proyect_ccip

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentTransaction
import kotlinx.android.synthetic.main.fragment_cuadrilla__cicsa.view.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [Cuadrilla_CicsaFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class Cuadrilla_CicsaFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        val view:View = inflater.inflate(R.layout.fragment_cuadrilla__cicsa, container, false)

        view.btnArequipa.setOnClickListener(){
            conceptos("Arequipa")
        }
        view.btnMoquegua.setOnClickListener{
            conceptos("Moquegua")
        }
        view.btnChala.setOnClickListener{
            conceptos("Chala")
        }
        view.btnMDD1.setOnClickListener{
            conceptos("MDD1")
        }
        view.btnMDD2.setOnClickListener{
            conceptos("MDD2")
        }
        return view
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment Cuadrilla_CicsaFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            Cuadrilla_CicsaFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    private fun conceptos(zona:String) {
        val ConceptoFragment = ConceptoFragment()
        val args = Bundle()
        args.putString("zona",zona)
        ConceptoFragment.arguments = args
        val transaccion: FragmentTransaction = requireActivity().supportFragmentManager.beginTransaction()
        transaccion.replace(R.id.contenedor,ConceptoFragment)
            .addToBackStack(null)
        transaccion.commit()
    }
}