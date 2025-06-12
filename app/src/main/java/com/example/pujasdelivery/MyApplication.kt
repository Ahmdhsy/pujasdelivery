package com.example.pujasdelivery

import android.app.Application
import com.example.pujasdelivery.api.ApiService
import com.example.pujasdelivery.api.RetrofitClient
import com.example.pujasdelivery.viewmodel.CourierViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.dsl.module

class MyApplication : Application() {
    companion object {
        var token: String? = null
        var userId: Int? = null
    }

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@MyApplication)
            modules(
                module {
                    single { RetrofitClient.apiService as ApiService }
                    factory { CourierViewModel(get()) }
                }
            )
        }
    }
}