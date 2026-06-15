package com.example.productauthenticityscanner

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.journeyapps.barcodescanner.DecoratedBarcodeView
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.zxing.*
import com.google.zxing.common.HybridBinarizer
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.GeoPoint

class ScanActivity : AppCompatActivity() {

    private lateinit var barcodeView: DecoratedBarcodeView
    private lateinit var btnFlash: ImageView
    private lateinit var btnGallery: ImageView

    private var isFlashOn = false
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan)

        db = FirebaseFirestore.getInstance()

        // Camera Permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                101
            )
        }

        barcodeView = findViewById(R.id.barcodeScanner)
        btnFlash = findViewById(R.id.btnFlash)
        btnGallery = findViewById(R.id.btnGallery)
        val backBtn = findViewById<ImageView>(R.id.backBtn)

        // 🔥 LIVE SCAN
        barcodeView.decodeContinuous { result ->
            if (result.text != null) {
                barcodeView.pause()

                val qrData = result.text.trim()
                Toast.makeText(this, "ID: $qrData", Toast.LENGTH_SHORT).show()

                verifyProduct(qrData)
            }
        }

        // 🔦 FLASH
        btnFlash.setOnClickListener {
            if (isFlashOn) {
                barcodeView.setTorchOff()
                isFlashOn = false
            } else {
                barcodeView.setTorchOn()
                isFlashOn = true
            }
        }

        // 🖼 GALLERY
        btnGallery.setOnClickListener {
            barcodeView.pause()

            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 100)
        }

        // 🔙 BACK
        backBtn.setOnClickListener {
            finish()
        }
    }

    // 🔥 MAIN LOGIC (UPDATED)
    private fun verifyProduct(qrData: String) {

        val productRef = db.collection("products").document(qrData)

        db.runTransaction { transaction ->

            val snapshot = transaction.get(productRef)

            if (!snapshot.exists()) {
                throw Exception("NOT_FOUND")
            }

            val currentScanCount = snapshot.getLong("scanCount") ?: 0
            val newScanCount = currentScanCount + 1

            // ✅ ALWAYS INCREMENT SCAN COUNT
            transaction.update(productRef, mapOf(
                "scanCount" to newScanCount,
                "lastScanTime" to System.currentTimeMillis()
            ))

            HashMap<String, Any>().apply {
                put("scanCount", newScanCount)
                put("scratchCode", snapshot.getString("scratchCode") ?: "")
                put("scratchVerified", snapshot.getBoolean("scratchVerified") ?: false)
                put("name", snapshot.getString("name") ?: "")
                put("batch", snapshot.getString("batch") ?: "")
                put("companyId", snapshot.getString("companyId") ?: "")
                put("mfgDate", snapshot.getTimestamp("manufacturingDate")?.toDate()?.toString() ?: "")
                put("expiryDate", snapshot.getTimestamp("expiryDate")?.toDate()?.toString() ?: "")
                put("productImageUrl", snapshot.getString("productImageUrl") ?: "")
            }

        }.addOnSuccessListener { data ->

            val scanCount = data["scanCount"] as Long
            val scratchCode = data["scratchCode"] as String
            val isVerified = data["scratchVerified"] as Boolean

            val name = data["name"] as String
            val batch = data["batch"] as String
            val company = data["companyId"] as String
            val mfg = data["mfgDate"] as String
            val expiry = data["expiryDate"] as String
            val imageUrl = data["productImageUrl"] as String

            if (scanCount == 1L) {

                // ✅ FIRST SCAN → SCRATCH
                val intent = Intent(this, VerifyScratchActivity::class.java)

//                val serialNumber = data["serialNumber"] as? String ?: ""

//                intent.putExtra("serialNumber", serialNumber)
                intent.putExtra("productId", qrData)
                intent.putExtra("realScratch", scratchCode)
                intent.putExtra("productImageUrl", imageUrl)

                startActivity(intent)
                finish()

            } else {

                val intent = Intent(this, ProductDetailsActivity::class.java)

//                val serialNumber = data["serialNumber"] as? String ?: ""

                if (isVerified) {
                    intent.putExtra("status", "already_scanned")
                } else {
                    intent.putExtra("status", "suspicious")
                }

                intent.putExtra("productId", qrData)
//                intent.putExtra("serialNumber", serialNumber)

                intent.putExtra("name", name)
                intent.putExtra("batchNo", batch)
                intent.putExtra("company", company)
                intent.putExtra("mfgDate", mfg)
                intent.putExtra("expiry", expiry)
                intent.putExtra("productImageUrl", imageUrl)


                //storing scan history
                val userId = FirebaseAuth.getInstance().currentUser?.uid

//                val serialNumber = data["serialNumber"] as? String ?: ""
//                val imageUrl = data["productImageUrl"] as? String ?: ""

                val statusValue = if (isVerified) "already_scanned" else "suspicious"

                val scanData = hashMapOf(
                    "productId" to qrData,
//                    "serialNumber" to serialNumber,
                    "batch" to batch,
                    "status" to statusValue,
                    "scanTime" to FieldValue.serverTimestamp(),
                    "location" to GeoPoint(28.6139, 77.2090),
                    "userId" to userId,
                    "productImageUrl" to imageUrl
                )

                db.collection("scans").add(scanData)


                startActivity(intent)
                finish()
            }

        }.addOnFailureListener {

            if (it.message == "NOT_FOUND") {

                val intent = Intent(this, ProductDetailsActivity::class.java)
                intent.putExtra("status", "fake")
                startActivity(intent)
                finish()

            } else {
                Toast.makeText(this, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        barcodeView.resume()
    }

    override fun onPause() {
        super.onPause()
        barcodeView.pause()
    }

    // 🖼 GALLERY QR SCAN
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 100 && resultCode == RESULT_OK) {

            val imageUri = data?.data

            try {
                val bitmap: Bitmap =
                    MediaStore.Images.Media.getBitmap(contentResolver, imageUri)

                val width = bitmap.width
                val height = bitmap.height
                val pixels = IntArray(width * height)

                bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

                val source = RGBLuminanceSource(width, height, pixels)
                val binaryBitmap = BinaryBitmap(HybridBinarizer(source))

                val result = MultiFormatReader().decode(binaryBitmap)

                val qrText = result.text.trim()

                Toast.makeText(this, "ID: $qrText", Toast.LENGTH_SHORT).show()

                verifyProduct(qrText)

            } catch (e: Exception) {
                Toast.makeText(this, "No QR found!", Toast.LENGTH_SHORT).show()
                barcodeView.resume()
            }
        }
    }
}