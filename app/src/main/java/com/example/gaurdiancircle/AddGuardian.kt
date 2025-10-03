package com.example.gaurdiancircle

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.gaurdiancircle.model.BasicResponse
import com.example.gaurdiancircle.retrofit.RetrofitClient
import com.example.gaurdiancircle.utils.Utils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AddGuardian : AppCompatActivity() {

    private lateinit var nameEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var phoneEditText: EditText
    private lateinit var addGuardianButton: Button
    private lateinit var maleRadioButton: RadioButton
    private lateinit var femaleRadioButton: RadioButton
    private var userId: Int = 0



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_guardian)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val sharedPref = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
        userId = sharedPref.getInt("user_id", 0)
        val user_name = Utils.getUserName(this) ?: ""
        Log.d("AddGuardian", "User ID: $userId")
        Log.d("AddGuardian", "User Name: $user_name")
        if (userId == 0) {
            Toast.makeText(this, "User ID missing", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        nameEditText = findViewById(R.id.nameEditText)
        emailEditText = findViewById(R.id.emailEditText)
        phoneEditText = findViewById(R.id.phoneEditText)
        addGuardianButton = findViewById(R.id.addGuardianButton)
        maleRadioButton = findViewById(R.id.male)
        femaleRadioButton = findViewById(R.id.female)

        findViewById<ImageView>(R.id.backButton).setOnClickListener { finish() }

        addGuardianButton.setOnClickListener {
            val name = nameEditText.text.toString().trim()
            val email = emailEditText.text.toString().trim()
            val phone = phoneEditText.text.toString().trim()
            val gender = when {
                maleRadioButton.isChecked -> "Male"
                femaleRadioButton.isChecked -> "Female"
                else -> ""
            }

            if (name.isEmpty() || email.isEmpty() || phone.isEmpty() || gender.isEmpty()||user_name.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            RetrofitClient.instance.addGuardian(userId, name, email, phone, gender, user_name)
                .enqueue(object : Callback<BasicResponse> {
                    override fun onResponse(call: Call<BasicResponse>, response: Response<BasicResponse>) {
                        if (response.isSuccessful && response.body()?.status == "success") {
                            Toast.makeText(this@AddGuardian, "Guardian Added", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this@AddGuardian, Myguardians::class.java))
                            finish()
                        } else {
                            Toast.makeText(this@AddGuardian, "Failed: ${response.body()?.message ?: "Unknown error"}", Toast.LENGTH_SHORT).show()
                        }
                    }
                    override fun onFailure(call: Call<BasicResponse>, t: Throwable) {
                        Toast.makeText(this@AddGuardian, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                    }
                })
        }
    }
}
