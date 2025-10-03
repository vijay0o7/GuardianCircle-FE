package com.example.gaurdiancircle

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.gaurdiancircle.responses.SignUpRequest
import com.example.gaurdiancircle.responses.SignUpResponse
import com.example.gaurdiancircle.retrofit.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class signup : AppCompatActivity() {

    private lateinit var etName: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPhoneNumber: EditText
    private lateinit var etPassword: EditText
    private lateinit var roleGroup: RadioGroup

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        etName = findViewById(R.id.etName)
        etEmail = findViewById(R.id.etEmail)
        etPhoneNumber = findViewById(R.id.etPhoneNumber)
        etPassword = findViewById(R.id.etPassword)
        roleGroup = findViewById(R.id.roleGroup)

        val btnSignUp = findViewById<Button>(R.id.btnSignUp)
        val tvLogin = findViewById<TextView>(R.id.tvLogin)

        btnSignUp.setOnClickListener {
            val name = etName.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val phone = etPhoneNumber.text.toString().trim()
            val password = etPassword.text.toString()
            val selectedRole = when (roleGroup.checkedRadioButtonId) {
                R.id.rbStudent -> "USER"
                R.id.rbFaculty -> "GUARDIAN"
                else -> ""
            }

            if (name.isEmpty() || email.isEmpty() || phone.isEmpty() || password.isEmpty() || selectedRole.isEmpty()) {
                Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val signupRequest = SignUpRequest(name, email, phone, password, selectedRole)
            performSignup(signupRequest)
        }

        tvLogin.setOnClickListener {
            startActivity(Intent(this, Login::class.java))
        }
    }

    private fun performSignup(signUpRequest: SignUpRequest) {
        RetrofitClient.instance.signup(signUpRequest.name,signUpRequest.email,signUpRequest.phone_number,signUpRequest.password,signUpRequest.role).enqueue(object : Callback<SignUpResponse> {
            override fun onResponse(call: Call<SignUpResponse>, response: Response<SignUpResponse>) {
                if (response.isSuccessful && response.body()?.status == "success") {
                    Toast.makeText(this@signup, "Signup successful", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this@signup, Login::class.java))
                    finish()
                } else {
                    Toast.makeText(this@signup, "Signup failed: ${response.body()?.message ?: "Unknown error"}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<SignUpResponse>, t: Throwable) {
                Toast.makeText(this@signup, "Network error: ${t.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
