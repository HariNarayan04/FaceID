package com.example.basicapp.screens

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.basicapp.api.RetrofitInstance
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.example.basicapp.model.VerifyResponse
import android.net.Uri
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.ui.graphics.Color

@Composable
fun ManualRollScreen(navController: NavHostController, imageUri: String) {
    var rollNumber by remember { mutableStateOf("") }
    var isVerifying by remember { mutableStateOf(false) }
    var verificationMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Enter Roll Number",
            fontSize = 24.sp,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        OutlinedTextField(
            value = rollNumber,
            onValueChange = { rollNumber = it },
            label = { Text("Roll Number") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black,
                focusedBorderColor = Color.Gray,
                unfocusedBorderColor = Color.LightGray,
                focusedLabelColor = Color.DarkGray,
                unfocusedLabelColor = Color.Gray
            )
        )
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (rollNumber.isNotEmpty()) {
                    isVerifying = true
                    verifyRollNumber(navController, rollNumber)
                } else {
                    Toast.makeText(navController.context, "Enter a valid Roll Number", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isVerifying
        ) {
            Text("Verify Face")
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (isVerifying) {
            CircularProgressIndicator()
        } else {
            verificationMessage?.let { message ->
                Text(
                    text = message,
                    fontSize = 18.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

// Verify face after manual roll input
// ✅ Add this function to verify roll number and navigate to ResultScreen
fun verifyRollNumber(navController: NavHostController, rollNumber: String) {
    val apiService = RetrofitInstance.api

    val call = apiService.verifyRollNumber(rollNumber)
    call.enqueue(object : Callback<VerifyResponse> {
        override fun onResponse(call: Call<VerifyResponse>, response: Response<VerifyResponse>) {
            if (response.isSuccessful && response.body() != null) {
                val result = response.body()!!
                val encodedStoredFace = Uri.encode(result.storedFace ?: "")
                val encodedCapturedFace = Uri.encode(result.capturedFace ?: "")
                val similarityScore = result.similarity ?: 0.0f

                // Navigate to ResultScreen with additional parameters
                navController.navigate("result/${result.verified}/${result.rollNumber}/${similarityScore}/${encodedStoredFace}/${encodedCapturedFace}")
            } else {
                Log.e("ManualRollScreen", "Verification failed. Try again.")
                Toast.makeText(navController.context, "Verification failed. Try again.", Toast.LENGTH_SHORT).show()
            }
        }

        override fun onFailure(call: Call<VerifyResponse>, t: Throwable) {
            Log.e("ManualRollScreen", "API call failed: ${t.message}")
            Toast.makeText(navController.context, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
        }
    })
}