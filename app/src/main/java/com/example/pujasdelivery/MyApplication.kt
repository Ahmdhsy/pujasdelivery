package com.example.pujasdelivery

import android.app.Application
import com.example.pujasdelivery.api.ApiService
import com.example.pujasdelivery.api.RetrofitClient
import com.example.pujasdelivery.viewmodel.CourierViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.dsl.module
import android.content.SharedPreferences

class MyApplication : Application() {
    companion object {
        var token: String? = null
            get() = field ?: getSharedPreferences().getString("auth_token", null)
            set(value) {
                field = value
                getSharedPreferences().edit().putString("auth_token", value).apply()
            }
        var userId: Int? = null
            get() = field ?: getSharedPreferences().getInt("user_id", 0)
            set(value) {
                field = value
                getSharedPreferences().edit().putInt("user_id", value ?: 0).apply()
            }

        private fun getSharedPreferences(): SharedPreferences =
            MyApplication.instance.getSharedPreferences("MyAppPrefs", MODE_PRIVATE)

        lateinit var instance: MyApplication
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this // Inisialisasi instance untuk akses global
        startKoin {
            androidContext(this@MyApplication)
            modules(
                module {
                    single { RetrofitClient.apiService as ApiService }
                    single { RetrofitClient.menuApiService } // Tambahkan menuApiService ke modul Koin
                    factory { CourierViewModel(get(), get()) } // Perbarui untuk menerima dua dependensi
                }
            )
        }
    }
}