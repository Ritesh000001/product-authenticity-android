package com.example.productauthenticityscanner

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.*
import com.example.productauthenticityscanner.adapter.ScanHistoryAdapter
import com.example.productauthenticityscanner.model.ScanHistory
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.auth.FirebaseAuth

class ScanHistoryActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ScanHistoryAdapter
    private lateinit var db: FirebaseFirestore

    private lateinit var emptyLayout: LinearLayout

    private val historyList = ArrayList<ScanHistory>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan_history)

        val backBtn = findViewById<ImageView>(R.id.backBtn)
        recyclerView = findViewById(R.id.recyclerHistory)

        emptyLayout = findViewById(R.id.emptyLayout)

        recyclerView.layoutManager = LinearLayoutManager(this)

        // 🔥 Firestore init
        db = FirebaseFirestore.getInstance()

        // 🔥 Adapter with empty list
        adapter = ScanHistoryAdapter(historyList)
        recyclerView.adapter = adapter

        // 🔥 Load data
        loadScanHistory()

        backBtn.setOnClickListener {
            finish()
        }
    }

    // 🚀 Fetch data from Firestore
    private fun loadScanHistory() {

        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

        db.collection("scans")
            .whereEqualTo("userId", currentUserId)
            .orderBy("scanTime", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { result ->

                historyList.clear()

                for (doc in result) {
                    val item = doc.toObject(ScanHistory::class.java)
                    historyList.add(item)
                }

                if (historyList.isEmpty()) {
                    emptyLayout.visibility = LinearLayout.VISIBLE
                    recyclerView.visibility = RecyclerView.GONE
                } else {
                    emptyLayout.visibility = LinearLayout.GONE
                    recyclerView.visibility = RecyclerView.VISIBLE
                }

                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                emptyLayout.visibility = LinearLayout.VISIBLE
                recyclerView.visibility = RecyclerView.GONE
            }
    }

    // 🔄 Auto refresh when screen opens again
    override fun onResume() {
        super.onResume()
        loadScanHistory()
    }
}