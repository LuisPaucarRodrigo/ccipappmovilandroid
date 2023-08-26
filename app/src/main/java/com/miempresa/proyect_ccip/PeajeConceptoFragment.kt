package com.miempresa.proyect_ccip

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import kotlinx.android.synthetic.main.fragment_peaje__concepto.*
import kotlinx.android.synthetic.main.fragment_peaje__concepto.view.*
import kotlinx.android.synthetic.main.opciones_foto.view.*
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [PeajeConceptoFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class PeajeConceptoFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private var intent:Intent? = null
    private var fotogaleriacamera:String? = null
    private var uri:Uri? = null
    private lateinit var file: File
    private var zona:String = ""
    private var dateFactura:Date? = null
    private val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.getDefault())
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
        val view:View = inflater.inflate(R.layout.fragment_peaje__concepto, container, false)
        val sp: SharedPreferences = view.context.getSharedPreferences("my_prefs", Context.MODE_PRIVATE)
        if (arguments != null){
            zona = requireArguments().getString("zona").toString()
        }
        view.dateEditTextPeaje.setOnClickListener {
            showDatePickerDialog(requireContext()) { selectedDate ->
                val formattedDate = dateFormatter.format(selectedDate.time)
                dateEditTextPeaje.setText(formattedDate)
                dateFactura = selectedDate
            }
        }

        view.btnPeajeFotoFactura.setOnClickListener{
            val builder = AlertDialog.Builder(this.requireActivity())
            val viewfoto = layoutInflater.inflate(R.layout.opciones_foto,null)

            builder.setView(viewfoto)

            val dialog = builder.create()
            dialog.show()

            viewfoto.btn_galeria.setOnClickListener{
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                    when (PackageManager.PERMISSION_GRANTED) {
                        ContextCompat.checkSelfPermission(
                            viewfoto.context,
                            Manifest.permission.READ_EXTERNAL_STORAGE
                        ) -> {
                            pickPhotoFromGallery()
                        }
                        else -> requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                    }
                }else{
                    pickPhotoFromGallery()
                }
                dialog.hide()
            }

            viewfoto.btn_camara.setOnClickListener{
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                    intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE_SECURE).also {
                        view.context?.let { it1 ->
                            it.resolveActivity(it1.packageManager).also { _ ->
                                createPhotoFile()
                                val photoUri: Uri =
                                    FileProvider.getUriForFile(
                                        requireView().context,
                                        BuildConfig.APPLICATION_ID + ".fileprovider", file
                                    )
                                it.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
                            }
                        }
                    }
                    when (PackageManager.PERMISSION_GRANTED) {
                        ContextCompat.checkSelfPermission(
                            viewfoto.context,
                            Manifest.permission.CAMERA
                        ) -> {
                            startForResult.launch(intent)
                        }
                        else -> {
                            requestPermissionLaunchercamera.launch(Manifest.permission.CAMERA)
                        }
                    }
                }else{
                    startForResult.launch(intent)
                }
                dialog.hide()
            }
        }
        view.btnPeajeEnviar.setOnClickListener{
            hideKeyboard()
            val ruc = view.findViewById<EditText>(R.id.txtPeajeNumeroRuc).text.toString()
            val factura = view.findViewById<EditText>(R.id.txtPeajeNumeroFactura).text.toString()
            val llegada = view.findViewById<EditText>(R.id.txtPeajeLugarLlegada).text.toString()
            val montopeaje = view.findViewById<EditText>(R.id.txtPeajeMontoTotal).text.toString()
            if (ruc.length == 11) {
                // El contenido tiene exactamente 11 dígitos
                enviarPeaje(factura,llegada,montopeaje,ruc,sp)
            } else {
                // El contenido no tiene 8 dígitos
                Toast.makeText(context, "Ruc debe tener 11 digitos", Toast.LENGTH_LONG).show()
            }
        }

        return view
    }

    private fun enviarPeaje(
        factura: String,
        llegada: String,
        montopeaje: String,
        ruc:String,
        sp: SharedPreferences
    ) {
        if (dateFactura != null && factura.isNotBlank() && llegada.isNotBlank() && montopeaje.isNotBlank() && ruc.isNotBlank()
            && fotogaleriacamera != null && zona.isNotEmpty()){
            btnPeajeEnviar.isEnabled = false
            val queue = Volley.newRequestQueue(context)
            val url = getString(R.string.urlAPI)+"/api/peaje"
            val json = JSONObject().apply {
                put("usuario_id", sp.getString("id", ""))
                put("ruc",ruc)
                put("nro_factura", factura)
                put("fecha_factura", dateFormat.format(dateFactura))
                put("lugar_llegada", llegada)
                put("monto_total", montopeaje)
                put("cuadrilla", zona)
                put("foto_factura", fotogaleriacamera)
                put("token", sp.getString("token", ""))
                put("fecha_insercion", dateFormat.format(Date()))
            }
            val stringRequest = JsonObjectRequest(
                Request.Method.POST,url,json,
                {response ->
                    if (response.getString("response") == "1" ){
//                        envioCorrecto(requireContext(),this)
                        val transaccion: FragmentTransaction = requireActivity().supportFragmentManager.beginTransaction()
                        transaccion.replace(R.id.contenedor,EnvioCorrectoFragment())
                        transaccion.commit()
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

    private fun pickPhotoFromGallery() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        startForActivityGallery.launch(intent)
    }

    private val startForActivityGallery = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ){result ->
        if (result.resultCode == Activity.RESULT_OK){
            uri = result.data?.data
            val bitmap = MediaStore.Images.Media.getBitmap(context?.contentResolver, Uri.parse(uri.toString()))
            fotogaleriacamera = encodeImage(bitmap)
            btnPeajeFotoFactura.text = getString(R.string.imagenSubida)
        }
    }


    private fun createPhotoFile() {
        val dir = view?.context?.getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!
        file = File.createTempFile("IMG_${System.currentTimeMillis()}_", ".png",dir)
    }

    private val startForResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ){result ->
        if (result.resultCode == Activity.RESULT_OK){
            fotogaleriacamera = encodeImage(getBitmap())
            btnPeajeFotoFactura.text = getString(R.string.imagenSubida)
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ){isGranted ->
        if (isGranted){
            pickPhotoFromGallery()
        }else{
            Toast.makeText(context,"Necesita habilitar los permisos", Toast.LENGTH_LONG).show()
        }
    }

    private fun encodeImage(bm: Bitmap): String? {
        val baos = ByteArrayOutputStream()
        bm.compress(Bitmap.CompressFormat.JPEG, 10, baos)
        val b = baos.toByteArray()
        return Base64.encodeToString(b, Base64.DEFAULT)
    }

    private fun getBitmap(): Bitmap = BitmapFactory.decodeFile(file.toString())

    private val requestPermissionLaunchercamera = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ){isGranted ->
        if (isGranted){
            startForResult.launch(intent)
        }else{
            Toast.makeText(context,"Necesita habilitar los permisos", Toast.LENGTH_LONG).show()
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment Peaje_ConceptoFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            PeajeConceptoFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}