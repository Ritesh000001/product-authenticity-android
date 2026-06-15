package com.example.productauthenticityscanner

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class ProfileActivity : AppCompatActivity() {

    private lateinit var logoutBtn: LinearLayout

    private lateinit var profileBtn: LinearLayout
    private lateinit var txtName: TextView
    private lateinit var txtEmail: TextView
    private lateinit var profileImage: ImageView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        txtName = findViewById(R.id.txtName)
        txtEmail = findViewById(R.id.txtEmail)
        logoutBtn = findViewById(R.id.btnLogout)
        profileBtn = findViewById(R.id.btnEditProfile)
        profileImage = findViewById(R.id.profileImage)

        // Back button
        findViewById<ImageView>(R.id.backBtn).setOnClickListener {
            finish()
        }

        val userId = FirebaseAuth.getInstance().currentUser?.uid

        if (userId != null) {

            FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener { document ->

                    if (document != null) {

                        val profileUrl = document.getString("profileUrl")

                        if (!profileUrl.isNullOrEmpty()) {

                            Glide.with(this)
                                .load(profileUrl)
                                .circleCrop()
                                .skipMemoryCache(true)
                                .into(profileImage)
                        }

                        val name = document.getString("name")
                        val email = document.getString("email")

                        txtName.text = name
                        txtEmail.text = email

                    }
                }
        }


        // edit profile
        profileBtn.setOnClickListener {

            startActivity(Intent(this, EditProfileActivity::class.java))
            finish()
        }


        // Logout
        logoutBtn.setOnClickListener {

            FirebaseAuth.getInstance().signOut()

            val sharedPreferences = getSharedPreferences("loginPrefs", MODE_PRIVATE)
            sharedPreferences.edit().putBoolean("remember", false).apply()

            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }


}