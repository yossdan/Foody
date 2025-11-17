package com.example.foodyapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
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

class Login : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        val logo = findViewById<TextView>(R.id.textViewLogo)
        val container = findViewById<View>(R.id.containerLogin)

// Ocultar formulario
        container.alpha = 0f
        container.translationY = 50f

// Animación escritura suave REAL (letra por letra)
        logo.text = ""
        val word = "Foody"

        var index = 0
        val handler = android.os.Handler(mainLooper)

        fun typing() {
            if (index < word.length) {

                // Crear texto con nueva letra
                val currentText = logo.text.toString() + word[index]
                val spannable = android.text.SpannableStringBuilder(currentText)

                val start = spannable.length - 1
                val end = spannable.length

                // La nueva letra inicia con alpha 0 (invisible)
                val initialColor = logo.currentTextColor and 0x00FFFFFF
                val alphaSpan = android.text.style.ForegroundColorSpan(initialColor)
                spannable.setSpan(alphaSpan, start, end, android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

                logo.text = spannable

                // Animación de desvanecido SUAVE tipo pincel
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

                // Mostrar formulario
                container.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(700)
                    .setInterpolator(android.view.animation.DecelerateInterpolator())
                    .start()
            }
        }

// Retraso inicial
        handler.postDelayed({ typing() }, 300)


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val usuario = findViewById<EditText>(R.id.usuario)
        val password = findViewById<EditText>(R.id.password)
        val btnLogin = findViewById<Button>(R.id.btnLogin)

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

            // ✅ ESTA LÍNEA DEBE IR FUERA DE LA CLASE ANÓNIMA
            queue.add(stringRequest)
        }
    }
}
