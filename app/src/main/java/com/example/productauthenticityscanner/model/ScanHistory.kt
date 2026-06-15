package com.example.productauthenticityscanner.model

data class ScanHistory(
    val productId: String = "",
    val batch: String = "",
    val status: String = "",
    val scanTime: com.google.firebase.Timestamp? = null,
    val location: com.google.firebase.firestore.GeoPoint? = null,
    val productImageUrl: String = ""
)