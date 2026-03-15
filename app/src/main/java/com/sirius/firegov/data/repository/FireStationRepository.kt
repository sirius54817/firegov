package com.sirius.firegov.data.repository

import android.location.Location
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.sirius.firegov.data.model.FireStation
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

import android.util.Log

@Singleton
class FireStationRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    suspend fun getStations(): List<FireStation> {
        return try {
            Log.d("FireStationRepository", "getStations: fetching from firestore")
            val stations = firestore.collection("fireStations")
                .get()
                .await()
                .toObjects(FireStation::class.java)
            Log.d("FireStationRepository", "getStations: found ${stations.size} stations")
            stations
        } catch (e: Exception) {
            Log.e("FireStationRepository", "getStations error", e)
            emptyList()
        }
    }

    suspend fun findNearestStation(userLocation: GeoPoint): Pair<FireStation?, Float?> {
        Log.d("FireStationRepository", "findNearestStation: for $userLocation")
        val stations = getStations()
        if (stations.isEmpty()) {
            Log.d("FireStationRepository", "findNearestStation: no stations found in DB")
            return null to null
        }

        var minDistance = Float.MAX_VALUE
        var nearestStation: FireStation? = null

        val results = FloatArray(1)
        for (station in stations) {
            val stationLoc = station.location ?: continue
            Location.distanceBetween(
                userLocation.latitude, userLocation.longitude,
                stationLoc.latitude, stationLoc.longitude,
                results
            )
            Log.d("FireStationRepository", "findNearestStation: checking ${station.name}, distance: ${results[0]}m")
            if (results[0] < minDistance) {
                minDistance = results[0]
                nearestStation = station
            }
        }
        Log.d("FireStationRepository", "findNearestStation: nearest is ${nearestStation?.name} at ${minDistance}m")
        return nearestStation to if (minDistance == Float.MAX_VALUE) null else minDistance
    }
}
