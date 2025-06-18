package com.example.basicapp.screens

import android.content.Context
import android.net.Uri
import android.util.Log
import android.view.ViewGroup
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import com.example.basicapp.utils.ImageUploader
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executor
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CameraScreen(navController: NavHostController) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var isCapturing by remember { mutableStateOf(false) }
    var capturedImageUri by remember { mutableStateOf<Uri?>(null) }
    var lensFacing by remember { mutableStateOf(CameraSelector.LENS_FACING_BACK) } // Default to back camera

    // Create an executor for camera operations
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    // Clean up when leaving the Composable
    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
        }
    }

    // Remember the ImageCapture instance to take photos
    val imageCapture = remember { ImageCapture.Builder().build() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Take Photo") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF5F5DC))
        ) {
            when {
                capturedImageUri != null -> {
                    // Show progress and handle upload
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Processing image...",
                                fontSize = 18.sp
                            )
                        }
                    }

                    // Upload captured image
                    LaunchedEffect(capturedImageUri) {
                        ImageUploader.uploadImageToBackend(
                            navController = navController,
                            selectedUri = capturedImageUri,
                            context = context,
                            onSuccess = {
                                // Processing and navigation will be handled in the ImageUploader
                            },
                            onFailure = { error ->
                                // Reset and allow retaking photo on error
                                capturedImageUri = null
                            }
                        )
                    }
                }
                else -> {
                    // Camera preview
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Camera preview takes most of the screen
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .padding(16.dp)
                                .clip(RoundedCornerShape(16.dp))
                        ) {
                            CameraPreview(
                                imageCapture = imageCapture,
                                executor = cameraExecutor,
                                lensFacing = lensFacing
                            )

                            // Camera switch button in top-right corner
                            IconButton(
                                onClick = {
                                    lensFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK) {
                                        CameraSelector.LENS_FACING_FRONT
                                    } else {
                                        CameraSelector.LENS_FACING_BACK
                                    }
                                },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(16.dp)
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(Color.Black.copy(alpha = 0.6f))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Cameraswitch,
                                    contentDescription = "Switch Camera",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        // Capture button at the bottom
                        Box(
                            modifier = Modifier
                                .padding(bottom = 32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Button(
                                onClick = {
                                    if (!isCapturing) {
                                        isCapturing = true
                                        takePhoto(
                                            context = context,
                                            imageCapture = imageCapture,
                                            executor = cameraExecutor,
                                            onImageCaptured = { uri ->
                                                capturedImageUri = uri
                                                isCapturing = false
                                            },
                                            onError = { error ->
                                                Log.e("CameraScreen", "Photo capture failed: $error")
                                                isCapturing = false
                                            }
                                        )
                                    }
                                },
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(CircleShape),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                ),
                                enabled = !isCapturing
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Camera,
                                    contentDescription = "Take Photo",
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CameraPreview(
    imageCapture: ImageCapture,
    executor: Executor,
    lensFacing: Int
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    AndroidView(
        factory = { ctx ->
            val previewView = PreviewView(ctx).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                implementationMode = PreviewView.ImplementationMode.COMPATIBLE
            }

            previewView
        },
        modifier = Modifier.fillMaxSize(),
        update = { previewView ->
            val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()

                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

                val cameraSelector = when (lensFacing) {
                    CameraSelector.LENS_FACING_FRONT -> CameraSelector.DEFAULT_FRONT_CAMERA
                    else -> CameraSelector.DEFAULT_BACK_CAMERA
                }

                try {
                    // Unbind any bound use cases before rebinding
                    cameraProvider.unbindAll()

                    // Bind use cases to camera
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview,
                        imageCapture
                    )
                } catch (e: Exception) {
                    Log.e("CameraPreview", "Use case binding failed", e)
                }
            }, ContextCompat.getMainExecutor(context))
        }
    )
}

private fun takePhoto(
    context: Context,
    imageCapture: ImageCapture,
    executor: Executor,
    onImageCaptured: (Uri) -> Unit,
    onError: (ImageCaptureException) -> Unit
) {
    // Create a fixed file for the photo
    val photoFile = File(context.cacheDir, "captured.jpg")

    // Delete existing file if it exists
    if (photoFile.exists()) {
        photoFile.delete()
    }

    // Setup output options
    val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

    // Take the photo
    imageCapture.takePicture(
        outputOptions,
        executor,
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                val savedUri = Uri.fromFile(photoFile)
                onImageCaptured(savedUri)
            }

            override fun onError(exception: ImageCaptureException) {
                onError(exception)
            }
        }
    )
}