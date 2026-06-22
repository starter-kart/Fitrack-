package com.example.data

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class UserProfile(
    var age: Int = 0,
    var weight: Float = 0f,
    var height: Float = 0f,
    var bmi: Float = 0f,
    var goals: String = "",
    @get:com.google.firebase.firestore.PropertyName("subscriptionActive")
    @set:com.google.firebase.firestore.PropertyName("subscriptionActive")
    var subscriptionActive: Boolean = false,
    var sedentaryAlertEnabled: Boolean = false,
    var sedentaryAlertThresholdMinutes: Int = 60,
    var waterGoalMl: Int = 2000,
    var waterReminderEnabled: Boolean = false,
    var waterReminderIntervalMinutes: Int = 60
)

data class DailyLog(
    var date: String = "",
    var steps: Int = 0,
    var caloriesBurned: Int = 0,
    var waterIntakeMl: Int = 0
)

data class BmiRecord(
    var id: String = "",
    var timestamp: Long = 0L,
    var dateString: String = "",
    var height: Float = 0f,
    var weight: Float = 0f,
    var bmi: Float = 0f,
    var category: String = ""
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

    suspend fun saveBmiRecord(userId: String, record: BmiRecord) {
        val docRef = if (record.id.isEmpty()) {
            db.collection("users").document(userId).collection("bmi_history").document()
        } else {
            db.collection("users").document(userId).collection("bmi_history").document(record.id)
        }
        val finalRecord = record.copy(id = docRef.id)
        docRef.set(finalRecord).await()
    }

    suspend fun getBmiHistory(userId: String): List<BmiRecord> {
        val snapshot = db.collection("users").document(userId).collection("bmi_history")
            .get().await()
        return snapshot.toObjects(BmiRecord::class.java).sortedByDescending { it.timestamp }
    }
}
