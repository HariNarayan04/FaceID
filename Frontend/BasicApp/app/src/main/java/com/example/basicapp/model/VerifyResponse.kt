package com.example.basicapp.model

import com.google.gson.annotations.SerializedName

data class VerifyResponse(
    @SerializedName("verified")
    val verified: Boolean,

    @SerializedName("roll_number")
    val rollNumber: String?,

    @SerializedName("similarity_score")
    val similarity: Float?,

    @SerializedName("stored_face")
    val storedFace: String?,

    @SerializedName("captured_face")
    val capturedFace: String?,

    @SerializedName("message")
    val message: String?
)