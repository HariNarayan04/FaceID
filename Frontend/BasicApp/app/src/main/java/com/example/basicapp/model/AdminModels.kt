package com.example.basicapp.model

data class SecurityUser(
    val id: Int,
    val email: String,
    val password: String // Note: This would only be used for display purposes, not storing raw passwords
)

data class VerificationRecord(
    val id: Int,
    val roll_number: String,
    val timestamp: String,
    val status: String
)

// Request model for admin security operations
data class AdminSecurityRequest(
    val email: String,
    val password: String,
    val oldEmail: String? = null
)

//data class ApiResponse(
//    val filename: String,
//    val status: String
//)
//
//data class ProcessResponse(
//    val roll_no_found: Boolean,
//    val roll_number: String? = null,
//    val verified: Boolean = false,
//    val similarity_score: Float = 0f,
//    val stored_face: String? = null,
//    val captured_face: String? = null,
//    val status: String? = null,
//    val message: String
//)
//
//data class VerifyResponse(
//    val verified: Boolean,
//    val similarity_score: Float,
//    val stored_face: String,
//    val captured_face: String,
//    val roll_number: String,
//    val status: String?,
//    val message: String
//)