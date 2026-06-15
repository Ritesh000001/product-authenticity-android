package com.example.productauthenticityscanner

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.*
import com.example.productauthenticityscanner.adapter.ReportAdapter
import com.example.productauthenticityscanner.model.ReportModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class ReportHistoryActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ReportAdapter
    private lateinit var emptyLayout: LinearLayout

    private val list = ArrayList<ReportModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report_history)

        val backBtn = findViewById<ImageView>(R.id.backBtn)
        recyclerView = findViewById(R.id.recyclerHistory)
        emptyLayout = findViewById(R.id.emptyLayout)

        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = ReportAdapter(list)
        recyclerView.adapter = adapter

        loadReports()

        backBtn.setOnClickListener {
            finish()
        }
    }

    private fun loadReports() {

        val userId = FirebaseAuth.getInstance().currentUser?.uid

        if (userId == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        FirebaseFirestore.getInstance()
            .collection("reports")
            .whereEqualTo("userId", userId)
            .orderBy("reportTime", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { result ->

                list.clear()

                for (doc in result) {
                    val item = doc.toObject(ReportModel::class.java)
                    list.add(item)
                }

                adapter.notifyDataSetChanged()

                // 🔥 EMPTY STATE
                if (list.isEmpty()) {
                    emptyLayout.visibility = View.VISIBLE
                    recyclerView.visibility = View.GONE
                } else {
                    emptyLayout.visibility = View.GONE
                    recyclerView.visibility = View.VISIBLE
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error: ${it.message}", Toast.LENGTH_LONG).show()
            }
    }

    override fun onResume() {
        super.onResume()
        loadReports()
    }
}