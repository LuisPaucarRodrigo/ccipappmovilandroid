package com.miempresa.proyect_ccip

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.view.GravityCompat
import androidx.fragment.app.FragmentTransaction
import com.google.android.material.navigation.NavigationView
import kotlinx.android.synthetic.main.activity_base.*
import androidx.work.*
import java.util.concurrent.TimeUnit
import androidx.work.PeriodicWorkRequestBuilder
import java.util.Calendar


class BaseActivity : AppCompatActivity(),NavigationView.OnNavigationItemSelectedListener {

    private var nombre:String = ""
    private var apellido:String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_Proyect_ccip)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_base)
        //SharedPreferences
        val sp:SharedPreferences = getSharedPreferences("my_prefs",Context.MODE_PRIVATE)

        val now = Calendar.getInstance()
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 23) // Hora en formato de 24 horas
            set(Calendar.MINUTE, 37)
            set(Calendar.SECOND, 0)
            if (before(now)) {
                add(Calendar.DAY_OF_MONTH, 1) // Si ya pasó la hora de hoy, establece para mañana
            }
        }

        val delay = calendar.timeInMillis - now.timeInMillis

        val workRequest = OneTimeWorkRequestBuilder<MyWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .build()

        WorkManager.getInstance(applicationContext).enqueue(workRequest)



        //NavigatioView
        val navigationView: NavigationView =findViewById(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener(this)

//        setSupportActionBar(findViewById(R.id.mitoolbar))
//        supportActionBar?.setDisplayShowTitleEnabled(false)
        setSupportActionBar(findViewById(R.id.mitoolbar))
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_baseline_menu_24)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        //Recibiendo datos
        val bundle: Bundle? = intent.extras
        if(bundle !=null){
            nombre = bundle.getString("nombre").toString()
            apellido = bundle.getString("apellido").toString()
        }

        val navheader = navigationView.getHeaderView(0)
        val nameheader = navheader.findViewById<TextView>(R.id.txtnombreuser)
        val nameheaderdni = navheader.findViewById<TextView>(R.id.txtdniuser)
        val nameheaderemail = navheader.findViewById<TextView>(R.id.txtemail)

        nameheader.text = sp.getString("nombre","")+" "+sp.getString("apellido","")
        nameheaderdni.text = sp.getString("dni","")
        nameheaderemail.text = sp.getString("email","")+"\n"

        val transaccion: FragmentTransaction = supportFragmentManager.beginTransaction()
        transaccion.replace(R.id.contenedor, OperacionesFragment())
        transaccion.commit()
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id: Int = item.itemId
        if (id == android.R.id.home) {
            layout_lateral.openDrawer(GravityCompat.START)
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.nav_home-> home()
            R.id.nav_saldo-> gastos()
            R.id.nav_logout-> cerrarsesion()
        }
        layout_lateral.closeDrawer(GravityCompat.START)
        return true
    }

    private fun home() {
        val transaccion: FragmentTransaction = supportFragmentManager.beginTransaction()
        transaccion.replace(R.id.contenedor, OperacionesFragment())
            .addToBackStack(null)
        transaccion.commit()
    }

    private fun gastos() {
        val transaccion: FragmentTransaction = supportFragmentManager.beginTransaction()
        transaccion.replace(R.id.contenedor, Operaciones_Gastos_Fragment())
            .addToBackStack(null)
        transaccion.commit()
    }

    private fun cerrarsesion() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Cerrar Sesion")
        builder.setMessage("Esta seguro de Cerrar Sesion?")

        builder.setPositiveButton(android.R.string.yes) { _, _ ->
            val sp = getSharedPreferences("my_prefs", Context.MODE_PRIVATE)
            with(sp.edit()){
                putString("id","")
                putString("token","")
                putString("active","false")
                apply()
            }
            val authActivity = Intent(this,AuthActivity::class.java)
            startActivity(authActivity)
            finish()
        }
        builder.setNegativeButton(android.R.string.no) { _, _ ->
        }
        builder.show()
    }
}