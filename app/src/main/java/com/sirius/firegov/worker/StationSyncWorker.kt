package com.sirius.firegov.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.google.gson.JsonParser
import com.sirius.firegov.data.model.FireStation
import com.sirius.firegov.data.network.OverpassApiService
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.tasks.await

import android.util.Log

@HiltWorker
class StationSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val firestore: FirebaseFirestore,
    private val overpassApiService: OverpassApiService
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        Log.d("StationSyncWorker", "doWork: starting")
        return try {
            val query = """
                [out:json][timeout:60];
                area["name"="India"]["boundary"="administrative"]->.india;
                (
                  node["amenity"="fire_station"](area.india);
                  way["amenity"="fire_station"](area.india);
                  rel["amenity"="fire_station"](area.india);
                );
                out center body;
            """.trimIndent()

            val responseBody = overpassApiService.getFireStations(query)
            val json = JsonParser.parseString(responseBody.string()).asJsonObject
            val elements = json.getAsJsonArray("elements")
            Log.d("StationSyncWorker", "doWork: found ${elements.size()} elements")

            for (element in elements) {
                val obj = element.asJsonObject
                val lat: Double
                val lon: Double
                
                if (obj.has("lat") && obj.has("lon")) {
                    lat = obj.get("lat").asDouble
                    lon = obj.get("lon").asDouble
                } else if (obj.has("center")) {
                    val center = obj.getAsJsonObject("center")
                    lat = center.get("lat").asDouble
                    lon = center.get("lon").asDouble
                } else {
                    continue
                }

                val tags = obj.getAsJsonObject("tags")
                val name = tags?.get("name")?.asString ?: "Unknown Fire Station"
                val state = tags?.get("addr:state")?.asString ?: "Unknown"
                val city = tags?.get("addr:city")?.asString ?: "Unknown"

                val geoPoint = GeoPoint(lat, lon)
                
                // Simplified station ID allocation for demo
                val existing = firestore.collection("fireStations")
                    .whereGreaterThanOrEqualTo("location", GeoPoint(lat - 0.0001, lon - 0.0001))
                    .whereLessThanOrEqualTo("location", GeoPoint(lat + 0.0001, lon + 0.0001))
                    .get()
                    .await()

                if (existing.isEmpty) {
                    val stateCode = getStateCode(state)
                    val stationId = "FS-$stateCode-${System.currentTimeMillis().toString().takeLast(5)}"
                    
                    val station = FireStation(
                        stationId = stationId,
                        name = name,
                        state = state,
                        city = city,
                        location = geoPoint,
                        addedAt = Timestamp.now()
                    )
                    firestore.collection("fireStations").document(stationId).set(station).await()
                    Log.d("StationSyncWorker", "doWork: added station $stationId - $name")
                }
            }

            firestore.collection("metadata").document("stationList").set(
                mapOf("lastUpdated" to Timestamp.now(), "totalStations" to elements.size())
            ).await()

            Log.d("StationSyncWorker", "doWork: success")
            Result.success()
        } catch (e: Exception) {
            Log.e("StationSyncWorker", "doWork: error", e)
            Result.retry()
        }
    }

    private fun getStateCode(stateName: String): String {
        return when (stateName.lowercase()) {
            "tamil nadu" -> "TN"
            "maharashtra" -> "MH"
            "delhi" -> "DL"
            "karnataka" -> "KA"
            "kerala" -> "KL"
            "andhra pradesh" -> "AP"
            "gujarat" -> "GJ"
            "west bengal" -> "WB"
            "uttar pradesh" -> "UP"
            "rajasthan" -> "RJ"
            "telangana" -> "TS"
            "punjab" -> "PB"
            "haryana" -> "HR"
            "madhya pradesh" -> "MP"
            "bihar" -> "BR"
            "odisha" -> "OD"
            "assam" -> "AS"
            "goa" -> "GA"
            "chhattisgarh" -> "CG"
            "jharkhand" -> "JH"
            "himachal pradesh" -> "HP"
            "uttarakhand" -> "UK"
            "manipur" -> "MN"
            "meghalaya" -> "ML"
            "tripura" -> "TR"
            "mizoram" -> "MZ"
            "nagaland" -> "NL"
            "sikkim" -> "SK"
            "arunachal pradesh" -> "AR"
            "jammu kashmir" -> "JK"
            "ladakh" -> "LA"
            "puducherry" -> "PY"
            "chandigarh" -> "CH"
            else -> "IN"
        }
    }
}
