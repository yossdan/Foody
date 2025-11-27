@file:Suppress("DEPRECATION")

package com.example.foodyapp

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import org.json.JSONObject

class Login : AppCompatActivity() {

    // GOOGLE
    private lateinit var googleSignInClient: com.google.android.gms.auth.api.signin.GoogleSignInClient
    private lateinit var googleSignInLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        // =====================
        // ANIMACIÓN DEL LOGO
        // =====================
        val logo = findViewById<TextView>(R.id.textViewLogo)
        val container = findViewById<View>(R.id.containerLogin)

        container.alpha = 0f
        container.translationY = 50f

        logo.text = ""
        val word = "Foody"
        var index = 0
        val handler = android.os.Handler(mainLooper)

        fun typing() {
            if (index < word.length) {
                val currentText = logo.text.toString() + word[index]
                val spannable = android.text.SpannableStringBuilder(currentText)

                val start = spannable.length - 1
                val end = spannable.length

                val initialColor = logo.currentTextColor and 0x00FFFFFF
                val alphaSpan = android.text.style.ForegroundColorSpan(initialColor)
                spannable.setSpan(alphaSpan, start, end, android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

                logo.text = spannable

                val animator = android.animation.ValueAnimator.ofInt(0, 255)
                animator.duration = 180
                animator.addUpdateListener { valueAnimator ->
                    val alpha = valueAnimator.animatedValue as Int
                    val newColor = (alpha shl 24) or (logo.currentTextColor and 0x00FFFFFF)

                    spannable.setSpan(
                        android.text.style.ForegroundColorSpan(newColor),
                        start, end,
                        android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )

                    logo.text = spannable
                }
                animator.start()

                index++
                handler.postDelayed({ typing() }, 150)
            } else {
                container.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(700)
                    .setInterpolator(android.view.animation.DecelerateInterpolator())
                    .start()
            }
        }

        handler.postDelayed({ typing() }, 300)

        // INSETS (si quieres puedes cambiar a un id del root, ej. R.id.main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.nombre_receta)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // =====================
        // LOGIN NORMAL (PHP)
        // =====================
        val txtOlvide = findViewById<TextView>(R.id.txtOlvidePassword)
        txtOlvide.setOnClickListener {
            startActivity(Intent(this, RecuperarPasswordActivity::class.java))
        }

        val usuario = findViewById<EditText>(R.id.usuario)
        val password = findViewById<EditText>(R.id.password)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val txtCrearCuenta = findViewById<TextView>(R.id.txtCrearCuenta)

        txtCrearCuenta.setOnClickListener {
            startActivity(Intent(this, Registro::class.java))
        }

        val url = "http://10.0.2.2/Foody/login.php"
        val queue = Volley.newRequestQueue(this)

        btnLogin.setOnClickListener {
            val user = usuario.text.toString().trim()
            val pass = password.text.toString().trim()

            if (user.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            Log.d("LoginDebug", "Usuario='$user', Password='$pass'")

            val stringRequest = object : StringRequest(
                Request.Method.POST, url,
                { response ->
                    try {
                        Log.d("LoginDebug", "Respuesta PHP: $response")
                        val json = JSONObject(response)
                        val message = json.optString("message")
                        Toast.makeText(this, message, Toast.LENGTH_LONG).show()

                        if (json.optBoolean("success")) {
                            val idUsuario = json.optString("IdUsuario")

                            val prefs = getSharedPreferences("FoodyPrefs", MODE_PRIVATE)
                            prefs.edit().putString("IdUsuario", idUsuario).apply()

                            startActivity(Intent(this, MainActivity::class.java))
                            finish()
                        }

                    } catch (e: Exception) {
                        Log.e("LoginDebug", "Error al parsear JSON: ${e.message}")
                        Toast.makeText(this, "Error en respuesta: $response", Toast.LENGTH_LONG).show()
                    }
                },
                { error ->
                    Log.e("LoginDebug", "Error de conexión: ${error.message}")
                    Toast.makeText(this, "Error de conexión: ${error.message}", Toast.LENGTH_LONG).show()
                }
            ) {
                override fun getParams(): MutableMap<String, String> {
                    val params = HashMap<String, String>()
                    params["usuario"] = user
                    params["password"] = pass
                    Log.d("LoginDebug", "Parámetros enviados: $params")
                    return params
                }
            }

            queue.add(stringRequest)
        }

        // =====================
        // GOOGLE SIGN-IN
        // =====================

        // 1) Configurar opciones: solo pedimos email
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // 2) Registrar el launcher para recibir el resultado
        googleSignInLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            Log.d("GoogleSignIn", "ResultCode=${result.resultCode}")
            if (result.resultCode == Activity.RESULT_OK) {
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                try {
                    val account: GoogleSignInAccount? = task.getResult(ApiException::class.java)

                    val email = account?.email
                    val nombre = account?.displayName
                    val googleId = account?.id

                    if (email != null) {
                        loginGooglePhp(email, nombre, googleId)
                    } else {
                        Toast.makeText(this, "No se pudo obtener el correo de Google", Toast.LENGTH_SHORT).show()
                    }

                } catch (e: ApiException) {
                    Log.e("GoogleSignIn", "Error código=${e.statusCode}", e)
                    Toast.makeText(this, "Error al iniciar con Google", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Inicio con Google cancelado", Toast.LENGTH_SHORT).show()
            }
        }

        // 3) Click del botón de Google
        val btnGoogle = findViewById<ImageButton>(R.id.btnGoogle)
        btnGoogle.setOnClickListener {
            googleSignInClient.signOut() // opcional
            val signInIntent = googleSignInClient.signInIntent
            googleSignInLauncher.launch(signInIntent)
        }
    }

    // ==================================
    // LLAMADA A PHP login_google.php
    // ==================================
    private fun loginGooglePhp(email: String, nombre: String?, googleId: String?) {
        Log.d("GooglePHP", "ENVIANDO → email=$email, nombre=$nombre, googleId=$googleId")
        val urlGoogle = "http://10.0.2.2/Foody/login_google.php"

        val queue = Volley.newRequestQueue(this)

        val request = object : StringRequest(
            Request.Method.POST,
            urlGoogle,
            { response ->
                try {
                    Log.d("GooglePHP", "Respuesta PHP Google: $response")

                    // 👇 Aquí SÍ existe "response"
                    val json = JSONObject(response)

                    val success  = json.optBoolean("success", false)
                    val message  = json.optString("message", "Error desconocido")
                    val esNuevo  = json.optBoolean("nuevo", false)
                    val idUsuario = json.optString("IdUsuario", "")

                    Toast.makeText(this, message, Toast.LENGTH_LONG).show()

                    if (success && idUsuario.isNotEmpty()) {
                        // Guardar IdUsuario
                        val prefs = getSharedPreferences("FoodyPrefs", MODE_PRIVATE)
                        prefs.edit().putString("IdUsuario", idUsuario).apply()

                        if (esNuevo) {
                            // Si es usuario nuevo por Google → lo mandas a completar registro
                            startActivity(Intent(this, Login::class.java))
                        } else {
                            // Si ya existía → directo al main
                            startActivity(Intent(this, MainActivity::class.java))
                        }
                        finish()
                    }

                } catch (e: Exception) {
                    Log.e("GooglePHP", "Error parseando JSON: ${e.message}", e)
                    Toast.makeText(this, "Error en formato de respuesta", Toast.LENGTH_LONG).show()
                }
            },
            { error ->
                Log.e("GooglePHP", "Error de conexión: ${error.message}", error)
                Toast.makeText(this, "Error de conexión (Google)", Toast.LENGTH_LONG).show()
            }
        ) {
            override fun getParams(): MutableMap<String, String> {
                val params = HashMap<String, String>()
                params["email"] = email
                params["nombre"] = nombre ?: ""
                params["google_id"] = googleId ?: ""
                return params
            }
        }

        queue.add(request)
    }

}
