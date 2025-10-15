    package com.example.foodyapp

    import android.content.Intent
    import android.os.Bundle
    import android.util.Log
    import android.widget.Button
    import android.widget.EditText
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
                                val loginExitoso = Intent(this, MainActivity::class.java)
                                startActivity(loginExitoso)
                                finish()
                            }
                        } catch (e: Exception) {
                            Log.e("LoginDebug", "Error al parsear JSON: ${e.message}")
                            Toast.makeText(this, "Error en respuesta: $response", Toast.LENGTH_LONG).show()
                        }
                    },
                    { error ->
                        Log.e("LoginDebug", "Error de conexión: ${error.message}")
                        Toast.makeText(this, "Error en conexión: ${error.message}", Toast.LENGTH_LONG).show()
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
        }
    }
