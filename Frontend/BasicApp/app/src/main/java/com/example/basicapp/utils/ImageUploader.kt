package com.example.basicapp.utils

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import android.widget.Toast
import androidx.navigation.NavHostController
import com.example.basicapp.api.ApiService
import com.example.basicapp.api.RetrofitInstance
import com.example.basicapp.model.ApiResponse
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import com.example.basicapp.model.ProcessResponse
import android.net.Uri

object ImageUploader {

    fun uploadImageToBackend(
        navController: NavHostController,
        selectedUri: Uri?,
        context: Context,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        if (selectedUri == null) {
            onFailure("No image selected")
            return
        }

        val inputStream = context.contentResolver.openInputStream(selectedUri)
        val fileBytes = inputStream?.readBytes()

        if (fileBytes == null) {
            onFailure("Failed to read image data")
            return
        }

        val requestBody = fileBytes.toRequestBody("image/jpeg".toMediaTypeOrNull())
        val imagePart = MultipartBody.Part.createFormData(
            "file",
            "uploaded_image.jpg",
            requestBody
        )

        val apiService: ApiService = RetrofitInstance.api
        val call = apiService.uploadImage(imagePart)

        call.enqueue(object : Callback<ApiResponse> {
            override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                if (response.isSuccessful && response.body()?.status == "success") {
                    Toast.makeText(context, "Image uploaded successfully!", Toast.LENGTH_SHORT).show()
                    onSuccess()
                    processImageAfterUpload(navController, context, selectedUri)
                } else {
                    val errorMessage = response.errorBody()?.string() ?: "Unknown error occurred"
                    Toast.makeText(context, "Failed to upload image: $errorMessage", Toast.LENGTH_SHORT).show()
                    onFailure(errorMessage)
                }
            }

            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                val errorMsg = "Error: ${t.localizedMessage}"
                Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
                Log.e("ImageUploader", errorMsg)
                onFailure(errorMsg)
            }
        })
    }
    private fun processImageAfterUpload(navController: NavHostController, context: Context, selectedUri: Uri?,) {
        val processCall = RetrofitInstance.api.processImageUpload()

        processCall.enqueue(object : Callback<ProcessResponse> {
            override fun onResponse(
                call: Call<ProcessResponse>,
                response: Response<ProcessResponse>
            ) {
                if (response.isSuccessful && response.body() != null) {
                    val result = response.body()!!
                    if (result.rollNoFound) {
                        val storedFaceEncoded = Uri.encode(result.storedFace ?: "")
                        val capturedFaceEncoded = Uri.encode(result.capturedFace ?: "")

                        // Navigate to ResultScreen with required data
                        navController.navigate("result/${result.verified}/${result.rollNumber}/${result.similarityScore}/$storedFaceEncoded/$capturedFaceEncoded")
                    } else {
                        // Navigate to ManualRollScreen if roll number is not found
                        val encodedUri = Uri.encode(selectedUri.toString())
                        navController.navigate("manual_roll/$encodedUri")
                    }
                } else {
                    Toast.makeText(context, "Error processing image!", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ProcessResponse>, t: Throwable) {
                Toast.makeText(
                    context,
                    "Failed to process image: ${t.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }
}

