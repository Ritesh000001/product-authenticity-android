package com.example.productauthenticityscanner.adapter

import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.productauthenticityscanner.FullScreenImageActivity
import com.example.productauthenticityscanner.R
import com.example.productauthenticityscanner.model.ScanHistory
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class ScanHistoryAdapter(
    private val list: List<ScanHistory>
) : RecyclerView.Adapter<ScanHistoryAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val txtProductName: TextView =
            itemView.findViewById(R.id.txtProductName)

        val txtProductId: TextView =
            itemView.findViewById(R.id.txtProductId)

        val txtStatus: TextView =
            itemView.findViewById(R.id.txtStatus)

        val txtTime: TextView =
            itemView.findViewById(R.id.txtTime)

        val txtLocation: TextView =
            itemView.findViewById(R.id.txtLocation)

        val imgProduct: ImageView =
            itemView.findViewById(R.id.imgProduct)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_scan_history, parent, false)

        return ViewHolder(view)
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val item = list[position]

        // 🔹 Product ID
        holder.txtProductId.text = "Product ID: ${item.productId}"

        // 🔹 Batch
        holder.txtProductName.text = "Loading..."

        // 🔥 Fetch product name from products collection
        FirebaseFirestore.getInstance()
            .collection("products")
            .document(item.productId)
            .get()
            .addOnSuccessListener { doc ->

                val productName = doc.getString("name") ?: "Unknown Product"

                holder.txtProductName.text = "Product Name : $productName"
            }

        // 🔹 Status
        holder.txtStatus.text = "Status: ${item.status}"

        // 🔹 Time
        val time = item.scanTime?.toDate()

        holder.txtTime.text =
            if (time != null) {
                "Time: " + SimpleDateFormat(
                    "dd MMM yyyy, hh:mm a",
                    Locale.getDefault()
                ).format(time)
            } else {
                "Time: N/A"
            }

        // 🔹 Location
        if (item.location != null) {

            holder.txtLocation.text =
                "Location: ${item.location.latitude}, ${item.location.longitude}"

        } else {

            holder.txtLocation.text = "Location: N/A"
        }

        // 🎨 Status Colors
        when (item.status) {

            "genuine" ->
                holder.txtStatus.setTextColor(
                    Color.parseColor("#22C55E")
                )

            "fake" ->
                holder.txtStatus.setTextColor(
                    Color.parseColor("#EF4444")
                )

            "suspicious" ->
                holder.txtStatus.setTextColor(
                    Color.parseColor("#F59E0B")
                )

            "already_scanned" ->
                holder.txtStatus.setTextColor(
                    Color.parseColor("#3B82F6")
                )
        }

        // 🖼 Image
        if (item.productImageUrl.isNotEmpty()) {

            Glide.with(holder.itemView.context)
                .load(item.productImageUrl)
                .placeholder(R.drawable.ic_scan)
                .error(R.drawable.ic_scan)
                .into(holder.imgProduct)

            // Fullscreen image
            holder.imgProduct.setOnClickListener {

                val context = holder.itemView.context

                val intent = Intent(
                    context,
                    FullScreenImageActivity::class.java
                )

                intent.putExtra(
                    "imageUrl",
                    item.productImageUrl
                )

                context.startActivity(intent)
            }

        } else {

            holder.imgProduct.setImageResource(R.drawable.ic_scan)
        }
    }
}