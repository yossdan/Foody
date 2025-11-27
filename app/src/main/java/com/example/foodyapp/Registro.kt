package com.example.foodyapp

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

class Registro : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_registro)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mainRegistro)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val edtUsuario   = findViewById<EditText>(R.id.edtUsuarioRegistro)
        val edtCorreo    = findViewById<EditText>(R.id.Correoedt)
        val edtPassword  = findViewById<EditText>(R.id.edtPasswordRegistro)
        val edtConfirmar = findViewById<EditText>(R.id.edtConfirmarPassword)
        val btnCrearCuenta = findViewById<Button>(R.id.btnCrearCuenta)
        val txtIrLogin     = findViewById<TextView>(R.id.txtIrLogin)

        txtIrLogin.setOnClickListener { finish() }

        val url = "http://10.0.2.2/Foody/registrar_usuario.php"
        val queue = Volley.newRequestQueue(this)

        btnCrearCuenta.setOnClickListener {
            val user   = edtUsuario.text.toString().trim()
            val correo = edtCorreo.text.toString().trim()
            val pass   = edtPassword.text.toString().trim()
            val pass2  = edtConfirmar.text.toString().trim()

            if (user.isEmpty() || correo.isEmpty() || pass.isEmpty() || pass2.isEmpty()) {
                Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
                Toast.makeText(this, "Correo no válido", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (pass != pass2) {
                Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val request = object : StringRequest(
                Request.Method.POST, url,
                { response ->
                    try {
                        val json = JSONObject(response)
                        val success = json.optBoolean("success", false)
                        val message = json.optString("message", "Error desconocido")

                        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

                        if (success) {
                            // Registro OK → volvemos al login
                            finish()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(this, "Error al procesar respuesta", Toast.LENGTH_SHORT).show()
                    }
                },
                { error ->
                    Toast.makeText(this, "Error de conexión: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            ) {
                override fun getParams(): MutableMap<String, String> {
                    val params = HashMap<String, String>()
                    params["usuario"]  = user
                    params["correo"]   = correo
                    params["password"] = pass
                    return params
                }
            }

            queue.add(request)
        }
    }
}
