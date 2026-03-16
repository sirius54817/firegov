package com.sirius.firegov.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.GeoPoint

data class User(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val role: String = "citizen",
    val createdAt: Timestamp? = null
)

data class Incident(
    val incidentId: String = "",
    val reportedBy: String = "",
    val reporterName: String = "",
    val reporterEmail: String = "",
    val location: GeoPoint? = null,
    val address: String = "",
    val photoUrls: List<String> = emptyList(),
    val description: String = "",
    val status: String = "pending", // pending, responding, resolved
    val assignedStation: String? = null,
    val nearestStationDistance: Double? = null,
    val createdAt: Timestamp? = null
)

data class FireStation(
    val stationId: String = "",
    val name: String = "",
    val state: String = "",
    val city: String = "",
    val location: GeoPoint? = null,
    val addedAt: Timestamp? = null,
    val lastVerified: Timestamp? = null
)

data class NewsArticle(
    val title: String,
    val link: String,
    val description: String?,
    val source_id: String?,
    val pubDate: String?,
    val image_url: String?
)

data class NewsResponse(
    val status: String,
    val results: List<NewsArticle>,
    val nextPage: String? = null
)
