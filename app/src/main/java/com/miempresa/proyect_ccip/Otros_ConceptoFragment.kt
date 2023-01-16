package com.miempresa.proyect_ccip


import android.Manifest
import android.app.Activity
import android.content.ContentValues
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
import android.text.Editable
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
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [Otros_ConceptoFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class Otros_ConceptoFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private var intent:Intent? = null
    private var btnsubirimagesotros:Button? = null
    private var fotogeleriacamera:String? = null
    private lateinit var file: File
    private var zona:String = ""
    private var uri:Uri? = null

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
        val spinner_tipo_doc = view.findViewById<Spinner>(R.id.spinner_tipo_doc)
        val tipo_doc = arrayOf("Deposito","Factura","Boleta","Voucher de Pago","Control Operativo")
        spinner_tipo_doc.adapter = ArrayAdapter(
            view.context,R.layout.styles_spinner_otros,tipo_doc
        )
        val spinner_autorizacion = view.findViewById<Spinner>(R.id.spinner_autorizacion)
        val autorizado = arrayOf("Gustavo Flores","Maria Flores")
        spinner_autorizacion.adapter = ArrayAdapter(
            view.context,R.layout.styles_spinner_otros,autorizado
        )
        view.btn_otros_foto.setOnClickListener{
            image_galeria_camera(btn_otros_foto)
        }
        view.btn_otros_Enviar.setOnClickListener{
            val tipo_doc_envio = spinner_tipo_doc.selectedItem.toString()
            val autorizacion_envio = spinner_autorizacion.selectedItem.toString()
            val nro_doc_envio = view.findViewById<EditText>(R.id.txt_otro_nro_doc).text
            val descripcion_envio = view.findViewById<EditText>(R.id.txt_otros_descripcion).text
            val monto_total_otros = view.findViewById<EditText>(R.id.txt_otros_monto_total).text
            hideKeyboard()
            enviar_otros(tipo_doc_envio,nro_doc_envio,descripcion_envio,monto_total_otros,autorizacion_envio,sp)
        }
        return view
    }

    private fun enviar_otros(
        tipoDocEnvio: String,
        nroDocEnvio: Editable,
        descripcionEnvio: Editable,
        montoTotalOtros: Editable,
        autorizacionEnvio: String,
        sp: SharedPreferences
    ) {
        if (tipoDocEnvio != null && autorizacionEnvio != null) {
            if (tipoDocEnvio.isNotEmpty() && nroDocEnvio.isNotEmpty()
                && descripcionEnvio.isNotEmpty() && montoTotalOtros.isNotEmpty()
                && fotogeleriacamera != null){
                btn_otros_Enviar.isEnabled = false
                val queue = Volley.newRequestQueue(context)
                val url = getString(R.string.urlAPI)+"/api/otros"
                val json = JSONObject()
                json.put("usuario_id",sp.getString("id",""))
                json.put("cuadrilla",zona)
                json.put("tipo_documento",tipoDocEnvio)
                json.put("numero_documento",nroDocEnvio)
                json.put("autorizacion",autorizacionEnvio)
                json.put("descripcion",descripcionEnvio)
                json.put("foto_otros",fotogeleriacamera)
                json.put("monto_total",montoTotalOtros)
                json.put("token",sp.getString("token",""))
                json.put("fecha_otros", SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(Date()))
                val stringRequest = JsonObjectRequest(
                    Request.Method.POST,url,json,
                    {response->
                        if(response.getString("response") == "1"){
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
                Toast.makeText(context,"Complete todos los campos",Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun image_galeria_camera(btn_combustible_foto: Button) {
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
                it.resolveActivity(it1.packageManager).also { component ->
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
            //datastring es para texto
            //data es para la imagen misma
            uri = result.data?.data
            val bitmap = MediaStore.Images.Media.getBitmap(context?.getContentResolver(), Uri.parse(uri.toString()))
            fotogeleriacamera = encodeImage(bitmap)
            btn_otros_foto.text = "Imagen subida"
        }
    }

    private fun createPhotoFile() {
        val dir = view?.context?.getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!
        file = File.createTempFile("IMG_${System.currentTimeMillis()}_", ".jpg",dir)
    }

    private fun saveToGallery() {
        val content = createContent()
        val uri = save(content)
        clearContents(content, uri)
    }

    private fun createContent(): ContentValues {
        val fileName = file.name
        val fileType = "image/jpg"
        return ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.Files.FileColumns.MIME_TYPE, fileType)
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
            put(MediaStore.MediaColumns.IS_PENDING, 1)
        }
    }

    private fun save(content: ContentValues): Uri {
        var outputStream: OutputStream? = null
        var uri: Uri? = null
        view?.context?.contentResolver.also { resolver ->
            if (resolver != null) {
                uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, content)
                outputStream = resolver.openOutputStream(uri!!)
            }
        }
        outputStream.use { output ->
            getBitmap().compress(Bitmap.CompressFormat.JPEG, 100, output)
        }
        return uri!!
    }

    private fun clearContents(content: ContentValues, uri: Uri) {
        content.clear()
        content.put(MediaStore.MediaColumns.IS_PENDING,0)
        view?.context?.contentResolver?.update(uri,content,null,null)
    }

    private fun getBitmap(): Bitmap = BitmapFactory.decodeFile(file.toString())

    private val startForResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
        ){result ->
        if (result.resultCode == Activity.RESULT_OK){
            //datastring es para texto
            //data es para la imagen misma
            //val intent = result.data
            //val imagenBitmap = intent?.extras?.get("data") as Bitmap
            fotogeleriacamera = encodeImage(getBitmap())
            saveToGallery()
            btn_otros_foto.text = "Imagen subida"
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
            Otros_ConceptoFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}