package com.example.productauthenticityscanner

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide

class ProductDetailsActivity : AppCompatActivity() {

    private lateinit var txtProductName: TextView
    private lateinit var txtCompany: TextView
    private lateinit var txtStatus: TextView
    private lateinit var txtBatch: TextView
    private lateinit var txtMfg: TextView
    private lateinit var txtExpiry: TextView

    private lateinit var productImage: ImageView

    private lateinit var btnSave: Button
    private lateinit var btnReport: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_details)

        // 🔹 Bind Views
        txtProductName = findViewById(R.id.txtProductName)
        txtCompany = findViewById(R.id.txtCompany)
        txtStatus = findViewById(R.id.txtStatus)
        txtBatch = findViewById(R.id.txtBatch)
        txtMfg = findViewById(R.id.txtMfg)
        txtExpiry = findViewById(R.id.txtExpiry)
        productImage = findViewById(R.id.productImage)

        btnSave = findViewById(R.id.btnSave)
        btnReport = findViewById(R.id.btnReport)

        val backBtn = findViewById<ImageView>(R.id.backBtn)

        // 🔹 Get Data
        val status = intent.getStringExtra("status") ?: ""
        val productId = intent.getStringExtra("productId") ?: ""
//        val serialNumber = intent.getStringExtra("serialNumber") ?: ""
        val name = intent.getStringExtra("name")
        val company = intent.getStringExtra("company")
        val batch = intent.getStringExtra("batchNo")
        val mfg = intent.getStringExtra("mfgDate")
        val expiry = intent.getStringExtra("expiry")
        val imageUrl = intent.getStringExtra("productImageUrl")

        if (!imageUrl.isNullOrEmpty()) {
            Glide.with(this)
                .load(imageUrl)
                .placeholder(R.drawable.ic_scan)
                .into(productImage)
        }

        // 🔹 Set Product Info
        txtProductName.text = name ?: "Unknown Product"
        txtCompany.text = company ?: "Unknown Company"

        txtBatch.text = "Batch No : ${batch ?: "N/A"}"
        txtMfg.text = "Mfg Date : ${mfg ?: "N/A"}"
        txtExpiry.text = "Expiry : ${expiry ?: "N/A"}"

        // 🔥 STATUS LOGIC (UPGRADED)
        when (status) {

            "genuine" -> {
                txtStatus.text = "✅ Genuine Product"
                txtStatus.setTextColor(Color.parseColor("#22C55E"))

                btnSave.visibility = Button.VISIBLE
                btnReport.visibility = Button.GONE
            }

            "fake" -> {
                txtStatus.text = "❌ Fake Product"
                txtStatus.setTextColor(Color.parseColor("#EF4444"))

                btnSave.visibility = Button.GONE
                btnReport.visibility = Button.VISIBLE
            }

            "already_scanned" -> {
                txtStatus.text = "⚠ Already Scanned Product"
                txtStatus.setTextColor(Color.parseColor("#F59E0B"))

                btnSave.visibility = Button.GONE
                btnReport.visibility = Button.VISIBLE
            }

            "suspicious" -> {
                txtStatus.text = "🚨 Suspicious Activity Detected"
                txtStatus.setTextColor(Color.parseColor("#DC2626"))

                btnSave.visibility = Button.GONE
                btnReport.visibility = Button.VISIBLE
            }

            else -> {
                txtStatus.text = "Unknown Status"
                txtStatus.setTextColor(Color.GRAY)

                btnSave.visibility = Button.GONE
                btnReport.visibility = Button.GONE
            }
        }

        // 🔙 Back Button
        backBtn.setOnClickListener {
            finish()
        }

        // 💾 Save Scan (future DB)
        btnSave.setOnClickListener {
            Toast.makeText(this, "Scan Saved", Toast.LENGTH_SHORT).show()
        }

        // 🚨 Report Fake
        btnReport.setOnClickListener {

            val reportIntent = Intent(this, ReportFormActivity::class.java)

            reportIntent.putExtra("productId", productId)
            reportIntent.putExtra("productName", name)
            reportIntent.putExtra("companyId", company)
//            reportIntent.putExtra("serialNumber", serialNumber)
            reportIntent.putExtra("productImageUrl", imageUrl)
            reportIntent.putExtra("status", status)

            startActivity(reportIntent)
        }

    }
}