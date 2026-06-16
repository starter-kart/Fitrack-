package com.example.data

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class UserProfile(
    val age: Int = 0,
    val weight: Float = 0f,
    val height: Float = 0f,
    val bmi: Float = 0f,
    val goals: String = "",
    val subscriptionActive: Boolean = false,
    val sedentaryAlertEnabled: Boolean = false,
    val sedentaryAlertThresholdMinutes: Int = 60,
    val waterGoalMl: Int = 2000,
    val waterReminderEnabled: Boolean = false,
    val waterReminderIntervalMinutes: Int = 60
)

data class DailyLog(
    val date: String = "",
    val steps: Int = 0,
    val caloriesBurned: Int = 0,
    val waterIntakeMl: Int = 0
)

class FirestoreRepository {
    private val db = FirebaseFirestore.getInstance()
    
    private fun getTodayDateString(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }

    suspend fun saveProfile(userId: String, profile: UserProfile) {
        db.collection("users").document(userId).collection("profile")
            .document("details").set(profile).await()
    }

    suspend fun getProfile(userId: String): UserProfile? {
        val snapshot = db.collection("users").document(userId).collection("profile")
            .document("details").get().await()
        return snapshot.toObject(UserProfile::class.java)
    }

    suspend fun saveDailyLog(userId: String, log: DailyLog) {
        db.collection("users").document(userId).collection("logs")
            .document(log.date.ifEmpty { getTodayDateString() }).set(log).await()
    }

    suspend fun getDailyLog(userId: String, date: String = getTodayDateString()): DailyLog? {
        val snapshot = db.collection("users").document(userId).collection("logs")
            .document(date).get().await()
        return snapshot.toObject(DailyLog::class.java)
    }

    suspend fun getAllDailyLogs(userId: String): List<DailyLog> {
        val snapshot = db.collection("users").document(userId).collection("logs")
            .get().await()
        return snapshot.toObjects(DailyLog::class.java).sortedByDescending { it.date }
    }
}
