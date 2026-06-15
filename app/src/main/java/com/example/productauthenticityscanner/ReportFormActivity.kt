package com.example.productauthenticityscanner

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ReportFormActivity : AppCompatActivity() {

    private lateinit var etReason: EditText
    private lateinit var btnSubmit: Button
    private lateinit var db: FirebaseFirestore

    private lateinit var txtProductName: TextView
    private lateinit var txtProductId: TextView
    private lateinit var txtManufacturerId: TextView
    private lateinit var txtSerial: TextView
    private lateinit var txtStatus: TextView
    private lateinit var imgProduct: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report_form)

        db = FirebaseFirestore.getInstance()

        etReason = findViewById(R.id.etReason)
        btnSubmit = findViewById(R.id.btnSubmit)

        txtProductName = findViewById(R.id.txtProductName)
        txtProductId = findViewById(R.id.txtProductId)
        txtManufacturerId = findViewById(R.id.txtManufacturerId)
        txtSerial = findViewById(R.id.txtSerial)
        txtStatus = findViewById(R.id.txtStatus)
        imgProduct = findViewById(R.id.imgProduct)

        val backBtn = findViewById<ImageView>(R.id.backBtn)

        // 🔥 Get data
        val productId = intent.getStringExtra("productId") ?: ""
        val productName = intent.getStringExtra("productName") ?: ""
        val manufacturerId = intent.getStringExtra("manufacturerId") ?: ""
        val serialNumber = intent.getStringExtra("serialNumber") ?: ""
        val imageUrl = intent.getStringExtra("productImageUrl") ?: ""
        val status = intent.getStringExtra("status") ?: "unknown"


        when(status){
            "fake" -> txtStatus.setTextColor(android.graphics.Color.RED)
            "already_scanned" -> txtStatus.setTextColor(android.graphics.Color.YELLOW)
            "suspicious" -> txtStatus.setTextColor(android.graphics.Color.parseColor("#F97316"))
        }

        // ✅ AUTO FILL UI
        txtProductName.text = productName
        txtProductId.text = "Product ID: $productId"
        txtManufacturerId.text = "Manufacturer ID: $manufacturerId"
        txtSerial.text = "Serial: $serialNumber"
        txtStatus.text = "Status: $status"

        Glide.with(this)
            .load(imageUrl)
            .placeholder(R.drawable.ic_scan)
            .into(imgProduct)

        Toast.makeText(this, "ID: $productId", Toast.LENGTH_SHORT).show()

        backBtn.setOnClickListener {
            finish()
        }

        btnSubmit.setOnClickListener {

            val reason = etReason.text.toString().trim()

            if (reason.isEmpty()) {
                Toast.makeText(this, "Enter reason", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

            val reportData = hashMapOf(
                "productId" to productId,
                "productName" to productName,
                "manufacturerId" to manufacturerId,
                "serialNumber" to serialNumber,
                "productImageUrl" to imageUrl,
                "reason" to reason,
                "status" to status,
                "userId" to userId,
                "reportTime" to Timestamp.now()
            )

            db.collection("reports")
                .add(reportData)
                .addOnSuccessListener {
                    Toast.makeText(this, "Report submitted", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}