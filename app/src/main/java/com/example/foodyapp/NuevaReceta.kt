package com.example.foodyapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.toolbox.Volley
import com.example.foodyapp.databinding.ActivityNuevaRecetaBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import android.util.Log
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import org.json.JSONObject

import android.widget.LinearLayout
import android.widget.TextView


class NuevaReceta : AppCompatActivity() {

    private lateinit var binding: ActivityNuevaRecetaBinding
    private var saborSeleccionado: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Inicializamos ViewBinding
        binding = ActivityNuevaRecetaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Usamos el string-array desde resources
        val sabores = resources.getStringArray(R.array.sabores_array)

        binding.btnSeleccionarSabor.setOnClickListener {
            mostrarDialogoSabores(sabores)
        }

        binding.btnAtras.setOnClickListener {
            finish()
        }

    }

    private fun mostrarDialogoSabores(sabores: Array<String>) {
        MaterialAlertDialogBuilder(this).apply {
            setTitle(getString(R.string.selecciona_sabor))
            setItems(sabores) { _, which ->
                saborSeleccionado = sabores[which]
                binding.btnSeleccionarSabor.text = saborSeleccionado
            }
            show()
        }

        val NuevaReceta = findViewById<EditText>(R.id.NombreReceta)
        val TiempoPreparacion = findViewById<EditText>(R.id.TiempoPreparacion)
        //val SaborPlatillo = saborSeleccionado ?: ""
        val botonGuardar : Button = findViewById<Button>(R.id.btnGuardarReceta)
        val Ingredientes = findViewById<EditText>(R.id.Ingredientes)
        val Pasos = findViewById<EditText>(R.id.Pasos)



        val url ="http://10.0.2.2/Foody/insert.php"
        //val url = "http://dann.local/Foody/insert.php"
        val queue = Volley.newRequestQueue(this)




        botonGuardar.setOnClickListener {
            val nueva_receta = NuevaReceta.text.toString().trim()
            val tiempo_preparacion = TiempoPreparacion.text.toString().trim()
            val sabor_platillo = saborSeleccionado?.trim() ?: ""
            val ingrediente = Ingredientes.text.toString().trim()
            val pasos = Pasos.text.toString().trim()

            Log.d(ingrediente, "aa")
            Log.d(pasos, "aa")

            if (nueva_receta.isEmpty()) {
                NuevaReceta.error = "Este campo es obligatorio"
                Toast.makeText(this, "Ingresa un nonmbre", Toast.LENGTH_LONG).show()

            }
            if (ingrediente.isEmpty()) {
                Ingredientes.error = "Este campo es obligatorio"
                Toast.makeText(this, "Ingresa los ingredientes", Toast.LENGTH_LONG).show()

            }
            if (pasos.isEmpty()) {
                Pasos.error = "Este campo es obligatorio"
                Toast.makeText(this, "Ingresa los pasos", Toast.LENGTH_LONG).show()

            }
            if (tiempo_preparacion.isEmpty()){
                TiempoPreparacion.error = "Este campo es obligatorio"
                Toast.makeText(this, "Ingresa un tiempo", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            val stringRequest = object : StringRequest(
                Request.Method.POST, url,
                { response ->
                    try {
                        val json = JSONObject(response)
                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                        Toast.makeText(this, json.optString("message"), Toast.LENGTH_SHORT).show()
                    } catch (e: Exception){
                        Toast.makeText(this, "Registro exitoso: $response", Toast.LENGTH_LONG).show()
                    }
                },
                { error ->
                    Toast.makeText(this, "Error: ${error.message}", Toast.LENGTH_LONG).show()
                }
            ){
                override fun getParams(): MutableMap<String, String> {
                    val params = HashMap<String, String>()
                        params["nueva_receta"] = nueva_receta;
                        params["tiempo_preparacion"] = tiempo_preparacion;
                        params["sabor_platillo"] = sabor_platillo;
                        params["ingrediente"] = ingrediente;
                        params["pasos"] = pasos;


                        return params
                }
            }

            queue.add(stringRequest)
        }


    }


}
