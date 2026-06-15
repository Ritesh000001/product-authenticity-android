package com.example.productauthenticityscanner

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.widget.TextView
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore



class SignupActivity : AppCompatActivity() {

    lateinit var auth: FirebaseAuth
    lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val name = findViewById<EditText>(R.id.etName)
        val email = findViewById<EditText>(R.id.etEmail)
        val phone = findViewById<EditText>(R.id.etPhone)
        val password = findViewById<EditText>(R.id.etPassword)
        val confirmPassword = findViewById<EditText>(R.id.etConfirmPassword)
        val btnSignup = findViewById<Button>(R.id.btnSignup)
        val txtLogin = findViewById<TextView>(R.id.txtLogin)

        btnSignup.setOnClickListener {

            val userName = name.text.toString().trim()
            val userEmail = email.text.toString().trim()
            val userPhone = phone.text.toString().trim()
            val userPassword = password.text.toString().trim()
            val userConfirmPassword = confirmPassword.text.toString().trim()

            if(userName.isEmpty() || userEmail.isEmpty() || userPhone.isEmpty() || userPassword.isEmpty()) {
                Toast.makeText(this,"Please fill all fields",Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if(userPassword != userConfirmPassword){
                Toast.makeText(this,"Passwords do not match",Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Firebase Signup
            auth.createUserWithEmailAndPassword(userEmail,userPassword)
                .addOnCompleteListener {

                    if(it.isSuccessful){

                        val userId = auth.currentUser!!.uid

                        val userMap = hashMapOf(
                            "name" to userName,
                            "email" to userEmail,
                            "phone" to userPhone,
                            "uid" to userId,
                            "totalScans" to 0,
                            "createdAt" to com.google.firebase.Timestamp.now()
                        )

                        db.collection("users")
                            .document(userId)
                            .set(userMap)

                        Toast.makeText(this,"Account Created",Toast.LENGTH_SHORT).show()

                        startActivity(Intent(this, DashboardActivity::class.java))
                        finish()

                    }else{
                        Toast.makeText(this,it.exception?.message,Toast.LENGTH_LONG).show()
                    }
                }
        }

        txtLogin.setOnClickListener{
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }
}