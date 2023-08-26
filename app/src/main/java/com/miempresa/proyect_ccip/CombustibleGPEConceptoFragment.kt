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
import android.os.*
import android.provider.MediaStore
import android.util.Base64
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.FragmentTransaction
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import kotlinx.android.synthetic.main.fragment_combustible_g_p_e_concepto.*
import kotlinx.android.synthetic.main.fragment_combustible_g_p_e_concepto.view.*
import kotlinx.android.synthetic.main.opciones_foto.view.*
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [CombustibleGPEConceptoFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class CombustibleGPEConceptoFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private var zona:String = ""
    private var dateFactura: Date? = null
    private var btnsubirimages:Button? = null
    private var intent:Intent? = null
    private var galeriacamerastringgep:String? = null
    private var galeriacamerastringgalonera:String? = null
    private var galeracamerastringfactura:String? = null
    private var uricombustiblegep:Uri? = null
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
        val view:View = inflater.inflate(R.layout.fragment_combustible_g_p_e_concepto, container, false)
        val sp: SharedPreferences = view.context.getSharedPreferences("my_prefs", Context.MODE_PRIVATE)

        if (arguments != null){
            zona = requireArguments().getString("zona").toString()
        }
        view.dateEditTextCombustibleGEP.setOnClickListener {
            showDatePickerDialog(requireContext()) { selectedDate ->
                val formattedDate = dateFormatter.format(selectedDate.time)
                dateEditTextCombustibleGEP.setText(formattedDate)
                dateFactura = selectedDate
            }
        }
        view.btnCombustiblegepFotoFactura.setOnClickListener{
            imagegallerycamera(btnCombustiblegepFotoFactura)
        }
        view.btnCombustiblegepFotoGalonera.setOnClickListener{
            imagegallerycamera(btnCombustiblegepFotoGalonera)
        }
        view.btnCombustibleGEPEnviar.setOnClickListener{
            hideKeyboard()
            val ruc = view.findViewById<EditText>(R.id.txtCombustibleGepNumeroRuc).text.toString()
            val nroFactura = view.findViewById<EditText>(R.id.txtCombustibleGepNumeroFactura).text.toString()
            val montoTotal = view.findViewById<EditText>(R.id.txtCombustibleGepMonto).text.toString()
            val estaciones = view.findViewById<EditText>(R.id.txtCombustibleGepEstaciones).text.toString()
            if (ruc.length == 11) {
                // El contenido tiene exactamente 11 dígitos
                combustibleGep(nroFactura,montoTotal,estaciones,ruc,sp)
            } else {
                // El contenido no tiene 8 dígitos
                Toast.makeText(context, "Ruc debe tener 11 digitos", Toast.LENGTH_LONG).show()
            }
        }
        return view
    }

    private fun combustibleGep(nroFactura:String,montoTotal:String,estaciones:String,ruc:String,sp:SharedPreferences){
        if (dateFactura != null && nroFactura.isNotBlank() && montoTotal.isNotBlank() && estaciones.isNotBlank() && ruc.isNotBlank()
            && galeracamerastringfactura != null && galeriacamerastringgalonera != null){
            btnCombustibleGEPEnviar.isEnabled = false
            val queue = Volley.newRequestQueue(context)
            val url = getString(R.string.urlAPI)+"/api/combustiblegep"
            val json = JSONObject().apply {
                put("usuario_id", sp.getString("id", ""))
                put("cuadrilla", zona)
                put("ruc",ruc)
                put("nro_factura", nroFactura)
                put("fecha_factura",dateFormat.format(dateFactura!!))
                put("monto_total", montoTotal)
                put("estacion", estaciones)
                put("foto_factura", galeracamerastringfactura)
                put("foto_galonera", galeriacamerastringgalonera)
                put("token", sp.getString("token", ""))
                put("fecha_insercion", dateFormat.format(Date()))
            }

            println("usuario_id: ${sp.getString("id", "")}")
            println("cuadrilla: $zona")
            println("ruc: $ruc")
            println("nro_factura: $nroFactura")
            println("fecha_factura: ${dateFormat.format(dateFactura!!)}")
            println("monto_total: $montoTotal")
            println("estacion: $estaciones")
            println("foto_factura: $galeracamerastringfactura")
            println("foto_galonera: $galeriacamerastringgalonera")
            println("token: ${sp.getString("token", "")}")
            println("fecha_insercion: ${dateFormat.format(Date())}")


            val stringRequest = JsonObjectRequest(
                Request.Method.POST,url,json,
                {response->
                    if(response.getString("response") == "1" ){
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
                    btnCombustibleGEPEnviar.isEnabled = true
                })
            queue.add(stringRequest)
        }else{

            Toast.makeText(context, "Complete todo los campos requeridos", Toast.LENGTH_LONG).show()
        }
    }

    private fun imagegallerycamera(btnCombustibleGepFoto: Button) {
        btnsubirimages = btnCombustibleGepFoto
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

        intent()

        viewfoto.btn_camara.setOnClickListener{

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
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

    private fun intent() {
        intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE_SECURE).also {
            view?.context?.let { it1 ->
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
    }

    //GALLERY

    private fun pickPhotoFromGallery() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        startForActivityGallery.launch(intent)
    }

    private val startForActivityGallery = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ){result ->
        if (result.resultCode == Activity.RESULT_OK){
            uricombustiblegep = result.data?.data
//            val bitmap = MediaStore.Images.Media.getBitmap(context?.getContentResolver(), Uri.parse(uricombustiblegep.toString()))
            val bitmap = MediaStore.Images.Media.getBitmap(context?.contentResolver, Uri.parse(uricombustiblegep.toString()))
            galeriacamerastringgep = encodeImage(bitmap)
            when(btnsubirimages){
                btnCombustiblegepFotoGalonera -> galeriacamerastringgalonera = galeriacamerastringgep
                btnCombustiblegepFotoFactura -> galeracamerastringfactura = galeriacamerastringgep
            }
            btnsubirimages!!.text = getString(R.string.imagenSubida)
        }
    }

    //Camara

    //Guardar imagen en galeria privada
    private lateinit var file: File
    private fun createPhotoFile() {
        val dir = view?.context?.getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!
        file = File.createTempFile("IMG_${System.currentTimeMillis()}_", ".jpg",dir)
    }

    private fun getBitmap(): Bitmap = BitmapFactory.decodeFile(file.toString())

    private val startForResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ){result ->
        if (result.resultCode == Activity.RESULT_OK){
            galeriacamerastringgep = encodeImage(getBitmap())
            when(btnsubirimages){
                btnCombustiblegepFotoGalonera -> galeriacamerastringgalonera = galeriacamerastringgep
                btnCombustiblegepFotoFactura -> galeracamerastringfactura = galeriacamerastringgep
            }
            btnsubirimages!!.text = getString(R.string.imagenSubida)
        }
    }

    private fun encodeImage(bm: Bitmap): String? {
        val baos = ByteArrayOutputStream()
        bm.compress(Bitmap.CompressFormat.JPEG, 25, baos)
        val b = baos.toByteArray()
        return Base64.encodeToString(b, Base64.DEFAULT)
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
         * @return A new instance of fragment CombustibleGPEConceptoFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            CombustibleGPEConceptoFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}