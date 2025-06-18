package com.example.basicapp.api

import com.example.basicapp.model.AdminSecurityRequest
import com.example.basicapp.model.ApiResponse
import com.example.basicapp.model.ProcessResponse
import com.example.basicapp.model.SecurityUser
import com.example.basicapp.model.VerificationRecord
import com.example.basicapp.model.VerifyResponse
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.*

// User data model
data class User(
    val email: String,
    val password: String
)

// Response from FastAPI server
data class LoginResponse(
    val message: String,
    val status: String
)

// Generic response model
data class GenericResponse(
    val status: String,
    val message: String
)

// API Interface
interface ApiService {
    @Multipart
    @POST("/upload")
    fun uploadImage(
        @Part image: MultipartBody.Part
    ): Call<ApiResponse>

    @POST("/process-image")
    fun processImageUpload(): Call<ProcessResponse>

    @POST("/login")
    fun loginUser(@Body user: User): Call<LoginResponse>

    // ✅ API to verify roll number after manual entry
    @FormUrlEncoded
    @POST("/manual-roll-input")
    fun verifyRollNumber(
        @Field("roll_number") rollNumber: String
    ): Call<VerifyResponse>

    // Admin APIs
    @GET("/admin/security-users")
    suspend fun getSecurityUsers(): List<SecurityUser>

    @POST("/admin/security-users")
    suspend fun addSecurityUser(@Body request: AdminSecurityRequest): GenericResponse

    @PUT("/admin/security-users")
    suspend fun updateSecurityUser(@Body request: AdminSecurityRequest): GenericResponse

    @DELETE("/admin/security-users/{email}")
    suspend fun deleteSecurityUser(@Path("email") email: String): GenericResponse

    @GET("/admin/records")
    suspend fun getVerificationRecords(): List<VerificationRecord>

    @DELETE("/admin/records")
    suspend fun clearAllRecords(): GenericResponse
}