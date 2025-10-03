package com.example.gaurdiancircle

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.gaurdiancircle.responses.LoginResponse
import com.example.gaurdiancircle.responses.LoginRequest
import com.example.gaurdiancircle.retrofit.RetrofitClient
import com.example.gaurdiancircle.utils.Utils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class Login : AppCompatActivity() {

    lateinit var emailOrPhoneEditText: EditText
    lateinit var passwordEditText: EditText
    lateinit var loginButton: Button
    lateinit var forgotPasswordText: TextView
    lateinit var signUpText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        emailOrPhoneEditText = findViewById(R.id.etEmailPhone)
        passwordEditText = findViewById(R.id.etPassword)
        loginButton = findViewById(R.id.loginBtn)
        forgotPasswordText = findViewById(R.id.forgotPassword)
        signUpText = findViewById(R.id.signUp)

        loginButton.setOnClickListener {
            val emailOrPhone = emailOrPhoneEditText.text.toString().trim()
            val password = passwordEditText.text.toString()

            if (emailOrPhone.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show()
            } else {
                checkLogin(emailOrPhone, password)
            }
        }

        forgotPasswordText.setOnClickListener {
            val intent = Intent(this, forgotpassword::class.java)
            startActivity(intent)
        }

        signUpText.setOnClickListener {
            startActivity(Intent(this, signup::class.java))
        }
    }

    private fun checkLogin(emailOrPhone: String, password: String) {
        RetrofitClient.instance.login(emailOrPhone, password)
            .enqueue(object : Callback<LoginResponse> {
                override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                    if (response.isSuccessful) {
                        val body = response.body()
                        if (body != null && body.status == "success") {
                            Toast.makeText(this@Login, "Login successful", Toast.LENGTH_SHORT).show()

                            // Save name, email, and user_id in SharedPreferences
                            Utils.saveLoginData(
                                context = this@Login,
                                name = body.name ?: "",
                                email = body.email ?: "",
                                userId = body.userId ?: -1,
                                role = body.role ?: "",
                                phone = body.phone_number ?: ""  // ✅ now works
                            )

                            Log.d("Login", "Name: ${body.name}, Email: ${body.email}, User ID: ${body.userId}")

                            // Go to Permissions screen with role
                            if (ContextCompat.checkSelfPermission(
                                    this@Login,
                                    Manifest.permission.ACCESS_FINE_LOCATION
                                ) == PackageManager.PERMISSION_GRANTED
                            ) {
                                // Permission granted → Go to home directly
                                goToHome(body.role)
                            } else {
                                // Permission not granted → Go to Permissions activity
                                val intent = Intent(this@Login, Permissions::class.java)
                                intent.putExtra("role", body.role)
                                startActivity(intent)
                            }
                            finish()
                        } else {
                            Toast.makeText(this@Login, body?.message ?: "Login failed", Toast.LENGTH_SHORT).show()
                            Log.e("LoginError", "Login error body: ${body?.message}")
                        }
                    } else {
                        // Retrofit error body requires .string() call, could throw if null
                        val errorMsg = try {
                            response.errorBody()?.string() ?: "Unknown error"
                        } catch (e: Exception) {
                            "Error parsing error body"
                        }
                        Toast.makeText(this@Login, "Login failed: ${response.message()}", Toast.LENGTH_SHORT).show()
                        Log.e("LoginError", "Response failed: $errorMsg")
                    }
                }

                override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                    Toast.makeText(this@Login, "Network error: ${t.localizedMessage}", Toast.LENGTH_SHORT).show()
                    Log.e("LoginError", "Network failure", t)
                }
            })
    }



    private fun goToHome(role: String?) {
        if (role?.uppercase() == "GUARDIAN") {
            startActivity(Intent(this, guardianhome2::class.java))
        } else {
            startActivity(Intent(this, Home::class.java))
        }
        finish()
    }

}
