package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.data.AuthManager
import com.example.data.FirestoreRepository
import com.example.network.GeminiHelper

class MainViewModelFactory(
    private val authManager: AuthManager,
    private val firestoreRepository: FirestoreRepository,
    private val geminiHelper: GeminiHelper
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(authManager, firestoreRepository, geminiHelper) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
