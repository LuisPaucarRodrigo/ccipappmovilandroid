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
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.FragmentTransaction
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import kotlinx.android.synthetic.main.fragment_otros__concepto.*
import kotlinx.android.synthetic.main.fragment_otros__concepto.view.*
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
 * Use the [OtrosConceptoFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class OtrosConceptoFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private var intent:Intent? = null
    private var btnsubirimagesotros:Button? = null
    private var fotogeleriacamera:String? = null
    private lateinit var file: File
    private var zona:String = ""
    private var uri:Uri? = null
    private var dateDocumento:Date? = null
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
        val view:View = inflater.inflate(R.layout.fragment_otros__concepto, container, false)
        if (arguments != null){
            zona = requireArguments().getString("zona").toString()
        }
        val sp: SharedPreferences = view.context.getSharedPreferences("my_prefs", Context.MODE_PRIVATE)
        val spinnerTipoDoc = view.findViewById<Spinner>(R.id.spinner_tipo_doc)
        val tipoDoc = arrayOf("Deposito","Factura","Boleta","Voucher de Pago","Control Operativo")
        spinnerTipoDoc.adapter = ArrayAdapter(
            view.context,R.layout.styles_spinner_otros,tipoDoc
        )
        val spinnerAutorizacion = view.findViewById<Spinner>(R.id.spinner_autorizacion)
        val autorizado = arrayOf("Gustavo Flores","Maria Flores")
        spinnerAutorizacion.adapter = ArrayAdapter(
            view.context,R.layout.styles_spinner_otros,autorizado
        )
        view.dateEditTextOtros.setOnClickListener {
            showDatePickerDialog(requireContext()) { selectedDate ->
                val formattedDate = dateFormatter.format(selectedDate.time)
                dateEditTextOtros.setText(formattedDate)
                dateDocumento = selectedDate
            }
        }
        view.btnOtrosFoto.setOnClickListener{
            imageGaleriaCamera(btnOtrosFoto)
        }
        view.btnOtrosEnviar.setOnClickListener{
            hideKeyboard()
            val ruc = view.findViewById<EditText>(R.id.txt_otros_numero_Ruc).text.toString()
            val tipoDocEnvio = spinner_tipo_doc.selectedItem.toString()
            val autorizacionEnvio = spinner_autorizacion.selectedItem.toString()
            val nroDocEnvio = view.findViewById<EditText>(R.id.txt_otro_nro_doc).text.toString()
            val descripcionEnvio = view.findViewById<EditText>(R.id.txt_otros_descripcion).text.toString()
            val montoTotalOtros = view.findViewById<EditText>(R.id.txt_otros_monto_total).text.toString()
            if (ruc.length == 11) {
                // El contenido tiene exactamente 11 dígitos
                enviarOtros(tipoDocEnvio,nroDocEnvio,descripcionEnvio,montoTotalOtros,autorizacionEnvio,ruc,sp)
            } else {
                // El contenido no tiene 8 dígitos
                Toast.makeText(context, "Ruc debe tener 11 digitos", Toast.LENGTH_LONG).show()
            }
        }
        return view
    }

    private fun enviarOtros(
        tipoDocEnvio: String,
        nroDocEnvio: String,
        descripcionEnvio: String,
        montoTotalOtros: String,
        autorizacionEnvio: String,
        ruc:String,
        sp: SharedPreferences
    ) {
        if (dateDocumento != null && tipoDocEnvio.isNotEmpty() && nroDocEnvio.isNotBlank() && autorizacionEnvio.isNotEmpty()
            && descripcionEnvio.isNotBlank() && montoTotalOtros.isNotBlank() && ruc.isNotBlank()
            && fotogeleriacamera != null){
            btnOtrosEnviar.isEnabled = false
            val queue = Volley.newRequestQueue(context)
            val url = getString(R.string.urlAPI)+"/api/otros"
            val json = JSONObject().apply {
                put("usuario_id",sp.getString("id",""))
                put("cuadrilla",zona)
                put("ruc",ruc)
                put("tipo_documento",tipoDocEnvio)
                put("numero_documento",nroDocEnvio)
                put("fecha_documento",dateFormat.format(dateDocumento))
                put("autorizacion",autorizacionEnvio)
                put("descripcion",descripcionEnvio)
                put("foto_otros",fotogeleriacamera)
                put("monto_total",montoTotalOtros)
                put("token",sp.getString("token",""))
                put("fecha_insercion", dateFormat.format(Date()))
            }
            val stringRequest = JsonObjectRequest(
                Request.Method.POST,url,json,
                {response->
                    if(response.getString("response") == "1"){
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
            Toast.makeText(context,"Complete todos los campos",Toast.LENGTH_LONG).show()
        }
    }

    private fun imageGaleriaCamera(btn_combustible_foto: Button) {
        btnsubirimagesotros = btn_combustible_foto
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

    //intent
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
            fotogeleriacamera = encodeImage(bitmap)
            btnOtrosFoto.text = getString(R.string.imagenSubida)
        }
    }

    private fun createPhotoFile() {
        val dir = view?.context?.getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!
        file = File.createTempFile("IMG_${System.currentTimeMillis()}_", ".jpg",dir)
    }

    private fun getBitmap(): Bitmap = BitmapFactory.decodeFile(file.toString())

    private val startForResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
        ){result ->
        if (result.resultCode == Activity.RESULT_OK){
            fotogeleriacamera = encodeImage(getBitmap())
            btnOtrosFoto.text = getString(R.string.imagenSubida)
        }
    }

    private fun encodeImage(bm: Bitmap): String? {
        val baos = ByteArrayOutputStream()
        bm.compress(Bitmap.CompressFormat.JPEG, 10, baos)
        val b = baos.toByteArray()
        return Base64.encodeToString(b, Base64.DEFAULT)
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ){isGranted ->
        if (isGranted){
            pickPhotoFromGallery()
        }else{
            Toast.makeText(context,"Necesita habilitar los permisos",Toast.LENGTH_LONG).show()
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
         * @return A new instance of fragment Otros_ConceptoFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            OtrosConceptoFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}