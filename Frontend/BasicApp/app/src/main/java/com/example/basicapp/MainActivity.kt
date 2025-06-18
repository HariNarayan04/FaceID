package com.example.basicapp

import android.Manifest
import android.os.Bundle
import android.os.Build
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.basicapp.screens.*
import com.example.basicapp.ui.theme.BasicAppTheme
import androidx.core.net.toUri
import android.net.Uri
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.basicapp.viewmodel.AdminViewModel
import com.example.basicapp.viewmodel.AdminViewModelFactory


class MainActivity : ComponentActivity() {

    private var hasAllPermissions by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check and request required permissions
        checkAndRequestPermissions()

        setContent {
            BasicAppTheme {
                if (hasAllPermissions) {
                    BasicApp()
                } else {
                    PermissionDeniedScreen {
                        checkAndRequestPermissions()
                    }
                }
            }
        }
    }

    private fun checkAndRequestPermissions() {
        // Define required permissions based on Android version
        val permissions = when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE -> {
                // Android 14+ (API 34+)
                arrayOf(
                    Manifest.permission.CAMERA,
                    Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
                )
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                // Android 13+ (API 33+)
                arrayOf(
                    Manifest.permission.CAMERA,
                    Manifest.permission.READ_MEDIA_IMAGES,
                    Manifest.permission.READ_MEDIA_VIDEO
                )
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
                // For Android 10-12 (API 29-32)
                arrayOf(
                    Manifest.permission.CAMERA,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                )
            }
            else -> {
                // For Android 9 and below
                arrayOf(
                    Manifest.permission.CAMERA,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            }
        }

        val launcher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissionsResult ->
            val allGranted = permissionsResult.all { it.value }
            if (allGranted) {
                hasAllPermissions = true
                Toast.makeText(this, "All permissions granted!", Toast.LENGTH_SHORT).show()
            } else {
                hasAllPermissions = false
                Toast.makeText(this, "Permission denied! Cannot proceed without camera and storage permissions.", Toast.LENGTH_LONG).show()
            }
        }
        launcher.launch(permissions)
    }
}


@Composable
fun BasicApp() {

    val navController = rememberNavController()
    val adminViewModel: AdminViewModel = viewModel(factory = AdminViewModelFactory())

    NavHost(
        navController = navController,
        startDestination = "home"
    ) {
        composable("home") { HomeScreen(navController) }
        composable("login") { LoginScreen(navController) }
        composable("main") { MainScreen(navController) }
        composable("camera") { CameraScreen(navController) }
        composable("upload/{imageUri}") { backStackEntry ->
            val encodedUri = backStackEntry.arguments?.getString("imageUri")
            val imageUri = encodedUri?.let { Uri.decode(it).toUri() }
            UploadScreen(navController, selectedUri = imageUri)
        }
        composable("manual_roll/{imageUri}") { backStackEntry ->
            val encodedUri = backStackEntry.arguments?.getString("imageUri") ?: ""
            val imageUri = Uri.decode(encodedUri)
            ManualRollScreen(navController, imageUri)
        }
        composable("result/{isVerified}/{rollNumber}/{similarityScore}/{storedFace}/{capturedFace}") { backStackEntry ->
            val isVerified = backStackEntry.arguments?.getString("isVerified")?.toBoolean() ?: false
            val rollNumber = backStackEntry.arguments?.getString("rollNumber") ?: "Unknown"
            val similarityScore = backStackEntry.arguments?.getString("similarityScore")?.toFloat() ?: 0f
            val storedFace = backStackEntry.arguments?.getString("storedFace") ?: ""
            val capturedFace = backStackEntry.arguments?.getString("capturedFace") ?: ""

            ResultScreen(navController, isVerified, rollNumber, similarityScore, storedFace, capturedFace)
        }
        // Admin routes
        composable("admin_login") { AdminLoginScreen(navController) }
        composable("admin_dashboard") { AdminDashboardScreen(navController) }
        composable("edit_security") { EditSecurityScreen(navController, adminViewModel) }
        composable("view_report") { ViewReportScreen(navController, adminViewModel) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavHostController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5DC)), // Beige background
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Face Identification System",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 32.dp)
        )
        Button(
            onClick = { navController.navigate("login") },
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth(0.6f),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("Security")
        }
        Button(
            onClick = {navController.navigate("admin_login") },
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth(0.6f),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("Admin")
        }
    }
}