package com.example.productauthenticityscanner

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import androidx.cardview.widget.CardView
import android.widget.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.bumptech.glide.Glide
import com.google.android.material.navigation.NavigationView


class DashboardActivity : AppCompatActivity() {

    private lateinit var txtLastStatus: TextView
    private lateinit var txtLastProduct: TextView

    private lateinit var txtLastProductDate: TextView

    private lateinit var imgLastProduct: ImageView
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_dashboard)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)

            val bottomNav = findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottomNav)
            bottomNav.setPadding(0,0,0,systemBars.bottom)
            insets
        }


        // latest scan status
        txtLastStatus = findViewById(R.id.txtLastStatus)
        txtLastProduct = findViewById(R.id.txtLastProduct)
        txtLastProductDate = findViewById(R.id.txtLastProductDate)
        imgLastProduct = findViewById(R.id.imgLastProduct)


        db = FirebaseFirestore.getInstance()

        loadLatestScan()

        // top hamburger menu
        val topMenu = findViewById<ImageView>(R.id.topMenu)

        topMenu.setOnClickListener {

            val popup = PopupMenu(this, topMenu)
            popup.menuInflater.inflate(R.menu.drawer_menu, popup.menu)

            popup.setOnMenuItemClickListener {

                when (it.itemId) {

                    R.id.nav_about -> {
                        startActivity(Intent(this, AboutActivity::class.java))
                        true
                    }

                    R.id.nav_privacy -> {
                        startActivity(Intent(this, PrivacyPolicyActivity::class.java))
                        true
                    }

                    else -> false
                }
            }

            popup.show()
        }


        // redirect to notifications page
        val notificatons = findViewById<ImageView>(R.id.notifications)

        notificatons.setOnClickListener {

            val intent = Intent(this@DashboardActivity, NotificationActivity::class.java)
            startActivity(intent)

        }



        // redirect to scan product from scan card
        val scanCard = findViewById<CardView>(R.id.scanCard)

        scanCard.setOnClickListener {

            val intent = Intent(this@DashboardActivity, ScanActivity::class.java)
            startActivity(intent)

        }

        // redirect to scan history from history card
        val scanHistory = findViewById<CardView>(R.id.historyCard)

        scanHistory.setOnClickListener {

            val intent = Intent(this@DashboardActivity, ScanHistoryActivity::class.java)
            startActivity(intent)

        }

        // redirect to fake report page from profile card
        val fakeReportCard = findViewById<CardView>(R.id.reportCard)

        fakeReportCard.setOnClickListener {

            val intent = Intent(this@DashboardActivity, ReportHistoryActivity::class.java)
            startActivity(intent)

        }

        // redirect to analytics page from analytics card
        val analyticsCard = findViewById<CardView>(R.id.analyticsCard)

        analyticsCard.setOnClickListener {

            val intent = Intent(this@DashboardActivity, AnalyticsActivity::class.java)
            startActivity(intent)

        }


        // redirect to profile page from profile card
        val profileCard = findViewById<CardView>(R.id.profileCard)

        profileCard.setOnClickListener {

            val intent = Intent(this@DashboardActivity, ProfileActivity::class.java)
            startActivity(intent)

        }



        // redirect to scan product from bottom scan menu
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)

        bottomNav.setOnItemSelectedListener { item ->

            when(item.itemId)
            {
                R.id.home -> true

                R.id.scan -> {

                    startActivity(Intent(this@DashboardActivity, ScanActivity::class.java))
                    true
                }

                R.id.history -> {
                    startActivity(Intent(this@DashboardActivity, ScanHistoryActivity::class.java))
                    true
                }


                R.id.profile -> {
                    startActivity(Intent(this@DashboardActivity, ProfileActivity::class.java))
                    true
                }


                else -> true
            }

        }

    }

    private fun loadLatestScan() {

        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

        db.collection("scans")
            .whereEqualTo("userId", currentUserId)
            .orderBy("scanTime", Query.Direction.DESCENDING)
            .limit(1)
            .get()
            .addOnSuccessListener { result ->

                if (!result.isEmpty) {

                    val doc = result.documents[0]

                    val status = doc.getString("status") ?: "Unknown"
                    val productId = doc.getString("productId") ?: "Unknown"
                    val imageUrl = doc.getString("productImageUrl")

                    val scanTimestamp = doc.getTimestamp("scanTime")

                    val scanDate = if (scanTimestamp != null) {
                        android.text.format.DateFormat.format(
                            "dd MMM yyyy, hh:mm a",
                            scanTimestamp.toDate()
                        ).toString()
                    } else {
                        "Unknown Date"
                    }

                    txtLastStatus.text = "Last Scan: $status"
                    txtLastProductDate.text = "Scan Date : $scanDate"

                    // 🎨 Status color
                    when (status) {
                        "genuine" -> txtLastStatus.setTextColor(android.graphics.Color.parseColor("#22C55E"))
                        "fake" -> txtLastStatus.setTextColor(android.graphics.Color.parseColor("#EF4444"))
                        "suspicious" -> txtLastStatus.setTextColor(android.graphics.Color.parseColor("#F59E0B"))
                        "already_scanned" -> txtLastStatus.setTextColor(android.graphics.Color.parseColor("#3B82F6"))
                    }

                    // 🖼 Image
                    if (!imageUrl.isNullOrEmpty()) {
                        Glide.with(this)
                            .load(imageUrl)
                            .placeholder(R.drawable.ic_scan)
                            .into(imgLastProduct)
                    } else {
                        imgLastProduct.setImageResource(R.drawable.ic_scan)
                    }

                    // 🔥 FETCH PRODUCT NAME FROM PRODUCTS COLLECTION
                    db.collection("products")
                        .document(productId)
                        .get()
                        .addOnSuccessListener { productDoc ->

                            val productName = productDoc.getString("name") ?: productId
                            txtLastProduct.text = "Product Name : $productName"
                        }
                        .addOnFailureListener {
                            txtLastProduct.text = productId // fallback
                        }

                } else {
                    txtLastStatus.text = "No scans yet"
                    txtLastProduct.text = ""
                    imgLastProduct.setImageResource(R.drawable.ic_scan)
                }
            }
    }
    override fun onResume() {
        super.onResume()
        loadLatestScan()
    }
}