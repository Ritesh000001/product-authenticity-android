package com.example.productauthenticityscanner.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.GeoPoint

data class ReportModel(
    val productId: String = "",
    val productName: String = "",
    val status: String = "",
    val reason: String = "",
    val reportTime: Timestamp? = null,
    val location: GeoPoint? = null,
    val userId: String = "",
    val productImageUrl: String = ""
)