package com.sirius.firegov.data.repository

import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.sirius.firegov.data.model.User
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {
    val currentUserUid: String?
        get() = auth.currentUser?.uid

    val currentUser
        get() = auth.currentUser

    suspend fun signInWithGoogle(idToken: String): String? {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        val result = auth.signInWithCredential(credential).await()
        return result.user?.uid
    }

    suspend fun getUserRole(uid: String): String? {
        Log.d("AuthRepository", "getUserRole: uid = $uid")
        return try {
            withTimeoutOrNull(5000) {
                firestore.collection("users").document(uid).get().await().getString("role")
            }
        } catch (e: Exception) {
            Log.e("AuthRepository", "getUserRole error", e)
            null
        }
    }

    suspend fun saveUser(uid: String, name: String, email: String, phoneNumber: String) {
        val user = User(
            uid = uid,
            name = name,
            email = email,
            phoneNumber = phoneNumber,
            role = "citizen",
            createdAt = Timestamp.now()
        )
        firestore.collection("users").document(uid).set(user).await()
    }

    suspend fun getUser(uid: String): User? {
        return try {
            firestore.collection("users").document(uid).get().await().toObject(User::class.java)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun updateProfile(uid: String, name: String, phoneNumber: String): Boolean {
        return try {
            firestore.collection("users").document(uid).update(
                mapOf(
                    "name" to name,
                    "phoneNumber" to phoneNumber
                )
            ).await()
            true
        } catch (e: Exception) {
            false
        }
    }
}
