package com.example.foodyapp

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import org.json.JSONObject
import android.widget.LinearLayout

class RecuperarPasswordActivity : AppCompatActivity() {

    private lateinit var edtCorreo: TextInputEditText
    private lateinit var edtCodigo: TextInputEditText
    private lateinit var edtPassNueva: TextInputEditText
    private lateinit var edtPassConfirmar: TextInputEditText
    private lateinit var layoutSegundoPaso: LinearLayout
    private lateinit var btnEnviarCodigo: MaterialButton
    private lateinit var btnCambiarPassword: MaterialButton

    private val queue by lazy { Volley.newRequestQueue(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recuperar_password)

        edtCorreo        = findViewById(R.id.edtCorreoRec)
        edtCodigo        = findViewById(R.id.edtCodigo)
        edtPassNueva     = findViewById(R.id.edtPassNueva)
        edtPassConfirmar = findViewById(R.id.edtPassConfirmar)
        layoutSegundoPaso = findViewById(R.id.layoutSegundoPaso)
        btnEnviarCodigo  = findViewById(R.id.btnEnviarCodigo)
        btnCambiarPassword = findViewById(R.id.btnCambiarPassword)

        btnEnviarCodigo.setOnClickListener { enviarCodigo() }
        btnCambiarPassword.setOnClickListener { cambiarPassword() }
    }

    private fun enviarCodigo() {
        val correo = edtCorreo.text?.toString()?.trim() ?: ""

        if (correo.isEmpty()) {
            Toast.makeText(this, "Ingresa tu correo", Toast.LENGTH_SHORT).show()
            return
        }

        val url = "http://10.0.2.2/foody/enviar_codigo_password.php"

        btnEnviarCodigo.isEnabled = false
        btnEnviarCodigo.text = "Enviando..."

        val request = object : StringRequest(
            Method.POST,
            url,
            { response ->
                btnEnviarCodigo.isEnabled = true
                btnEnviarCodigo.text = "Enviar código"

                // 👇 log para depurar
                android.util.Log.d("RecuperarDebug", "RESP: $response")

                try {
                    val json = JSONObject(response)
                    val success = json.optBoolean("success", false)
                    val message = json.optString("message", "Respuesta del servidor")

                    Toast.makeText(this, message, Toast.LENGTH_LONG).show()

                    if (success) {
                        layoutSegundoPaso.visibility = View.VISIBLE
                    }

                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(this, "Error al procesar respuesta", Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                btnEnviarCodigo.isEnabled = true
                btnEnviarCodigo.text = "Enviar código"
                error.printStackTrace()
                Toast.makeText(this, "Error de conexión", Toast.LENGTH_SHORT).show()
            }
        ) {
            override fun getParams(): MutableMap<String, String> {
                return hashMapOf(
                    "correo" to correo
                )
            }
        }

        queue.add(request)
    }

    private fun cambiarPassword() {
        val correo   = edtCorreo.text?.toString()?.trim() ?: ""
        val codigo   = edtCodigo.text?.toString()?.trim() ?: ""
        val passNew  = edtPassNueva.text?.toString()?.trim() ?: ""
        val passConf = edtPassConfirmar.text?.toString()?.trim() ?: ""

        if (correo.isEmpty() || codigo.isEmpty() || passNew.isEmpty() || passConf.isEmpty()) {
            Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        if (passNew != passConf) {
            Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
            return
        }

        if (passNew.length < 6) {
            Toast.makeText(this, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show()
            return
        }

        val url = "http://10.0.2.2/foody/cambiar_password_con_codigo.php"

        btnCambiarPassword.isEnabled = false
        btnCambiarPassword.text = "Guardando..."

        val request = object : StringRequest(
            Method.POST,
            url,
            { response ->
                btnCambiarPassword.isEnabled = true
                btnCambiarPassword.text = "Cambiar contraseña"

                try {
                    val json = JSONObject(response)
                    val success = json.optBoolean("success", false)
                    val message = json.optString("message", "Respuesta del servidor")

                    Toast.makeText(this, message, Toast.LENGTH_LONG).show()

                    if (success) {
                        // aquí puedes cerrar y volver al login
                        finish()
                    }

                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(this, "Error al procesar respuesta", Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                btnCambiarPassword.isEnabled = true
                btnCambiarPassword.text = "Cambiar contraseña"
                error.printStackTrace()
                Toast.makeText(this, "Error de conexión", Toast.LENGTH_SHORT).show()
            }
        ) {
            override fun getParams(): MutableMap<String, String> {
                return hashMapOf(
                    "correo"         to correo,
                    "codigo"         to codigo,
                    "password_nueva" to passNew
                )
            }
        }

        queue.add(request)
    }
}
