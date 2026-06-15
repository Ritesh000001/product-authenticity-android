package com.example.productauthenticityscanner

import android.os.Bundle
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.widget.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.GeoPoint

class VerifyScratchActivity : AppCompatActivity() {

    private lateinit var etScratch: EditText
    private lateinit var btnVerify: Button
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verify_scratch)

        etScratch = findViewById(R.id.etScratch)
        btnVerify = findViewById(R.id.btnVerify)
        val backBtn = findViewById<ImageView>(R.id.backBtn)

        db = FirebaseFirestore.getInstance()

        val productId = intent.getStringExtra("productId")
        val realScratch = intent.getStringExtra("realScratch")

        backBtn.setOnClickListener {
            finish()
        }

        btnVerify.setOnClickListener {

            val enteredCode = etScratch.text.toString().trim().uppercase()

            if (enteredCode.isEmpty()) {
                Toast.makeText(this, "Enter scratch code", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (enteredCode == realScratch) {

                // ✅ CORRECT SCRATCH
                db.collection("products").document(productId!!)
                    .update(
                        mapOf(
                            "scratchVerified" to true,
                            "firstScanTime" to System.currentTimeMillis()
                        )
                    )
                    .addOnSuccessListener {

                        // 🔥 FETCH FULL DATA
                        db.collection("products").document(productId)
                            .get()
                            .addOnSuccessListener { doc ->

                                val intent = Intent(this, ProductDetailsActivity::class.java)

                                intent.putExtra("status", "genuine")
                                intent.putExtra("name", doc.getString("name"))
                                intent.putExtra("batchNo", doc.getString("batch"))
                                intent.putExtra("company", doc.getString("companyId"))
                                intent.putExtra("mfgDate", doc.getTimestamp("manufacturingDate")?.toDate().toString())
                                intent.putExtra("expiry", doc.getTimestamp("expiryDate")?.toDate().toString())
                                intent.putExtra("productImageUrl", doc.getString("productImageUrl"))
                                intent.putExtra("productId", productId)
//                                intent.putExtra("serialNumber", doc.getString("serialNumber"))

                                //storing scan history
                                val userId = FirebaseAuth.getInstance().currentUser?.uid

                                val scanData = hashMapOf(
                                    "productId" to productId,
//                                    "serialNumber" to doc.getString("serialNumber"),
                                    "batch" to doc.getString("batch"),
                                    "status" to "genuine",
                                    "scanTime" to FieldValue.serverTimestamp(),
                                    "location" to GeoPoint(28.6139, 77.2090),
                                    "userId" to userId,
                                    "productImageUrl" to doc.getString("productImageUrl")
                                )

                                db.collection("scans").add(scanData)


                                startActivity(intent)
                                finish()
                            }
                    }

            } else {

                // ❌ WRONG SCRATCH → INCREMENT FAILED COUNT
                db.collection("products").document(productId!!)
                    .update("failedCount", FieldValue.increment(1))

                val intent = Intent(this, ProductDetailsActivity::class.java)
                intent.putExtra("status", "fake")


                //storing scan history
                val userId = FirebaseAuth.getInstance().currentUser?.uid

                val scanData = hashMapOf(
                    "productId" to productId,
                    "status" to "fake",
                    "scanTime" to FieldValue.serverTimestamp(),
                    "location" to GeoPoint(28.6139, 77.2090),
                    "userId" to userId,
                    "productImageUrl" to ""
                )

                db.collection("scans").add(scanData)

                startActivity(intent)
                finish()
            }
        }
    }
}