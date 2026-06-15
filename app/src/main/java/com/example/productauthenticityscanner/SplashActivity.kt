package com.example.productauthenticityscanner

import android.os.Bundle
import android.content.Intent
import android.content.SharedPreferences
import android.os.Handler
import android.os.Looper
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat


class SplashActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_splash)

        sharedPreferences = getSharedPreferences("loginPrefs", MODE_PRIVATE)

        Handler(Looper.getMainLooper()).postDelayed({

            val remember = sharedPreferences.getBoolean("remember", false)

            if (remember) {

                val intent = Intent(this, DashboardActivity::class.java)
                startActivity(intent)

            } else {

                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
            }
            finish()

        }, 2000)


    }
}