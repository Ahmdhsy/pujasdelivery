package com.example.pujasdelivery

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.pujasdelivery.api.RetrofitClient
import com.example.pujasdelivery.data.RegisterResponse
import com.example.pujasdelivery.databinding.ActivitySignInBinding
import com.google.firebase.auth.FirebaseAuth
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SignInActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignInBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var sharedPreferences: SharedPreferences
    private val TAG = "SignInActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)

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
                                    fetchUserData(idToken, isManualLogin = true)
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

    override fun onStart() {
        super.onStart()
        val currentUser = firebaseAuth.currentUser
        if (currentUser != null) {
            Log.d(TAG, "Pengguna sudah login, UID=${currentUser.uid}")
            currentUser.getIdToken(true).addOnCompleteListener { tokenTask ->
                if (tokenTask.isSuccessful) {
                    val idToken = tokenTask.result?.token
                    if (idToken != null) {
                        Log.d(TAG, "ID Token diperoleh untuk auto-login")
                        fetchUserData(idToken, isManualLogin = false)
                    } else {
                        Log.e(TAG, "ID Token null saat auto-login")
                    }
                } else {
                    Log.e(TAG, "Gagal mendapatkan token saat auto-login: ${tokenTask.exception?.message}")
                }
            }
        }
    }

    private fun fetchUserData(idToken: String, isManualLogin: Boolean) {
        val apiService = RetrofitClient.apiService

        apiService.getUser("Bearer $idToken").enqueue(object : Callback<RegisterResponse> {
            override fun onResponse(call: Call<RegisterResponse>, response: Response<RegisterResponse>) {
                if (response.isSuccessful && response.body()?.status == "success") {
                    val userData = response.body()?.data
                    Log.d(TAG, "Data pengguna diperoleh: role=${userData?.role}")
                    if (isManualLogin) {
                        Toast.makeText(this@SignInActivity, "Login berhasil! Role: ${userData?.role}", Toast.LENGTH_SHORT).show()
                    }
                    // Simpan role ke SharedPreferences
                    sharedPreferences.edit().apply {
                        putString("user_role", userData?.role)
                        apply()
                    }
                    navigateToMainActivity(userData?.role)
                } else {
                    Log.e(TAG, "Gagal mengambil data pengguna: code=${response.code()}, message=${response.message()}, body=${response.errorBody()?.string()}")
                    if (isManualLogin) {
                        Toast.makeText(this@SignInActivity, "Gagal mengambil data pengguna: ${response.message()}", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onFailure(call: Call<RegisterResponse>, t: Throwable) {
                Log.e(TAG, "Error jaringan: ${t.message}")
                if (isManualLogin) {
                    Toast.makeText(this@SignInActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    private fun navigateToMainActivity(role: String?) {
        val intent = Intent(this@SignInActivity, MainActivity::class.java)
        intent.putExtra("role", role)
        startActivity(intent)
        finish()
    }
}