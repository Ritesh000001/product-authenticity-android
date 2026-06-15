package com.example.productauthenticityscanner

import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


class EditProfileActivity : AppCompatActivity() {

    private lateinit var profileImage: ImageView
    private lateinit var edtName: EditText
    private lateinit var edtEmail: EditText
    private lateinit var edtMobile: EditText
    private lateinit var edtAddress: EditText
    private lateinit var btnSave: Button

    private var imageUri: Uri? = null

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val imagePicker =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri != null) {
                imageUri = uri
                profileImage.setImageURI(uri)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        profileImage = findViewById(R.id.profileImage)
        edtName = findViewById(R.id.etName)
        edtEmail = findViewById(R.id.etEmail)
        edtMobile = findViewById(R.id.etPhone)
        edtAddress = findViewById(R.id.etAddress)
        btnSave = findViewById(R.id.btnSave)

        findViewById<ImageView>(R.id.backBtn).setOnClickListener {
            finish()
        }

        // 🔥 Cloudinary Config
        val config = HashMap<String, String>()
        config["cloud_name"] = "drmm6wqp2"

        try {
            MediaManager.get()
        } catch (e: Exception) {
            MediaManager.init(this, config)
        }

        loadProfile()

        profileImage.setOnClickListener {
            imagePicker.launch("image/*")
        }

        findViewById<ImageView>(R.id.editImageBtn).setOnClickListener {
            imagePicker.launch("image/*")
        }

        btnSave.setOnClickListener {
            uploadAndSave()
        }
    }

    private fun loadProfile() {
        val user = auth.currentUser ?: return
        val userId = user.uid

        edtEmail.setText(user.email ?: "")

        db.collection("users")
            .document(userId)
            .get()
            .addOnSuccessListener { doc ->

                if (doc.exists()) {
                    edtName.setText(doc.getString("name") ?: user.displayName ?: "")
                    edtMobile.setText(doc.getString("mobile") ?: "")
                    edtAddress.setText(doc.getString("address") ?: "")

                    val imageUrl = doc.getString("profileUrl")

                    if (!imageUrl.isNullOrEmpty()) {
                        Glide.with(this)
                            .load(imageUrl)
                            .circleCrop()
                            .skipMemoryCache(true)
                            .into(profileImage)
                    }
                } else {
                    edtName.setText(user.displayName ?: "")
                }
            }
    }

    private fun uploadAndSave() {

        if (imageUri != null) {

            MediaManager.get().upload(imageUri)
                .unsigned("profile_upload")   // ✅ ADD HERE
                .callback(object : UploadCallback {

                    override fun onStart(requestId: String?) {
                        btnSave.isEnabled = false
                        btnSave.text = "Uploading..."
                    }

                    override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {}

                    override fun onSuccess(requestId: String?, resultData: Map<*, *>?) {
                        btnSave.isEnabled = true
                        btnSave.text = "Save Changes"

                        val imageUrl = resultData?.get("secure_url").toString()
                        saveProfile(imageUrl)
                    }

                    override fun onError(requestId: String?, error: ErrorInfo?) {
                        btnSave.isEnabled = true
                        btnSave.text = "Save Changes"

                        Toast.makeText(this@EditProfileActivity, "Upload Failed", Toast.LENGTH_SHORT).show()
                    }

                    override fun onReschedule(requestId: String?, error: ErrorInfo?) {}
                })
                .dispatch()

        } else {
            saveProfile()
        }
    }

    private fun saveProfile(imageUrl: String? = null) {

        val userId = auth.currentUser?.uid ?: return

        if (edtMobile.text.toString().trim().isEmpty() ||
            edtAddress.text.toString().trim().isEmpty()) {
            Toast.makeText(this, "Fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        val map = HashMap<String, Any>()
        map["name"] = edtName.text.toString()
        map["mobile"] = edtMobile.text.toString()
        map["address"] = edtAddress.text.toString()

        map["profileUrl"] = imageUrl ?: ""


        db.collection("users")
            .document(userId)
            .set(map, com.google.firebase.firestore.SetOptions.merge())
            .addOnSuccessListener {
                Toast.makeText(this, "Profile Updated", Toast.LENGTH_SHORT).show()
                finish()
            }
    }
}