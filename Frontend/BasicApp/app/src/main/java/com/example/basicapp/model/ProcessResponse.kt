package com.example.basicapp.model

import com.google.gson.annotations.SerializedName

data class ProcessResponse(
    @SerializedName("roll_no_found")
    val rollNoFound: Boolean,  // true if roll number is found

    @SerializedName("roll_number")
    val rollNumber: String? = null,  // Roll number (if found)

    @SerializedName("verified")
    val verified: Boolean? = null,  // true if face is verified after roll no input

    @SerializedName("similarity_score")
    val similarityScore: Float? = null,  // Similarity score between stored and uploaded face

    @SerializedName("stored_face")
    val storedFace: String? = null,  // Base64-encoded stored face image

    @SerializedName("captured_face")
    val capturedFace: String? = null,  // Base64-encoded captured face image

    @SerializedName("message")
    val message: String? = null  // Success or error message
)