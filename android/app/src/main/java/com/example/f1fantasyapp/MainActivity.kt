package com.example.f1fantasyapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val etUsername = findViewById<EditText>(R.id.etUsername)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val tvResult = findViewById<TextView>(R.id.tvResult)

        val btnRegister = findViewById<Button>(R.id.tvGoToRegister)

        btnRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        btnLogin.setOnClickListener {
            val username = etUsername.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter details", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val request = LoginRequest(username, password)

            RetrofitClient.instance.login(request).enqueue(object : Callback<LoginResponse> {
                override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                    if (response.isSuccessful && response.body() != null) {
                        val loginResponse = response.body()!!

                        UserManager.userId = loginResponse.user_id
                        UserManager.username = loginResponse.username
                        UserManager.isAdmin = loginResponse.is_admin

                        Toast.makeText(this@MainActivity, "Welcome to the Paddock, ${UserManager.username}!", Toast.LENGTH_SHORT).show()

                        val intent = Intent(this@MainActivity, HomeActivity::class.java)
                        startActivity(intent)
                        finish()

                    } else {
                        tvResult.text = "Login Failed: Invalid credentials"
                        tvResult.setTextColor(resources.getColor(android.R.color.holo_red_light))
                    }
                }

                override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                    tvResult.text = "Error: ${t.message}"
                    tvResult.setTextColor(resources.getColor(android.R.color.holo_red_light))
                }
            })
        }
    }
}