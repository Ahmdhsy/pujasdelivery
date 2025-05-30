package com.example.pujasdelivery

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.pujasdelivery.api.ApiService
import com.example.pujasdelivery.data.RegisterResponse
import com.example.pujasdelivery.databinding.ActivitySignInBinding
import com.google.firebase.auth.FirebaseAuth
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class SignInActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignInBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private val TAG = "SignInActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        binding.textView.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }

        binding.button.setOnClickListener {
            val email = binding.emailEt.text.toString().trim()
            val pass = binding.passET.text.toString().trim()

            if (email.isNotEmpty() && pass.isNotEmpty()) {
                Log.d(TAG, "Mencoba login: email=$email")
                firebaseAuth.signInWithEmailAndPassword(email, pass).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val user = firebaseAuth.currentUser
                        Log.d(TAG, "Firebase login berhasil, UID=${user?.uid}")
                        user?.getIdToken(true)?.addOnCompleteListener { tokenTask ->
                            if (tokenTask.isSuccessful) {
                                val idToken = tokenTask.result?.token
                                if (idToken != null) {
                                    Log.d(TAG, "ID Token diperoleh, memanggil backend")
                                    fetchUserData(idToken)
                                } else {
                                    Log.e(TAG, "ID Token null")
                                    Toast.makeText(this, "Gagal mendapatkan token", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                Log.e(TAG, "Gagal mendapatkan token: ${tokenTask.exception?.message}")
                                Toast.makeText(this, "Gagal mendapatkan token: ${tokenTask.exception?.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        Log.e(TAG, "Login Firebase gagal: ${task.exception?.message}")
                        Toast.makeText(this, "Login gagal: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(this, "Tidak boleh ada kotak yang kosong", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun fetchUserData(idToken: String) {
        val retrofit = Retrofit.Builder()
            .baseUrl("http://10.0.2.2:8000/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService = retrofit.create(ApiService::class.java)

        apiService.getUser("Bearer $idToken").enqueue(object : Callback<RegisterResponse> {
            override fun onResponse(call: Call<RegisterResponse>, response: Response<RegisterResponse>) {
                if (response.isSuccessful && response.body()?.status == "success") {
                    val userData = response.body()?.data
                    Log.d(TAG, "Data pengguna diperoleh: role=${userData?.role}")
                    Toast.makeText(this@SignInActivity, "Login berhasil! Role: ${userData?.role}", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this@SignInActivity, MainActivity::class.java)
                    intent.putExtra("role", userData?.role)
                    startActivity(intent)
                    finish()
                } else {
                    Log.e(TAG, "Gagal mengambil data pengguna: code=${response.code()}, message=${response.message()}, body=${response.errorBody()?.string()}")
                    Toast.makeText(this@SignInActivity, "Gagal mengambil data pengguna: ${response.message()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<RegisterResponse>, t: Throwable) {
                Log.e(TAG, "Error jaringan: ${t.message}")
                Toast.makeText(this@SignInActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onStart() {
        super.onStart()
        if (firebaseAuth.currentUser != null) {
            // Tidak langsung login, tunggu fetchUserData untuk menentukan role
        }
    }
}