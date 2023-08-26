package com.miempresa.proyect_ccip

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley

class MyWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {

    override fun doWork(): Result {
        Log.d("MyWorker", "Tarea programada iniciada")
        val queue = Volley.newRequestQueue(applicationContext)
        val url = applicationContext.getString(R.string.urlAPI)+"/api/notificaciones"
//        val json = JSONObject().apply {
//            put("usuario_id", sp.getString("id", ""))
//            put("token", sp.getString("token", ""))
//        }
        val stringRequest = JsonObjectRequest(
            Request.Method.GET,url,null,
            {response->
                if(response.getString("response") == "1" ){
                    showNotification(response.getString("titulo"), response.getString("mensaje"))
                } else{

                    showNotification("Buenos dias","Hoy tendras un gran dia ")
                }

            },{
                Toast.makeText(applicationContext, "Compruebe su Conexion a Intenet", Toast.LENGTH_LONG).show()
            })
        queue.add(stringRequest)

//        createNotificationChannel()
//        showNotification("Tarea programada", "Esta es una notificación programada.")

        Log.d("MyWorker", "Tarea programada finalizada")
        return Result.success()
    }

    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Primera Notificacion"
            val descriptionText = "Esto es una descriupcion"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("1", name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                applicationContext.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }

    }


    private fun showNotification(title: String, message: String) {
        createNotificationChannel()
        val builder = NotificationCompat.Builder(applicationContext, "1")
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        with(NotificationManagerCompat.from(applicationContext)) {
            notify(123, builder.build())
        }
    }
}
