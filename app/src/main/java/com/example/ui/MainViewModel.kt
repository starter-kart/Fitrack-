package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AuthManager
import com.example.data.DailyLog
import com.example.data.FirestoreRepository
import com.example.data.UserProfile
import com.example.data.BmiRecord
import com.example.network.GeminiHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class AppState(
    val isAuthenticated: Boolean = false,
    val isLoading: Boolean = false,
    val profile: UserProfile = UserProfile(),
    val todayLog: DailyLog = DailyLog(),
    val dailyLogs: List<DailyLog> = emptyList(),
    val bmiHistory: List<BmiRecord> = emptyList(),
    val aiInsights: String? = null,
    val errorMessage: String? = null
)

class MainViewModel(
    private val authManager: AuthManager,
    private val firestoreRepo: FirestoreRepository,
    private val geminiHelper: GeminiHelper
) : ViewModel() {

    private val _state = MutableStateFlow(AppState())
    val state: StateFlow<AppState> = _state.asStateFlow()

    init {
        checkAuthStatus()
    }

    private fun checkAuthStatus() {
        val user = authManager.currentUser
        if (user != null) {
            _state.update { it.copy(isAuthenticated = true, isLoading = false) }
            fetchData(user.uid)
        } else {
            _state.update { it.copy(isAuthenticated = false, isLoading = false) }
        }
    }

    fun signInWithEmail(email: String, javaStringPassword: String) {
        _state.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            val result = authManager.signInWithEmail(email, javaStringPassword)
            if (result.isSuccess) {
                val authResult = result.getOrNull()
                if (authResult?.user != null) {
                    _state.update { it.copy(isAuthenticated = true, isLoading = false) }
                    fetchData(authResult.user!!.uid)
                } else {
                    _state.update { it.copy(isLoading = false, errorMessage = "Sign in failed: no user returned.") }
                }
            } else {
                val error = result.exceptionOrNull()
                _state.update { it.copy(isLoading = false, errorMessage = "Sign in failed: ${error?.message}") }
            }
        }
    }

    fun signUpWithEmail(email: String, javaStringPassword: String) {
        _state.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            val result = authManager.signUpWithEmail(email, javaStringPassword)
            if (result.isSuccess) {
                val authResult = result.getOrNull()
                if (authResult?.user != null) {
                    _state.update { it.copy(isAuthenticated = true, isLoading = false) }
                    fetchData(authResult.user!!.uid)
                } else {
                    _state.update { it.copy(isLoading = false, errorMessage = "Registration failed: no user returned.") }
                }
            } else {
                val error = result.exceptionOrNull()
                _state.update { it.copy(isLoading = false, errorMessage = "Registration failed: ${error?.message}") }
            }
        }
    }

    fun signInAsGuest() {
        _state.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            val result = authManager.signInAnonymously()
            if (result.isSuccess) {
                val authResult = result.getOrNull()
                if (authResult?.user != null) {
                    _state.update { it.copy(isAuthenticated = true, isLoading = false) }
                    fetchData(authResult.user!!.uid)
                } else {
                    _state.update { it.copy(isLoading = false, errorMessage = "Guest sign in failed: no user returned.") }
                }
            } else {
                val error = result.exceptionOrNull()
                _state.update { it.copy(isLoading = false, errorMessage = "Guest sign in failed: ${error?.message}") }
            }
        }
    }

    fun signOut() {
        authManager.signOut()
        _state.update { AppState(isAuthenticated = false) }
    }

    private fun fetchData(userId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                val profile = firestoreRepo.getProfile(userId) ?: UserProfile()
                val todayLog = firestoreRepo.getDailyLog(userId) ?: DailyLog()
                val dailyLogs = firestoreRepo.getAllDailyLogs(userId)
                val bmiHistory = firestoreRepo.getBmiHistory(userId)
                _state.update { it.copy(
                    profile = profile, 
                    todayLog = todayLog, 
                    dailyLogs = dailyLogs, 
                    bmiHistory = bmiHistory,
                    isLoading = false
                ) }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, errorMessage = "Failed to load data.") }
            }
        }
    }

    fun updateProfile(age: Int, height: Float, weight: Float, goals: String) {
        val userId = authManager.currentUser?.uid ?: return
        val bmi = if (height > 0) weight / ((height / 100) * (height / 100)) else 0f
        
        val newProfile = _state.value.profile.copy(age = age, height = height, weight = weight, goals = goals, bmi = bmi)
        _state.update { it.copy(profile = newProfile, isLoading = true) }
        
        viewModelScope.launch {
            try {
                firestoreRepo.saveProfile(userId, newProfile)
                
                // Save calculation history
                val todayStr = SimpleDateFormat("EEEE, d MMMM yyyy, HH:mm", Locale.getDefault()).format(Date())
                val category = when {
                    bmi < 18.5f -> "Underweight"
                    bmi < 25f -> "Normal"
                    bmi < 30f -> "Overweight"
                    else -> "Obese"
                }
                val record = BmiRecord(
                    id = "",
                    timestamp = System.currentTimeMillis(),
                    dateString = todayStr,
                    height = height,
                    weight = weight,
                    bmi = bmi,
                    category = category
                )
                firestoreRepo.saveBmiRecord(userId, record)
                
                // Fetch updated history
                val bmiHistory = firestoreRepo.getBmiHistory(userId)
                _state.update { it.copy(bmiHistory = bmiHistory, isLoading = false) }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, errorMessage = "Failed to save profile.") }
            }
        }
    }

    fun updateDailyLog(steps: Int, calories: Int) {
        val userId = authManager.currentUser?.uid ?: return
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val newLog = _state.value.todayLog.copy(date = todayStr, steps = steps, caloriesBurned = calories)
        _state.update { it.copy(todayLog = newLog, isLoading = true) }
        
        viewModelScope.launch {
            try {
                firestoreRepo.saveDailyLog(userId, newLog)
                val dailyLogs = firestoreRepo.getAllDailyLogs(userId)
                _state.update { it.copy(dailyLogs = dailyLogs, isLoading = false) }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, errorMessage = "Failed to save log.") }
            }
        }
    }

    fun addStepsAutomatically(stepsToAdd: Int) {
        val userId = authManager.currentUser?.uid ?: return
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val currentLog = _state.value.todayLog
        val updatedSteps = currentLog.steps + stepsToAdd
        // Calculate calories based on updated steps: 0.04 calories per step
        val updatedCalories = (updatedSteps * 0.04f).toInt()
        
        val newLog = currentLog.copy(
            date = todayStr, 
            steps = updatedSteps, 
            caloriesBurned = updatedCalories
        )
        
        _state.update { it.copy(todayLog = newLog) }
        
        viewModelScope.launch {
            try {
                firestoreRepo.saveDailyLog(userId, newLog)
                val dailyLogs = firestoreRepo.getAllDailyLogs(userId)
                _state.update { it.copy(dailyLogs = dailyLogs) }
            } catch (e: Exception) {
                // Ignore silent errors for real-time sensor updates
            }
        }
    }

    fun fetchInsights() {
        _state.update { it.copy(isLoading = true, aiInsights = null) }
        viewModelScope.launch {
            val prof = _state.value.profile
            val log = _state.value.todayLog
            val profStr = "Age: ${prof.age}, Height: ${prof.height}cm, Weight: ${prof.weight}kg, BMI: ${prof.bmi}, Goals: ${prof.goals}"
            val logStr = "Steps: ${log.steps}, Calories Burned: ${log.caloriesBurned}"
            
            val insights = geminiHelper.getInsights(profStr, logStr)
            _state.update { it.copy(aiInsights = insights, isLoading = false) }
        }
    }

    fun activateSubscription() {
        val userId = authManager.currentUser?.uid ?: return
        val updatedProfile = _state.value.profile.copy(subscriptionActive = true)
        _state.update { it.copy(profile = updatedProfile) }
        viewModelScope.launch {
            firestoreRepo.saveProfile(userId, updatedProfile)
        }
    }

    fun updateSubscriptionSettings(
        sedentaryAlertEnabled: Boolean,
        sedentaryAlertMinutes: Int,
        waterGoalMl: Int,
        waterReminderEnabled: Boolean,
        waterReminderMinutes: Int
    ) {
        val userId = authManager.currentUser?.uid ?: return
        val currentProfile = _state.value.profile
        val updatedProfile = currentProfile.copy(
            sedentaryAlertEnabled = sedentaryAlertEnabled,
            sedentaryAlertThresholdMinutes = sedentaryAlertMinutes,
            waterGoalMl = waterGoalMl,
            waterReminderEnabled = waterReminderEnabled,
            waterReminderIntervalMinutes = waterReminderMinutes
        )
        _state.update { it.copy(profile = updatedProfile, isLoading = true) }
        viewModelScope.launch {
            try {
                firestoreRepo.saveProfile(userId, updatedProfile)
                _state.update { it.copy(isLoading = false) }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, errorMessage = "Failed to save subscription settings.") }
            }
        }
    }

    fun addWaterIntake(amountMl: Int) {
        val userId = authManager.currentUser?.uid ?: return
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val currentLog = _state.value.todayLog
        val updatedWater = currentLog.waterIntakeMl + amountMl
        val newLog = currentLog.copy(date = todayStr, waterIntakeMl = updatedWater)
        _state.update { it.copy(todayLog = newLog) }
        viewModelScope.launch {
            try {
                firestoreRepo.saveDailyLog(userId, newLog)
                val dailyLogs = firestoreRepo.getAllDailyLogs(userId)
                _state.update { it.copy(dailyLogs = dailyLogs) }
            } catch (e: Exception) {
                // Silent catch
            }
        }
    }

    fun resetWaterIntake() {
        val userId = authManager.currentUser?.uid ?: return
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val currentLog = _state.value.todayLog
        val newLog = currentLog.copy(date = todayStr, waterIntakeMl = 0)
        _state.update { it.copy(todayLog = newLog) }
        viewModelScope.launch {
            try {
                firestoreRepo.saveDailyLog(userId, newLog)
                val dailyLogs = firestoreRepo.getAllDailyLogs(userId)
                _state.update { it.copy(dailyLogs = dailyLogs) }
            } catch (e: Exception) {
                // Silent catch
            }
        }
    }

    fun clearError() {
        _state.update { it.copy(errorMessage = null) }
    }
}
