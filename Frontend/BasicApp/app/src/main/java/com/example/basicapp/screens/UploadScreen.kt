package com.example.basicapp.screens

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.basicapp.utils.ImageUploader
import com.example.basicapp.api.uriToBitmap

@Composable
fun UploadScreen(navController: NavHostController,selectedUri: Uri?) {
    val context = LocalContext.current
    var isUploading by remember { mutableStateOf(false) }
    var uploadStatus by remember { mutableStateOf<String?>(null) }

    // Display selected image
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Selected Image Preview",
            fontSize = 20.sp,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        selectedUri?.let { uri ->
            Image(
                painter = rememberAsyncImagePainter(uri),
                contentDescription = "Selected Image",
                modifier = Modifier
                    .size(250.dp)
                    .padding(8.dp)
            )
        } ?: Text("No image selected", fontSize = 18.sp)

        Spacer(modifier = Modifier.height(24.dp))

        // Upload button
        Button(
            onClick = {
                selectedUri?.let { uri ->
                    isUploading = true
                    uploadStatus = null

                    // Launch coroutine to upload the image
                    if (selectedUri != null) {
                        ImageUploader.uploadImageToBackend(
                            navController = navController,
                            selectedUri = selectedUri,
                            context = context,
                            onSuccess = {
                                uploadStatus = "Image uploaded successfully!"
                                isUploading = false
                            },
                            onFailure = { error ->
                                uploadStatus = "Error: $error"
                                isUploading = false
                            }
                        )
                    } else {
                        uploadStatus = "Failed to load image from URI."
                        isUploading = false
                    }
                } ?: run {
                    uploadStatus = "No image selected."
                }
            },
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth(0.6f),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(text = "Upload Image")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Show uploading progress or status message
        if (isUploading) {
            CircularProgressIndicator()
        } else {
            uploadStatus?.let { status ->
                Text(text = status, fontSize = 16.sp, modifier = Modifier.padding(top = 8.dp))
            }
        }
    }
}