package com.example.productauthenticityscanner.adapter

import android.content.Intent
import android.view.*
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.productauthenticityscanner.R
import com.example.productauthenticityscanner.FullScreenImageActivity
import com.example.productauthenticityscanner.model.ReportModel

class ReportAdapter(
    private val list: List<ReportModel>
) : RecyclerView.Adapter<ReportAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val txtProductName: TextView = itemView.findViewById(R.id.txtProductName)
        val txtProductId: TextView = itemView.findViewById(R.id.txtProductId)

        val txtStatus: TextView = itemView.findViewById(R.id.txtStatus)

        val txtReason: TextView = itemView.findViewById(R.id.txtReason)
        val txtTime: TextView = itemView.findViewById(R.id.txtTime)
        val txtLocation: TextView = itemView.findViewById(R.id.txtLocation)
        val imgProduct: ImageView = itemView.findViewById(R.id.imgProduct)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_report, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val item = list[position]

        holder.txtProductId.text = "Product ID: ${item.productId}"
        holder.txtStatus.text = "Status: ${item.status}"
        holder.txtProductName.text = "Product Name : ${item.productName}"
        holder.txtReason.text = "Reason : ${item.reason}"



        val time = item.reportTime?.toDate()
        holder.txtTime.text = if (time != null) {
            "Time: " + java.text.SimpleDateFormat(
                "dd MMM yyyy, hh:mm a"
            ).format(time)
        } else {
            "Time: N/A"
        }


        if (item.location != null) {
            holder.txtLocation.text =
                "Location: ${item.location.latitude}, ${item.location.longitude}"
        } else {
            holder.txtLocation.text = "Location: N/A"
        }

        // Status color (mostly fake)
        holder.txtStatus.setTextColor(
            android.graphics.Color.parseColor("#EF4444")
        )

        // Image
        if (!item.productImageUrl.isNullOrEmpty()) {
            Glide.with(holder.itemView.context)
                .load(item.productImageUrl)
                .placeholder(R.drawable.ic_scan)
                .into(holder.imgProduct)

            // Full screen
            holder.imgProduct.setOnClickListener {
                val context = holder.itemView.context
                val intent = Intent(context, FullScreenImageActivity::class.java)
                intent.putExtra("imageUrl", item.productImageUrl)
                context.startActivity(intent)
            }

        } else {
            holder.imgProduct.setImageResource(R.drawable.ic_scan)
        }
    }
}