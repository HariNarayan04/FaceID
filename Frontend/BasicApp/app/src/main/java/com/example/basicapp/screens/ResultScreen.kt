package com.example.basicapp.screens

import android.graphics.Bitmap
import android.util.Base64
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale

@Composable
fun ResultScreen(
    navController: NavHostController,
    isVerified: Boolean,
    rollNumber: String,
    similarityScore: Float,
    storedFace: String,
    capturedFace: String
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = if (isVerified) "Face Verified Successfully!" else "Face Verification Failed.",
            style = MaterialTheme.typography.headlineMedium,
            color = if (isVerified) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = "Roll Number: $rollNumber")
        Text(text = "Similarity Score: ${"%.2f".format(similarityScore)}")

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            storedFace.decodeBase64ToBitmap()?.let {
                Image(
                    bitmap = it.asImageBitmap(),
                    contentDescription = "Stored Face",
                    modifier = Modifier
                        .size(150.dp) // Set a fixed size
                        .clip(RoundedCornerShape(8.dp)), // Optional rounded corners
                    contentScale = ContentScale.Crop // Ensures it fits within bounds
                )
            }
            capturedFace.decodeBase64ToBitmap()?.let {
                Image(
                    bitmap = it.asImageBitmap(),
                    contentDescription = "Captured Face",
                    modifier = Modifier
                        .size(150.dp) // Same fixed size
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = { navController.navigate("main") }) {
            Text(text = "Return to Home")
        }
    }
}

fun String.decodeBase64ToBitmap(): Bitmap? {
    return try {
        val decodedBytes = Base64.decode(this, Base64.DEFAULT)
        BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
    } catch (e: IllegalArgumentException) {
        null
    }
}