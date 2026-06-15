package com.example.productauthenticityscanner

import android.graphics.Color
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.*
import com.github.mikephil.charting.data.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.*
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter

class AnalyticsActivity : AppCompatActivity() {

    private lateinit var pieChart: PieChart
    private lateinit var lineChart: LineChart
    private lateinit var barChart: BarChart

    private lateinit var txtTotal: TextView
    private lateinit var txtGenuine: TextView
    private lateinit var txtReports: TextView
    private lateinit var txtSuspicious: TextView

    private val db = FirebaseFirestore.getInstance()

    private var totalScans = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_analytics)

        pieChart = findViewById(R.id.pieChart)
        lineChart = findViewById(R.id.lineChart)
        barChart = findViewById(R.id.barChart)

        txtTotal = findViewById(R.id.txtTotal)
        txtGenuine = findViewById(R.id.txtGenuine)
        txtReports = findViewById(R.id.txtReports)
        txtSuspicious = findViewById(R.id.txtSuspicious)

        findViewById<ImageView>(R.id.backBtn).setOnClickListener { finish() }

        loadAnalytics()
    }

    private fun loadAnalytics() {

        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        db.collection("scans")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { result ->

                var total = 0
                var genuine = 0
                var suspicious = 0
                var fake = 0

                val dateMap = HashMap<String, Int>()

                for (doc in result) {

                    total++

                    val status = doc.getString("status")

                    when (status) {
                        "genuine" -> genuine++
                        "suspicious" -> suspicious++
                        "fake" -> fake++
                    }

                    val time = doc.getTimestamp("scanTime")?.toDate()
                    if (time != null) {
                        val date = SimpleDateFormat("dd MMM", Locale.getDefault()).format(time)
                        dateMap[date] = (dateMap[date] ?: 0) + 1
                    }
                }

                txtTotal.text = "$total\nTotal Scans"
                txtGenuine.text = "$genuine\nGenuine Scans"
                txtSuspicious.text = "$suspicious\nSuspicious Scan"

                totalScans = total

                if (total == 0) {
                    Toast.makeText(this, "No data for analytics", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                setupPieChart(genuine, fake, suspicious)
                setupLineChart(dateMap)
                loadReports(userId)
            }
    }


    private fun loadReports(userId: String) {

        db.collection("reports")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { result ->

                val reports = result.size()

                txtReports.text = "$reports\nScan Reports"

                setupBarChart(totalScans, reports)
            }
    }

    private fun setupPieChart(genuine: Int, fake: Int, suspicious: Int) {

        val entries = ArrayList<PieEntry>()
        entries.add(PieEntry(genuine.toFloat(), "Genuine"))
        entries.add(PieEntry(fake.toFloat(), "Fake"))
        entries.add(PieEntry(suspicious.toFloat(), "Suspicious"))

        entries.removeAll { it.value == 0f }

        val dataSet = PieDataSet(entries, "")

        dataSet.colors = listOf(
            android.graphics.Color.parseColor("#22C55E"),
            android.graphics.Color.parseColor("#EF4444"),
            android.graphics.Color.parseColor("#F59E0B")
        )

        val data = PieData(dataSet)
        data.setValueTextColor(android.graphics.Color.WHITE)
        data.setValueTextSize(14f)

        pieChart.data = data

        pieChart.centerText = "Scan Status"
        pieChart.setCenterTextSize(16f)

        pieChart.isDrawHoleEnabled = true
        pieChart.setHoleColor(Color.TRANSPARENT)

        pieChart.setDrawEntryLabels(true)
        pieChart.setEntryLabelTextSize(12f)
        pieChart.legend.isEnabled = true
        pieChart.legend.textSize = 12f

        // 🔥 DARK MODE FIX
        pieChart.setEntryLabelColor(android.graphics.Color.WHITE)
        pieChart.setCenterTextColor(android.graphics.Color.WHITE)

        pieChart.description.isEnabled = false
        pieChart.isHighlightPerTapEnabled = true
        pieChart.legend.textColor = android.graphics.Color.WHITE

        // 🔥 ANIMATION
        pieChart.animateY(1200)

        pieChart.invalidate()
    }

    private fun setupLineChart(dateMap: HashMap<String, Int>) {

        val entries = ArrayList<Entry>()

        // 🔥 Sort dates for proper graph
        val sdf = SimpleDateFormat("dd MMM", Locale.getDefault())

        val sorted = dateMap.toList().sortedBy {
            try {
                sdf.parse(it.first)
            } catch (e: Exception) {
                Date(0)
            }
        }

        var index = 0f
        for ((_, value) in sorted) {
            entries.add(Entry(index, value.toFloat()))
            index++
        }

        val dataSet = LineDataSet(entries, "Scans Trend")

        dataSet.color = Color.parseColor("#22C55E")
        dataSet.valueTextColor = Color.WHITE
        dataSet.lineWidth = 2.5f
        dataSet.circleRadius = 5f
        dataSet.setCircleColor(Color.WHITE)
        dataSet.setDrawValues(false)

        val data = LineData(dataSet)
        lineChart.data = data

        val labels = ArrayList<String>()
        for ((date, _) in sorted) {
            labels.add(date)
        }

        val xAxis = lineChart.xAxis
        xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        xAxis.granularity = 1f
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
        xAxis.textColor = Color.WHITE

        // 🔥 DARK MODE FIX
        lineChart.xAxis.textColor = Color.WHITE
        lineChart.axisLeft.textColor = Color.WHITE
        lineChart.axisRight.isEnabled = false
        lineChart.legend.textColor = Color.WHITE

        lineChart.axisLeft.setDrawGridLines(false)
        lineChart.xAxis.setDrawGridLines(false)

        lineChart.description.isEnabled = false
        lineChart.isHighlightPerTapEnabled = true

        // 🔥 SMOOTH CURVE
        dataSet.mode = LineDataSet.Mode.CUBIC_BEZIER

        // 🔥 ANIMATION
        lineChart.animateXY(1200, 1200)

        lineChart.invalidate()
    }


    private fun setupBarChart(total: Int, reports: Int) {

        val entries = listOf(
            BarEntry(0f, total.toFloat()),
            BarEntry(1f, reports.toFloat())
        )

        val dataSet = BarDataSet(entries, "")

        dataSet.colors = listOf(
            android.graphics.Color.parseColor("#3B82F6"),
            android.graphics.Color.parseColor("#EF4444")
        )

        val data = BarData(dataSet)
        data.setValueTextColor(android.graphics.Color.WHITE)

        data.barWidth = 0.4f
        barChart.setFitBars(true)

        barChart.data = data

        val xAxis = barChart.xAxis
        xAxis.valueFormatter = IndexAxisValueFormatter(listOf("Scans", "Reports"))
        xAxis.granularity = 1f
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
        xAxis.textColor = Color.WHITE

        // 🔥 DARK MODE FIX
        barChart.xAxis.textColor = android.graphics.Color.WHITE
        barChart.axisLeft.textColor = android.graphics.Color.WHITE
        barChart.axisRight.isEnabled = false
        barChart.legend.textColor = android.graphics.Color.WHITE

        barChart.axisLeft.setDrawGridLines(false)
        barChart.xAxis.setDrawGridLines(false)

        barChart.description.isEnabled = false

        // 🔥 ANIMATION
        barChart.animateXY(1200, 1200)

        barChart.invalidate()
    }
}