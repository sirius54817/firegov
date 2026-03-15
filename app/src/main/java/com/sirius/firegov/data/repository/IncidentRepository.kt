package com.sirius.firegov.data.repository

import android.net.Uri
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import com.sirius.firegov.data.model.Incident
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

import android.util.Log

@Singleton
class IncidentRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage
) {
    suspend fun uploadPhotos(uid: String, uris: List<Uri>): List<String> {
        val urls = mutableListOf<String>()
        val timestamp = System.currentTimeMillis()
        for ((index, uri) in uris.withIndex()) {
            try {
                val ref = storage.reference.child("incidents/$uid/$timestamp/photo_$index.jpg")
                ref.putFile(uri).await()
                val url = ref.downloadUrl.await().toString()
                urls.add(url)
            } catch (e: Exception) {
                Log.e("IncidentRepository", "Photo upload failed", e)
            }
        }
        return urls
    }

    suspend fun submitIncident(incident: Incident): Boolean {
        return try {
            val ref = firestore.collection("incidents").document()
            val incidentWithId = incident.copy(
                incidentId = ref.id,
                createdAt = Timestamp.now()
            )
            Log.d("IncidentRepository", "Submitting incident: ${ref.id} by ${incident.reporterEmail}")
            ref.set(incidentWithId).await()
            Log.d("IncidentRepository", "Incident submitted successfully")
            true
        } catch (e: Exception) {
            Log.e("IncidentRepository", "Incident submission failed", e)
            false
        }
    }

    suspend fun getIncidentHistory(uid: String): List<Incident> {
        return try {
            Log.d("IncidentRepository", "Fetching history for uid: $uid")
            val query = firestore.collection("incidents")
                .whereEqualTo("reportedBy", uid)
                .orderBy("createdAt", Query.Direction.DESCENDING)
            
            val result = query.get().await()
            Log.d("IncidentRepository", "History fetched: ${result.size()} documents")
            result.toObjects(Incident::class.java)
        } catch (e: Exception) {
            Log.e("IncidentRepository", "Failed to fetch history", e)
            // If it's a missing index error, try without ordering as a fallback
            if (e.message?.contains("FAILED_PRECONDITION") == true || e.message?.contains("index") == true) {
                Log.w("IncidentRepository", "Attempting fallback fetch without order")
                try {
                    val fallbackResult = firestore.collection("incidents")
                        .whereEqualTo("reportedBy", uid)
                        .get()
                        .await()
                    return fallbackResult.toObjects(Incident::class.java).sortedByDescending { it.createdAt }
                } catch (fallbackEx: Exception) {
                    Log.e("IncidentRepository", "Fallback fetch failed", fallbackEx)
                }
            }
            emptyList()
        }
    }
}
