package com.example.gaurdiancircle

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gaurdiancircle.model.BasicResponse
import com.example.gaurdiancircle.models.Guardian
import com.example.gaurdiancircle.models.GuardianListResponse
import com.example.gaurdiancircle.retrofit.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class Myguardians : AppCompatActivity() {

    private lateinit var guardianRecyclerView: RecyclerView
    private lateinit var guardianAdapter: GuardianAdapter
    private lateinit var guardiansCountText: TextView
    private var guardianList: MutableList<Guardian> = mutableListOf()
    private var userId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_myguardians)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        guardianRecyclerView = findViewById(R.id.recyclerViewGuardians)
        guardiansCountText = findViewById(R.id.guardiansCountText)

        guardianRecyclerView.layoutManager = LinearLayoutManager(this)

        val sharedPref = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
        userId = sharedPref.getInt("user_id", 0)
        if (userId == 0) {
            Toast.makeText(this, "User not recognized", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        guardianAdapter = GuardianAdapter(guardianList) { guardian, position ->
            // Show confirmation dialog before deleting guardian
            AlertDialog.Builder(this)
                .setTitle("Delete Guardian")
                .setMessage("Are you sure you want to delete ${guardian.name}?")
                .setPositiveButton("Yes") { _, _ -> deleteGuardian(guardian.id, position) }
                .setNegativeButton("No", null)
                .show()
        }
        guardianRecyclerView.adapter = guardianAdapter

        findViewById<ImageView>(R.id.backButton).setOnClickListener { finish() }
        findViewById<Button>(R.id.addGuardianButton).setOnClickListener {
            startActivity(Intent(this, AddGuardian::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        loadGuardians(userId)
    }

    private fun loadGuardians(userId: Int) {
        RetrofitClient.instance.getGuardians(userId)
            .enqueue(object : Callback<GuardianListResponse> {
                override fun onResponse(call: Call<GuardianListResponse>, response: Response<GuardianListResponse>) {
                    if (response.isSuccessful) {
                        val allGuardians = response.body()?.guardians ?: emptyList()

                        // **Filter the list to include only "accepted" guardians**
                        val acceptedGuardians = allGuardians.filter {
                            it.status.equals("accepted", ignoreCase = true)
                        }

                        guardianList.clear()
                        guardianList.addAll(acceptedGuardians) // Add the filtered list
                        guardianAdapter.notifyDataSetChanged()

                        // Update the count based on the filtered list size
                        guardiansCountText.text = "${acceptedGuardians.size} Guardians Added"
                    } else {
                        Toast.makeText(this@Myguardians, "Failed to fetch guardians", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<GuardianListResponse>, t: Throwable) {
                    Toast.makeText(this@Myguardians, "Network error fetching guardians", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun deleteGuardian(guardianId: Int, position: Int) {
        RetrofitClient.instance.deleteGuardian(guardianId.toString())
            .enqueue(object : Callback<BasicResponse> {
                override fun onResponse(call: Call<BasicResponse>, response: Response<BasicResponse>) {
                    if (response.isSuccessful && response.body()?.status == "success") {
                        Toast.makeText(this@Myguardians, "Guardian deleted", Toast.LENGTH_SHORT).show()
                        guardianList.removeAt(position)
                        guardianAdapter.notifyItemRemoved(position)
                        guardiansCountText.text = "${guardianList.size} Guardians Added"
                    } else {
                        Toast.makeText(this@Myguardians, "Failed to delete guardian", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<BasicResponse>, t: Throwable) {
                    Toast.makeText(this@Myguardians, "Network error deleting guardian", Toast.LENGTH_SHORT).show()
                }
            })
    }
}
