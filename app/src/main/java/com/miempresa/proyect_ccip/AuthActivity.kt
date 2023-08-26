package com.miempresa.proyect_ccip

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.net.NetworkInfo
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.StrictMode
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import kotlinx.android.synthetic.main.activity_auth.*
import org.json.JSONObject

class AuthActivity : AppCompatActivity() {

    private var id = ""
    private var usuariodni = ""
    private var password = ""
    private var nombre = ""
    private var apellido = ""
    private var dni = ""
    private var email = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_Proyect_ccip)
        super.onCreate(savedInstanceState)

        val cm = this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork: NetworkInfo? = cm.activeNetworkInfo
        val isConnected: Boolean = activeNetwork?.isConnectedOrConnecting == true

        if (isConnected){
            configuracionesactividad()
        }else{
            setContentView(R.layout.activity_conexion)
            Handler(Looper.getMainLooper()).postDelayed({
                setContentView(R.layout.activity_conexion)
                val confi = configuraciones()
                conexion(confi)
            },1000)

        }
    }

    private fun configuracionesactividad(){
        setContentView(R.layout.activity_auth)
        val policy= StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

        val sp:SharedPreferences = getSharedPreferences("my_prefs",Context.MODE_PRIVATE)

        btnlogeo.setOnClickListener{
            usuariodni = findViewById<EditText>(R.id.txtusuariodni).text.toString()
            password = findViewById<EditText>(R.id.txtpassword).text.toString()
            validate(sp)
        }
        checkLogin(sp)
    }
    private fun configuraciones(): Boolean {
        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork: NetworkInfo? = cm.activeNetworkInfo
        return activeNetwork?.isConnectedOrConnecting == true
    }
    private fun conexion(
        isConnected: Boolean
    ) {
        Handler(Looper.getMainLooper()).postDelayed({
            if (isConnected){
                configuracionesactividad()
            }else{
                reconexion()
            }
        },1000)

    }

    private fun reconexion() {
        val confi = configuraciones()
        conexion(confi)
    }

    private fun validate(sp: SharedPreferences) {
        val result = arrayOf(validateEmail())
        if(false in result){
            return
        }
        autenthication(usuariodni,password,sp)
    }

    private fun validateEmail():Boolean {
        return if(usuariodni.isEmpty()){
            txtusuariodni.error = "Campo Obligatorio"
            false
        }else{
            txtusuariodni.error = null
            true
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        onDestroy()
    }

    private fun checkLogin(sp: SharedPreferences) {
        if (sp.getString("active","") == "true"){
            val baseActivity = Intent(this,BaseActivity::class.java)
            startActivity(baseActivity)
            this.finish()
        }else{
            if (sp.getString("remember","") == "true"){
                txtusuariodni.setText(sp.getString("dni",""))
                txtpassword.setText(sp.getString("password",""))
            }
        }
    }

    private fun rememberUser(
        sp: SharedPreferences,
        usuario: String,
        password: String,
        id: String,
        nombre: String,
        apellido: String,
        dni:String,
        email: String
    ) {
        val checkedPreference = findViewById<CheckBox>(R.id.checkBox_sharedPreference)
        if (checkedPreference.isChecked){
            with(sp.edit()){
                putString("user",usuario)
                putString("password",password)
                putString("id",id)
                putString("nombre",nombre)
                putString("apellido",apellido)
                putString("dni",dni)
                putString("email",email)
                putString("active","true")
                putString("remember","true")
                apply()
            }
        }else{
            with(sp.edit()){
                putString("nombre",nombre)
                putString("apellido",apellido)
                putString("dni",dni)
                putString("email",email)
                putString("id",id)
                putString("active","true")
                putString("remember","false")
                apply()
            }
        }
        val baseActivity = Intent(this,BaseActivity::class.java)
        startActivity(baseActivity)
        this.finish()
    }

    private fun autenthication(usuariodni: String, password: String, sp: SharedPreferences) {
        val queue = Volley.newRequestQueue(this)
        val url = getString(R.string.urlAPI)+"/api/login"
        val json = JSONObject()
        json.put("dni",usuariodni)
        json.put("password",password)
        val stringRequest = JsonObjectRequest(
            Request.Method.POST,url,json,
            { response ->
                if (response.getString("token").toString() != "0"){
                    val token = response.getString("token").toString()
                    id = response.getString("id").toString()
                    nombre = response.getString("name").toString()
                    apellido = response.getString("lastname").toString()
                    dni = response.getString("dni").toString()
                    email = response.getString("email").toString()
                    rememberUser(sp,usuariodni,password,id,nombre,apellido,dni,email)
                    with(sp.edit()){
                        putString("token",token)
                        apply()
                    }
                }else{
                    Toast.makeText(this,"Usuario o Password Incorrectos",Toast.LENGTH_LONG).show()
                }
            }, {
                Toast.makeText(
                    applicationContext,
                    "Compruebe su conexion a Internet",
                    Toast.LENGTH_LONG
                ).show()
            })
        queue.add(stringRequest)
    }
}