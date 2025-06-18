package com.example.basicapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.basicapp.model.AdminSecurityRequest
import com.example.basicapp.model.SecurityUser
import com.example.basicapp.model.VerificationRecord
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.example.basicapp.api.RetrofitInstance

class AdminViewModel : ViewModel() {

    private val _securityUsers = MutableStateFlow<List<SecurityUser>>(emptyList())
    val securityUsers: StateFlow<List<SecurityUser>> = _securityUsers.asStateFlow()

    private val _records = MutableStateFlow<List<VerificationRecord>>(emptyList())
    val records: StateFlow<List<VerificationRecord>> = _records.asStateFlow()

    // Fetch all security users
    fun fetchSecurityUsers() {
        viewModelScope.launch {
            try {
                val users = RetrofitInstance.api.getSecurityUsers()
                _securityUsers.value = users
            } catch (e: Exception) {
                // Handle network errors
                e.printStackTrace()
            }
        }
    }

    // Add a new security user
    fun addSecurityUser(email: String, password: String) {
        viewModelScope.launch {
            try {
                val request = AdminSecurityRequest(email, password)
                RetrofitInstance.api.addSecurityUser(request)
                fetchSecurityUsers() // Refresh the list
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Update an existing security user
    fun updateSecurityUser(oldEmail: String, newEmail: String, newPassword: String) {
        viewModelScope.launch {
            try {
                val request = AdminSecurityRequest(
                    email = newEmail,
                    password = newPassword,
                    oldEmail = oldEmail
                )
                RetrofitInstance.api.updateSecurityUser(request)
                fetchSecurityUsers() // Refresh the list
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Delete a security user
    fun deleteSecurityUser(email: String) {
        viewModelScope.launch {
            try {
                RetrofitInstance.api.deleteSecurityUser(email)
                fetchSecurityUsers() // Refresh the list
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Fetch all verification records
    fun fetchRecords() {
        viewModelScope.launch {
            try {
                val recordsList = RetrofitInstance.api.getVerificationRecords()
                _records.value = recordsList
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Clear all records
    fun clearAllRecords() {
        viewModelScope.launch {
            try {
                RetrofitInstance.api.clearAllRecords()
                _records.value = emptyList()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}

// Factory for creating the ViewModel
class AdminViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AdminViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AdminViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}